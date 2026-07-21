package br.com.vagamais.controller;

import br.com.vagamais.dto.AuthResponse;
import br.com.vagamais.dto.LoginRequest;
import br.com.vagamais.dto.RegisterRequest;
import br.com.vagamais.service.AuthService;
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
    
    private final AuthService authService;
    
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/confirm-email")
    public ResponseEntity<Map<String, String>> confirmarEmail(@RequestBody Map<String, String> request) {
        String token = request.get("token");
        authService.confirmarEmail(token);
        return ResponseEntity.ok(Map.of("message", "Email confirmado com sucesso"));
    }
    
    @PostMapping("/resend-confirmation")
    public ResponseEntity<Map<String, String>> reenviarConfirmacao(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        authService.reenviarConfirmacao(email);
        return ResponseEntity.ok(Map.of("message", "Email de confirmação reenviado"));
    }
    
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refreshToken(@RequestBody Map<String, String> request) {
        String refreshToken = request.get("refreshToken");
        AuthResponse response = authService.refreshToken(refreshToken);
        return ResponseEntity.ok(response);
    }
}
