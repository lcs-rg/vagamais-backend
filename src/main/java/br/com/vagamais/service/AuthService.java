package br.com.vagamais.service;

import br.com.vagamais.config.JwtConfig;
import br.com.vagamais.dto.*;
import br.com.vagamais.model.AreaAtuacao;
import br.com.vagamais.model.User;
import br.com.vagamais.repository.UserRepository;
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
public class AuthService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtConfig jwtConfig;
    private final AreaAtuacaoService areaAtuacaoService;
    private final EmailService emailService;
    
    @Transactional
    public Map<String, String> register(RegisterRequest request) {
        // Verifica se email já existe
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email já cadastrado");
        }
        
        // Valida data de nascimento
        if (request.getDataNascimento() != null) {
            validarDataNascimento(request.getDataNascimento());
        }
        
        // Busca área de atuação se fornecida
        AreaAtuacao areaAtuacao = null;
        if (request.getAreaAtuacaoId() != null) {
            areaAtuacao = areaAtuacaoService.buscarPorId(request.getAreaAtuacaoId());
        }
        
        // Cria usuário
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
        
        // Gera token de confirmação
        String token = jwtConfig.generateEmailConfirmationToken(user.getId(), user.getEmail());
        user.setTokenConfirmacao(token);
        user.setTokenExpiraEm(LocalDateTime.now().plusHours(24));
        userRepository.save(user);
        
        // Envia email de confirmação
        emailService.enviarEmailConfirmacao(user.getEmail(), user.getNome(), token);
        
        log.info("Usuário registrado com sucesso: {}", user.getEmail());
        
        return Map.of("message", "Cadastro realizado com sucesso. Verifique seu email para confirmar a conta.");
    }
    
    @Transactional
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Credenciais inválidas"));
        
        // Verifica se é usuário local
        if (!"local".equals(user.getProvider())) {
            throw new RuntimeException("Usuário cadastrado via OAuth. Use login social.");
        }
        
        // Verifica senha
        if (!passwordEncoder.matches(request.getSenha(), user.getSenhaHash())) {
            throw new RuntimeException("Credenciais inválidas");
        }
        
        // Verifica se email foi confirmado
        if (!user.getEmailVerificado()) {
            throw new RuntimeException("Email não confirmado. Verifique sua caixa de entrada.");
        }
        
        log.info("Usuário logado com sucesso: {}", user.getEmail());
        
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
        
        log.info("Email confirmado com sucesso: {}", user.getEmail());
    }
    
    @Transactional
    public void reenviarConfirmacao(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Email não encontrado"));
        
        if (user.getEmailVerificado()) {
            throw new RuntimeException("Email já confirmado");
        }
        
        String token = jwtConfig.generateEmailConfirmationToken(user.getId(), user.getEmail());
        user.setTokenConfirmacao(token);
        user.setTokenExpiraEm(LocalDateTime.now().plusHours(24));
        userRepository.save(user);
        
        emailService.enviarEmailConfirmacao(user.getEmail(), user.getNome(), token);
        
        log.info("Email de confirmação reenviado para: {}", user.getEmail());
    }
    
    @Transactional
    public AuthResponse refreshToken(String refreshToken) {
        if (!jwtConfig.validateToken(refreshToken)) {
            throw new RuntimeException("Refresh token inválido");
        }
        
        String tokenType = jwtConfig.getTokenType(refreshToken);
        if (!"refresh".equals(tokenType)) {
            throw new RuntimeException("Token inválido");
        }
        
        UUID userId = jwtConfig.getUserIdFromToken(refreshToken);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
        
        return gerarAuthResponse(user);
    }
    
    private AuthResponse gerarAuthResponse(User user) {
        String accessToken = jwtConfig.generateAccessToken(user.getId(), user.getEmail());
        String refreshToken = jwtConfig.generateRefreshToken(user.getId());
        
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
                .expiresIn(jwtConfig.getJwtExpiration() / 1000)
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
