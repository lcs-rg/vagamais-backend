package br.com.vagamais.application.auth.dto;

import br.com.vagamais.application.areaatuacao.dto.AreaAtuacaoResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {

    private String accessToken;
    private String refreshToken;
    @Builder.Default
    private String tokenType = "Bearer";
    private Long expiresIn;
    private UserResponse user;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserResponse {
        private String id;
        private String nome;
        private String email;
        private Boolean emailVerificado;
        private String provider;
        private AreaAtuacaoResponse areaAtuacao;
    }
}
