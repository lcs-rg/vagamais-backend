package br.com.vagamais.infrastructure.auth.persistence;

import br.com.vagamais.domain.auth.User;
import br.com.vagamais.domain.auth.UserRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface JpaUserRepository extends UserRepository, JpaRepository<User, UUID> {

    @Override
    Optional<User> findByEmail(String email);

    @Override
    boolean existsByEmail(String email);

    @Override
    Optional<User> findByProviderAndProviderId(String provider, String providerId);

    @Override
    Optional<User> findByTokenConfirmacao(String token);

    @Override
    Optional<User> findById(UUID id);
}
