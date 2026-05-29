package com.company.aidevstaff.common.config;

import com.company.aidevstaff.worker.infrastructure.WorkerProperties;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class WorkerSecretFilter extends OncePerRequestFilter {
    private static final String WORKER_SECRET_HEADER = "X-Worker-Secret";

    private final WorkerProperties workerProperties;

    public WorkerSecretFilter(WorkerProperties workerProperties) {
        this.workerProperties = workerProperties;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        if (!requiresWorkerSecret(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        String configuredSecret = workerProperties.secret();
        String presentedSecret = request.getHeader(WORKER_SECRET_HEADER);
        if (configuredSecret == null || configuredSecret.isBlank() || !configuredSecret.equals(presentedSecret)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"message\":\"invalid worker secret\"}");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private boolean requiresWorkerSecret(HttpServletRequest request) {
        String path = request.getRequestURI();
        if (!"POST".equalsIgnoreCase(request.getMethod())) {
            return false;
        }
        return "/api/worker-jobs/claim-next".equals(path)
                || (path.startsWith("/api/worker-jobs/") && path.endsWith("/report"));
    }
}
