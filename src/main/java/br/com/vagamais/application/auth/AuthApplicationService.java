package br.com.vagamais.application.auth;

import br.com.vagamais.application.areaatuacao.AreaAtuacaoApplicationService;
import br.com.vagamais.application.areaatuacao.dto.AreaAtuacaoResponse;
import br.com.vagamais.application.auth.dto.AuthResponse;
import br.com.vagamais.application.auth.dto.LoginRequest;
import br.com.vagamais.application.auth.dto.RegisterRequest;
import br.com.vagamais.domain.areaatuacao.AreaAtuacao;
import br.com.vagamais.domain.auth.User;
import br.com.vagamais.domain.auth.UserRepository;
import br.com.vagamais.infrastructure.email.ResendEmailService;
import br.com.vagamais.infrastructure.security.JwtProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthApplicationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final AreaAtuacaoApplicationService areaAtuacaoApplicationService;
    private final ResendEmailService resendEmailService;

    @Transactional
    public Map<String, String> register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            return Map.of("message", "Se o email não estiver cadastrado, você receberá um link de confirmação.");
        }

        if (request.getDataNascimento() != null) {
            validarDataNascimento(request.getDataNascimento());
        }

        AreaAtuacao areaAtuacao = null;
        if (request.getAreaAtuacaoId() != null) {
            areaAtuacao = areaAtuacaoApplicationService.buscarPorId(request.getAreaAtuacaoId());
        }

        User user = User.builder()
                .nome(request.getNome())
                .email(request.getEmail())
                .senhaHash(passwordEncoder.encode(request.getSenha()))
                .dataNascimento(request.getDataNascimento())
                .telefone(request.getTelefone())
                .linkedin(request.getLinkedin())
                .areaAtuacao(areaAtuacao)
                .provider("local")
                .emailVerificado(false)
                .build();

        user = userRepository.save(user);

        String token = jwtProvider.generateEmailConfirmationToken(user.getId(), user.getEmail());
        user.setTokenConfirmacao(token);
        user.setTokenExpiraEm(LocalDateTime.now().plusHours(24));
        userRepository.save(user);

        resendEmailService.enviarEmailConfirmacao(user.getEmail(), user.getNome(), token);

        log.info("Usuário registrado: userId={}", user.getId());

        return Map.of("message", "Cadastro realizado com sucesso. Verifique seu email para confirmar a conta.");
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Credenciais inválidas"));

        if (!"local".equals(user.getProvider())) {
            throw new RuntimeException("Credenciais inválidas");
        }

        if (!passwordEncoder.matches(request.getSenha(), user.getSenhaHash())) {
            throw new RuntimeException("Credenciais inválidas");
        }

        if (!user.getEmailVerificado()) {
            throw new RuntimeException("Credenciais inválidas");
        }

        log.info("Login realizado: userId={}", user.getId());

        return gerarAuthResponse(user);
    }

    @Transactional
    public void confirmarEmail(String token) {
        User user = userRepository.findByTokenConfirmacao(token)
                .orElseThrow(() -> new RuntimeException("Token inválido"));

        if (user.getTokenExpiraEm().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Token expirado");
        }

        user.setEmailVerificado(true);
        user.setTokenConfirmacao(null);
        user.setTokenExpiraEm(null);
        userRepository.save(user);

        log.info("Email confirmado: userId={}", user.getId());
    }

    @Transactional
    public void reenviarConfirmacao(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Se o email estiver cadastrado, um novo link será enviado."));

        if (user.getEmailVerificado()) {
            throw new RuntimeException("Se o email estiver cadastrado, um novo link será enviado.");
        }

        String token = jwtProvider.generateEmailConfirmationToken(user.getId(), user.getEmail());
        user.setTokenConfirmacao(token);
        user.setTokenExpiraEm(LocalDateTime.now().plusHours(24));
        userRepository.save(user);

        resendEmailService.enviarEmailConfirmacao(user.getEmail(), user.getNome(), token);

        log.info("Confirmacao reenviada: userId={}", user.getId());
    }

    @Transactional
    public AuthResponse refreshToken(String refreshToken) {
        if (!jwtProvider.validateToken(refreshToken)) {
            throw new RuntimeException("Refresh token inválido");
        }

        String tokenType = jwtProvider.getTokenType(refreshToken);
        if (!"refresh".equals(tokenType)) {
            throw new RuntimeException("Token inválido");
        }

        UUID userId = jwtProvider.getUserIdFromToken(refreshToken);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        return gerarAuthResponse(user);
    }

    private AuthResponse gerarAuthResponse(User user) {
        String accessToken = jwtProvider.generateAccessToken(user.getId(), user.getEmail());
        String refreshToken = jwtProvider.generateRefreshToken(user.getId());

        AuthResponse.UserResponse userResponse = AuthResponse.UserResponse.builder()
                .id(user.getId().toString())
                .nome(user.getNome())
                .email(user.getEmail())
                .emailVerificado(user.getEmailVerificado())
                .provider(user.getProvider())
                .areaAtuacao(user.getAreaAtuacao() != null ?
                    AreaAtuacaoResponse.builder()
                        .id(user.getAreaAtuacao().getId())
                        .nome(user.getAreaAtuacao().getNome())
                        .slug(user.getAreaAtuacao().getSlug())
                        .build() : null)
                .build();

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtProvider.getJwtExpiration() / 1000)
                .user(userResponse)
                .build();
    }

    private void validarDataNascimento(LocalDate dataNascimento) {
        LocalDate hoje = LocalDate.now();
        int idade = hoje.getYear() - dataNascimento.getYear();

        if (idade < 14) {
            throw new RuntimeException("Idade mínima: 14 anos");
        }

        if (idade > 120) {
            throw new RuntimeException("Data de nascimento inválida");
        }
    }
}
