package org.example.crosswords_backend.service;

import org.example.crosswords_backend.dto.TokenDto;
import org.example.crosswords_backend.exception.InvalidLoginException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class TokenService {

    private final JwtEncoder jwtEncoder;
    private final long lifetimeMinutes;

    public TokenService(JwtEncoder jwtEncoder, @Value("${app.jwt.lifetime-minutes}") long lifetimeMinutes) {
        this.jwtEncoder = jwtEncoder;
        this.lifetimeMinutes = lifetimeMinutes;
    }

    // The token holds who the user is (sub) and the role, signed so the client cannot change them.
    // It is not encrypted: anyone can read the claims, so nothing secret goes in here.
    public TokenDto createToken(Authentication authentication) {
        requirePasswordLogin(authentication);
        Instant now = Instant.now();
        Instant expiresAt = now.plus(lifetimeMinutes, ChronoUnit.MINUTES);
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("crosswords_backend")
                .issuedAt(now)
                .expiresAt(expiresAt)
                .subject(authentication.getName())
                .claim("roles", roles(authentication))
                .build();
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        String token = jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
        return new TokenDto(token, expiresAt);
    }

    // A new token only for username and password. Otherwise a stolen token could be swapped for new ones forever.
    private void requirePasswordLogin(Authentication authentication) {
        if (!(authentication instanceof UsernamePasswordAuthenticationToken)) {
            throw new InvalidLoginException("Log in with username and password (HTTP Basic) to get a token");
        }
    }

    // "ROLE_ADMIN" becomes "ADMIN". JwtConfig adds the prefix back when the token is read.
    private List<String> roles(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .map(authority -> authority.replaceFirst("^ROLE_", ""))
                .toList();
    }
}
