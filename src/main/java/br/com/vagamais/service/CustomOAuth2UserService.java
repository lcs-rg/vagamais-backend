package br.com.vagamais.service;

import br.com.vagamais.config.JwtConfig;
import br.com.vagamais.model.User;
import br.com.vagamais.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {
    
    private final UserRepository userRepository;
    private final JwtConfig jwtConfig;
    
    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        
        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");
        String provider = userRequest.getClientRegistration().getRegistrationId();
        String providerId = oAuth2User.getName();
        
        // Busca usuário existente ou cria novo
        Optional<User> userOptional = userRepository.findByEmail(email);
        
        User user;
        if (userOptional.isPresent()) {
            // Usuário existente - atualiza provider se necessário
            user = userOptional.get();
            if (user.getProvider() == null || user.getProvider().equals("local")) {
                user.setProvider(provider);
                user.setProviderId(providerId);
                user.setEmailVerificado(true); // OAuth já verifica o email
                userRepository.save(user);
            }
        } else {
            // Novo usuário
            user = User.builder()
                    .email(email)
                    .nome(name != null ? name : email.split("@")[0])
                    .provider(provider)
                    .providerId(providerId)
                    .emailVerificado(true) // OAuth já verifica o email
                    .build();
            userRepository.save(user);
        }
        
        // Gera tokens JWT
        String accessToken = jwtConfig.generateAccessToken(user.getId(), user.getEmail());
        String refreshToken = jwtConfig.generateRefreshToken(user.getId());
        
        // Adiciona tokens aos atributos do usuário
        Map<String, Object> attributes = new HashMap<>(oAuth2User.getAttributes());
        attributes.put("accessToken", accessToken);
        attributes.put("refreshToken", refreshToken);
        attributes.put("userId", user.getId().toString());
        
        return new DefaultOAuth2User(
                oAuth2User.getAuthorities(),
                attributes,
                "email"
        );
    }
}
