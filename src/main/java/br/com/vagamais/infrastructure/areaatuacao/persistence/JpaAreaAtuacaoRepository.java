package br.com.vagamais.infrastructure.areaatuacao.persistence;

import br.com.vagamais.domain.areaatuacao.AreaAtuacao;
import br.com.vagamais.domain.areaatuacao.AreaAtuacaoRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface JpaAreaAtuacaoRepository extends AreaAtuacaoRepository, JpaRepository<AreaAtuacao, Integer> {

    @Override
    Optional<AreaAtuacao> findBySlug(String slug);

    @Override
    List<AreaAtuacao> findByAtivoTrueOrderByOrdemAsc();

    @Override
    boolean existsByNome(String nome);

    @Override
    boolean existsBySlug(String slug);

    @Override
    Optional<AreaAtuacao> findById(Integer id);
}
