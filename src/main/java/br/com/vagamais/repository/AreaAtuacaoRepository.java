package br.com.vagamais.repository;

import br.com.vagamais.model.AreaAtuacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AreaAtuacaoRepository extends JpaRepository<AreaAtuacao, Integer> {
    
    Optional<AreaAtuacao> findBySlug(String slug);
    
    List<AreaAtuacao> findByAtivoTrueOrderByOrdemAsc();
    
    boolean existsByNome(String nome);
    
    boolean existsBySlug(String slug);
}
