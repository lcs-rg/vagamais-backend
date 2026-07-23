package br.com.vagamais.application.areaatuacao.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AreaAtuacaoResponse {

    private Integer id;
    private String nome;
    private String slug;
    private String descricao;
    private Boolean ativo;
    private Integer ordem;
}
