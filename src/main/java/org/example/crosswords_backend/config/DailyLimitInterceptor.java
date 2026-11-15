package org.example.crosswords_backend.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.crosswords_backend.service.DailyLimitService;
import org.example.crosswords_backend.model.PermissionName;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

// Runs before the controller on the two endpoints that cost money (registered in WebConfig), like Spring Security
// runs before every controller to check login. The services stay unchanged and do not get one more dependency.
// A DailyLimitException thrown here is handled by ControllerExceptionHandler like any other (429).
@Component
public class DailyLimitInterceptor implements HandlerInterceptor {

    private final DailyLimitService dailyLimitService;

    public DailyLimitInterceptor(DailyLimitService dailyLimitService) {
        this.dailyLimitService = dailyLimitService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // Not logged in is Spring Security's job (401). A role with UNLIMITED_USAGE (the admin) has no limit.
        if (!"POST".equals(request.getMethod()) || request.getUserPrincipal() == null || hasUnlimitedUsage()) {
            return true;
        }
        // The principal's name is the user's id (see AppUserDetailsService).
        int userId = Integer.parseInt(request.getUserPrincipal().getName());
        if (request.getRequestURI().endsWith("/submissions/claude")) {
            dailyLimitService.requireClaudeAllowed(userId);
        } else {
            dailyLimitService.requireCrosswordAllowed(userId);
        }
        return true;
    }

    private boolean hasUnlimitedUsage() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals(PermissionName.UNLIMITED_USAGE.name()));
    }
}
