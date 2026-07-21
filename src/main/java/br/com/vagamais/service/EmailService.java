package br.com.vagamais.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {
    
    @Value("${app.email.resend-api-key}")
    private String resendApiKey;
    
    @Value("${app.email.from-address}")
    private String fromAddress;
    
    @Value("${app.frontend.url}")
    private String frontendUrl;
    
    private final HttpClient httpClient = HttpClient.newHttpClient();
    
    public void enviarEmailConfirmacao(String email, String nome, String token) {
        String linkConfirmacao = frontendUrl + "/confirmar-email?token=" + token;
        
        String html = """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <title>Confirme seu email - Vaga+</title>
            </head>
            <body style="font-family: Arial, sans-serif; line-height: 1.6; color: #333;">
                <div style="max-width: 600px; margin: 0 auto; padding: 20px;">
                    <h2 style="color: #2563eb;">Bem-vindo ao Vaga+, %s!</h2>
                    <p>Obrigado por se cadastrar. Para começar a usar o Vaga+, confirme seu email clicando no botão abaixo:</p>
                    <div style="text-align: center; margin: 30px 0;">
                        <a href="%s" 
                           style="background-color: #2563eb; color: white; padding: 12px 30px; text-decoration: none; border-radius: 5px; font-weight: bold;">
                            Confirmar Email
                        </a>
                    </div>
                    <p>Ou copie e cole este link no seu navegador:</p>
                    <p style="background-color: #f3f4f6; padding: 10px; word-break: break-all;">%s</p>
                    <p style="color: #6b7280; font-size: 14px;">Este link expira em 24 horas.</p>
                    <p style="color: #6b7280; font-size: 14px;">Se você não criou esta conta, ignore este email.</p>
                </div>
            </body>
            </html>
            """.formatted(nome, linkConfirmacao, linkConfirmacao);
        
        enviarEmail(email, "Confirme seu email - Vaga+", html);
    }
    
    private void enviarEmail(String to, String subject, String html) {
        try {
            String jsonPayload = """
                {
                    "from": "%s",
                    "to": ["%s"],
                    "subject": "%s",
                    "html": %s
                }
                """.formatted(fromAddress, to, subject, escapeJson(html));
            
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.resend.com/emails"))
                    .header("Authorization", "Bearer " + resendApiKey)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                    .build();
            
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() == 200) {
                log.info("Email enviado com sucesso para: {}", to);
            } else {
                log.error("Erro ao enviar email para {}: {} - {}", to, response.statusCode(), response.body());
                throw new RuntimeException("Erro ao enviar email");
            }
        } catch (Exception e) {
            log.error("Erro ao enviar email para {}: {}", to, e.getMessage());
            throw new RuntimeException("Erro ao enviar email: " + e.getMessage());
        }
    }
    
    private String escapeJson(String value) {
        return "\"" + value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t") + "\"";
    }
}
