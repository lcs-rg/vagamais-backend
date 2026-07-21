package br.com.vagamais.repository;

import br.com.vagamais.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    
    Optional<User> findByEmail(String email);
    
    boolean existsByEmail(String email);
    
    Optional<User> findByProviderAndProviderId(String provider, String providerId);
    
    Optional<User> findByTokenConfirmacao(String token);
}
