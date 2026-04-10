package com.bank.paymentsportal.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private static final int MAX_ATTEMPTS = 10;
    private static final long WINDOW_SECONDS = 60;

    private final Map<String, AttemptWindow> attempts = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String path = request.getRequestURI();
        if ("POST".equalsIgnoreCase(request.getMethod()) &&
                ("/api/auth/login".equals(path) || "/api/employee/auth/login".equals(path))) {
            String key = request.getRemoteAddr();
            AttemptWindow window = attempts.computeIfAbsent(key, ignored -> new AttemptWindow());
            if (window.isExpired()) {
                window.reset();
            }
            if (window.count.incrementAndGet() > MAX_ATTEMPTS) {
                response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                response.setContentType("application/json");
                response.getWriter().write("{\"message\":\"Too many login attempts. Please try again later.\"}");
                return;
            }
        }
        filterChain.doFilter(request, response);
    }

    private static final class AttemptWindow {
        private final AtomicInteger count = new AtomicInteger(0);
        private Instant startedAt = Instant.now();

        private boolean isExpired() {
            return Instant.now().isAfter(startedAt.plusSeconds(WINDOW_SECONDS));
        }

        private void reset() {
            count.set(0);
            startedAt = Instant.now();
        }
    }
}
