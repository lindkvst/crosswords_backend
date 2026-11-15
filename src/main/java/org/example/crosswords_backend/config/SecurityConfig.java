package org.example.crosswords_backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
public class SecurityConfig {

    // The only origin allowed to call the API from a browser: IntelliJ's built-in server for the frontend.
    private static final String FRONTEND_ORIGIN = "http://localhost:63342";

    // BCrypt is slow on purpose and salts every hash, so a leaked table cannot be cracked with lookup tables.
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   JsonAuthenticationEntryPoint entryPoint) throws Exception {
        http
                // CSRF attacks rely on the browser sending a cookie by itself. We use no cookies and no session:
                // the frontend adds the Basic header explicitly to every call, which another site cannot do.
                .csrf(csrf -> csrf.disable())
                // Uses the corsConfigurationSource bean below. CORS runs before login is checked,
                // so the browser's preflight (OPTIONS, sent without credentials) gets its answer instead of 401.
                .cors(Customizer.withDefaults())
                // Stateless: no session and no JSESSIONID cookie. Every request is authenticated on its own.
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.POST, "/api/users/register").permitAll()
                        // Spring forwards errors (e.g. 404) to /error. Without this the real error would become a 401.
                        .requestMatchers("/error").permitAll()
                        // Everything else needs login. Above all the Claude endpoint, because each call costs money.
                        .anyRequest().authenticated())
                .httpBasic(basic -> basic.authenticationEntryPoint(entryPoint))
                .exceptionHandling(exceptions -> exceptions.authenticationEntryPoint(entryPoint));
        return http.build();
    }

    // CORS lives here instead of @CrossOrigin on each controller, so one place decides which origin may call us.
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of(FRONTEND_ORIGIN));
        config.setAllowedMethods(List.of("GET", "POST", "OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", config);
        return source;
    }
}
