package org.example.crosswords_backend.service;

import org.example.crosswords_backend.model.AppUser;
import org.example.crosswords_backend.repository.AppUserRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

// Spring Security calls this on every request with a Basic header. It compares the sent password
// with the stored BCrypt hash itself, using our PasswordEncoder bean.
// The user logs in with the email, but the name Spring keeps for the logged-in user is the id. That way
// principal.getName() is the id both after Basic and after a JWT (whose "sub" is made from it), and a changed
// email or username does not change who the user is.
@Service
public class AppUserDetailsService implements UserDetailsService {

    private final AppUserRepository appUserRepository;

    public AppUserDetailsService(AppUserRepository appUserRepository) {
        this.appUserRepository = appUserRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) {
        AppUser user = appUserRepository.findByEmail(UserService.normalizeEmail(email))
                .orElseThrow(() -> new UsernameNotFoundException("Unknown user"));
        // hasRole("ADMIN") checks for the authority "ROLE_ADMIN", so the prefix is added here.
        return User.withUsername(String.valueOf(user.getId()))
                .password(user.getPasswordHash())
                .authorities("ROLE_" + user.getRole().name())
                .build();
    }
}
