package br.com.vagamais.controller;

import br.com.vagamais.dto.AreaAtuacaoResponse;
import br.com.vagamais.service.AreaAtuacaoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/areas-atuacao")
@RequiredArgsConstructor
public class AreaAtuacaoController {
    
    private final AreaAtuacaoService areaAtuacaoService;
    
    @GetMapping
    public ResponseEntity<List<AreaAtuacaoResponse>> listar() {
        List<AreaAtuacaoResponse> areas = areaAtuacaoService.listarAtivas();
        return ResponseEntity.ok(areas);
    }
}
