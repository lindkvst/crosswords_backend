package org.example.crosswords_backend.config;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Stream;

// JWT with one shared secret (HMAC-SHA256): the same key signs the token at login and checks it on every request.
// That is fine when one app does both. With several services one would use a key pair (RS256) instead.
@Configuration
public class JwtConfig {

    // HS256 needs a key of at least 256 bits, so a short JWT_SECRET stops the app at startup instead of failing later.
    @Bean
    public SecretKey jwtSecretKey(@Value("${app.jwt.secret}") String secret) {
        byte[] bytes = secret.getBytes(StandardCharsets.UTF_8);
        if (bytes.length < 32) {
            throw new IllegalStateException("JWT_SECRET must be at least 32 bytes (256 bits)");
        }
        return new SecretKeySpec(bytes, "HmacSHA256");
    }

    @Bean
    public JwtEncoder jwtEncoder(SecretKey jwtSecretKey) {
        return new NimbusJwtEncoder(new ImmutableSecret<>(jwtSecretKey));
    }

    // Checks the signature and the expiry time. A token that was changed or has expired gives 401.
    @Bean
    public JwtDecoder jwtDecoder(SecretKey jwtSecretKey) {
        return NimbusJwtDecoder.withSecretKey(jwtSecretKey).macAlgorithm(MacAlgorithm.HS256).build();
    }

    // Turns the token's claims back into the same authorities a Basic login gets: "roles" ["ADMIN"] becomes
    // ROLE_ADMIN, and each entry in "permissions" (e.g. VIEW_STATS) becomes an authority of that name.
    // Spring's default would read a "scope" claim and make SCOPE_ authorities instead.
    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(jwt -> {
            Stream<String> roles = claim(jwt, "roles").stream().map(role -> "ROLE_" + role);
            Stream<String> permissions = claim(jwt, "permissions").stream();
            return Stream.concat(roles, permissions)
                    .<GrantedAuthority>map(SimpleGrantedAuthority::new)
                    .toList();
        });
        return converter;
    }

    private static List<String> claim(Jwt jwt, String name) {
        List<String> values = jwt.getClaimAsStringList(name);
        return values == null ? List.of() : values;
    }
}
