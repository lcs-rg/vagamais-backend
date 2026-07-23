package br.com.vagamais.domain.auth;

import br.com.vagamais.domain.areaatuacao.AreaAtuacao;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 100)
    private String nome;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(name = "senha_hash", length = 255)
    private String senhaHash;

    @Column(name = "data_nascimento")
    private LocalDate dataNascimento;

    @Column(length = 20)
    private String telefone;

    @Column(length = 255)
    private String linkedin;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "area_atuacao_id")
    private AreaAtuacao areaAtuacao;

    @Column(name = "email_verificado", nullable = false)
    @Builder.Default
    private Boolean emailVerificado = false;

    @Column(name = "token_confirmacao", length = 255)
    private String tokenConfirmacao;

    @Column(name = "token_expira_em")
    private LocalDateTime tokenExpiraEm;

    @Column(length = 20, nullable = false)
    @Builder.Default
    private String provider = "local";

    @Column(name = "provider_id", length = 255)
    private String providerId;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public boolean isOAuthUser() {
        return !"local".equals(provider);
    }

    public boolean isEmailVerificado() {
        return emailVerificado != null && emailVerificado;
    }
}
