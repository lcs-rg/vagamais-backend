package br.com.vagamais.domain.auth;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    Optional<User> findByProviderAndProviderId(String provider, String providerId);

    Optional<User> findByTokenConfirmacao(String token);

    Optional<User> findById(UUID id);

    User save(User user);
}
