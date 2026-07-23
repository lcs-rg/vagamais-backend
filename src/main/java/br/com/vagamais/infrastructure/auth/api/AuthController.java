package br.com.vagamais.infrastructure.auth.api;

import br.com.vagamais.application.auth.AuthApplicationService;
import br.com.vagamais.application.auth.dto.AuthResponse;
import br.com.vagamais.application.auth.dto.LoginRequest;
import br.com.vagamais.application.auth.dto.RegisterRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthApplicationService authApplicationService;

    @PostMapping("/register")
    public ResponseEntity<Map<String, String>> register(@Valid @RequestBody RegisterRequest request) {
        Map<String, String> response = authApplicationService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authApplicationService.login(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/confirm-email")
    public ResponseEntity<Map<String, String>> confirmarEmail(@RequestBody Map<String, String> request) {
        String token = request.get("token");
        authApplicationService.confirmarEmail(token);
        return ResponseEntity.ok(Map.of("message", "Email confirmado com sucesso"));
    }

    @PostMapping("/resend-confirmation")
    public ResponseEntity<Map<String, String>> reenviarConfirmacao(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        authApplicationService.reenviarConfirmacao(email);
        return ResponseEntity.ok(Map.of("message", "Email de confirmação reenviado"));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refreshToken(@RequestBody Map<String, String> request) {
        String refreshToken = request.get("refreshToken");
        AuthResponse response = authApplicationService.refreshToken(refreshToken);
        return ResponseEntity.ok(response);
    }
}
