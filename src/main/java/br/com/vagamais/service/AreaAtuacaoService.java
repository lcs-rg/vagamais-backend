package br.com.vagamais.service;

import br.com.vagamais.dto.AreaAtuacaoResponse;
import br.com.vagamais.model.AreaAtuacao;
import br.com.vagamais.repository.AreaAtuacaoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AreaAtuacaoService {
    
    private final AreaAtuacaoRepository areaAtuacaoRepository;
    
    @Transactional(readOnly = true)
    public List<AreaAtuacaoResponse> listarAtivas() {
        return areaAtuacaoRepository.findByAtivoTrueOrderByOrdemAsc()
                .stream()
                .map(this::toResponse)
                .toList();
    }
    
    @Transactional(readOnly = true)
    public AreaAtuacao buscarPorId(Integer id) {
        return areaAtuacaoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Área de atuação não encontrada"));
    }
    
    @Transactional(readOnly = true)
    public AreaAtuacao buscarPorSlug(String slug) {
        return areaAtuacaoRepository.findBySlug(slug)
                .orElseThrow(() -> new RuntimeException("Área de atuação não encontrada"));
    }
    
    private AreaAtuacaoResponse toResponse(AreaAtuacao area) {
        return AreaAtuacaoResponse.builder()
                .id(area.getId())
                .nome(area.getNome())
                .slug(area.getSlug())
                .descricao(area.getDescricao())
                .ativo(area.getAtivo())
                .ordem(area.getOrdem())
                .build();
    }
}
