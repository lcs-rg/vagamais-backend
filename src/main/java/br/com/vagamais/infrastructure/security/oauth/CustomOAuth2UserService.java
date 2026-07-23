package br.com.vagamais.infrastructure.security.oauth;

import br.com.vagamais.domain.auth.User;
import br.com.vagamais.domain.auth.UserRepository;
import br.com.vagamais.infrastructure.security.JwtProvider;
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
    private final JwtProvider jwtProvider;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");
        String provider = userRequest.getClientRegistration().getRegistrationId();
        String providerId = oAuth2User.getName();

        Optional<User> userOptional = userRepository.findByEmail(email);

        User user;
        if (userOptional.isPresent()) {
            user = userOptional.get();
            if (user.getProvider() != null && user.getProvider().equals(provider)) {
                // Usuário existente do mesmo provider - login normal
            } else {
                throw new OAuth2AuthenticationException(
                    "Já existe uma conta com este email. Faça login com email e senha."
                );
            }
        } else {
            user = User.builder()
                    .email(email)
                    .nome(name != null ? name : email.split("@")[0])
                    .provider(provider)
                    .providerId(providerId)
                    .emailVerificado(true)
                    .build();
            userRepository.save(user);
        }

        String accessToken = jwtProvider.generateAccessToken(user.getId(), user.getEmail());
        String refreshToken = jwtProvider.generateRefreshToken(user.getId());

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
