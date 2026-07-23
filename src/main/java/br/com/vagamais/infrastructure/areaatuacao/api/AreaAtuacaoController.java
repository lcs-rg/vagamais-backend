package br.com.vagamais.infrastructure.areaatuacao.api;

import br.com.vagamais.application.areaatuacao.AreaAtuacaoApplicationService;
import br.com.vagamais.application.areaatuacao.dto.AreaAtuacaoResponse;
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

    private final AreaAtuacaoApplicationService areaAtuacaoApplicationService;

    @GetMapping
    public ResponseEntity<List<AreaAtuacaoResponse>> listar() {
        List<AreaAtuacaoResponse> areas = areaAtuacaoApplicationService.listarAtivas();
        return ResponseEntity.ok(areas);
    }
}
