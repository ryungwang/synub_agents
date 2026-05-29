package com.company.aidevstaff.admin.presentation;

import com.company.aidevstaff.common.config.AdminProperties;
import com.company.aidevstaff.common.config.AdminTokenFilter;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/admin/session")
public class AdminSessionController {
    private final AdminProperties adminProperties;

    public AdminSessionController(AdminProperties adminProperties) {
        this.adminProperties = adminProperties;
    }

    @GetMapping("/status")
    public AdminSessionStatus status() {
        return new AdminSessionStatus(adminProperties.enabled());
    }

    @PostMapping("/verify")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void verify(HttpServletRequest request) {
        if (!adminProperties.enabled()) {
            return;
        }
        String presentedToken = request.getHeader(AdminTokenFilter.ADMIN_TOKEN_HEADER);
        if (!adminProperties.matches(presentedToken)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "invalid admin token");
        }
    }

    public record AdminSessionStatus(boolean enabled) {
    }
}
