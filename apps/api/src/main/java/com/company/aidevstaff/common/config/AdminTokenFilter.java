package com.company.aidevstaff.common.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class AdminTokenFilter extends OncePerRequestFilter {
    public static final String ADMIN_TOKEN_HEADER = "X-Admin-Token";

    private final AdminProperties adminProperties;

    public AdminTokenFilter(AdminProperties adminProperties) {
        this.adminProperties = adminProperties;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        if (!adminProperties.enabled() || !requiresAdminToken(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        String presentedToken = request.getHeader(ADMIN_TOKEN_HEADER);
        if (!adminProperties.matches(presentedToken)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"message\":\"invalid admin token\"}");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private boolean requiresAdminToken(HttpServletRequest request) {
        String path = request.getRequestURI();
        String method = request.getMethod();

        if ("OPTIONS".equalsIgnoreCase(method) || path.startsWith("/api/admin/session")) {
            return false;
        }
        if (path.equals("/api/github/issues") && "POST".equalsIgnoreCase(method)) {
            return false;
        }
        if (path.equals("/api/worker-jobs/claim-next") || (path.startsWith("/api/worker-jobs/") && path.endsWith("/report"))) {
            return false;
        }
        if (path.startsWith("/api/approvals/tasks/") || path.startsWith("/api/runs/tasks/")) {
            return false;
        }
        if (path.equals("/api/workspace/projects") && "GET".equalsIgnoreCase(method)) {
            return false;
        }
        if (path.startsWith("/api/workspace/projects/") && path.endsWith("/work-requests")) {
            return false;
        }
        if (path.equals("/api/workspace/work-requests") && "GET".equalsIgnoreCase(method)) {
            return false;
        }

        return path.startsWith("/api/agents")
                || path.startsWith("/api/tasks")
                || path.startsWith("/api/approvals")
                || path.startsWith("/api/audit-logs")
                || path.startsWith("/api/github")
                || path.startsWith("/api/local-services")
                || path.startsWith("/api/worker-jobs")
                || path.startsWith("/api/runs")
                || path.startsWith("/api/workspace");
    }
}
