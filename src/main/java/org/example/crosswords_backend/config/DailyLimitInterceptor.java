package org.example.crosswords_backend.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.crosswords_backend.service.DailyLimitService;
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
        // Not logged in is Spring Security's job (401). The admin pays the bill, so the admin has no limit.
        if (!"POST".equals(request.getMethod()) || request.getUserPrincipal() == null || request.isUserInRole("ADMIN")) {
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
}
