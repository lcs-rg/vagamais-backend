package br.com.vagamais.domain.areaatuacao;

import java.util.List;
import java.util.Optional;

public interface AreaAtuacaoRepository {

    Optional<AreaAtuacao> findBySlug(String slug);

    List<AreaAtuacao> findByAtivoTrueOrderByOrdemAsc();

    boolean existsByNome(String nome);

    boolean existsBySlug(String slug);

    Optional<AreaAtuacao> findById(Integer id);
}
