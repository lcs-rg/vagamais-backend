package br.com.vagamais.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
public class RateLimitFilter extends OncePerRequestFilter {

    private static final int MAX_REQUESTS = 20;
    private static final long WINDOW_MS = 60_000;
    private static final int BLOCK_DURATION_MS = 300_000;

    private final Map<String, RateLimitEntry> requestCounts = new ConcurrentHashMap<>();

    private static class RateLimitEntry {
        final long windowStart;
        int count;
        long blockedUntil;

        RateLimitEntry(long windowStart) {
            this.windowStart = windowStart;
            this.count = 1;
        }
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String path = request.getRequestURI();
        if (!path.startsWith("/api/auth/login") && !path.startsWith("/api/auth/register") &&
            !path.startsWith("/api/auth/resend-confirmation")) {
            filterChain.doFilter(request, response);
            return;
        }

        String clientIp = getClientIp(request);
        String key = path + ":" + clientIp;
        long now = System.currentTimeMillis();

        RateLimitEntry entry = requestCounts.compute(key, (k, existing) -> {
            if (existing == null) return new RateLimitEntry(now);
            if (existing.blockedUntil > now) return existing;
            if (now - existing.windowStart > WINDOW_MS) return new RateLimitEntry(now);
            existing.count++;
            return existing;
        });

        if (entry.blockedUntil > now) {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"Muitas requisições. Tente novamente mais tarde.\"}");
            return;
        }

        if (now - entry.windowStart <= WINDOW_MS && entry.count > MAX_REQUESTS) {
            entry.blockedUntil = now + BLOCK_DURATION_MS;
            log.warn("Rate limit bloqueado: ip={} path={}", clientIp, path);
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"Muitas requisições. Tente novamente mais tarde.\"}");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private String getClientIp(HttpServletRequest request) {
        String xForwarded = request.getHeader("X-Forwarded-For");
        if (xForwarded != null && !xForwarded.isBlank()) {
            return xForwarded.split(",")[0].trim();
        }
        String realIp = request.getHeader("X-Real-IP");
        if (realIp != null && !realIp.isBlank()) {
            return realIp;
        }
        return request.getRemoteAddr();
    }
}
