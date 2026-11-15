package org.example.crosswords_backend.service;

import org.example.crosswords_backend.model.AppUser;
import org.example.crosswords_backend.repository.AppUserRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.stream.Stream;

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
        return User.withUsername(String.valueOf(user.getId()))
                .password(user.getPasswordHash())
                .authorities(authorities(user))
                .build();
    }

    // The role as ROLE_<name> (for display and hasRole), and each permission by its name (what the checks use).
    // A user without a role (from before slice 31, not migrated yet) gets no authorities and can do nothing.
    static String[] authorities(AppUser user) {
        if (user.getRole() == null) {
            return new String[0];
        }
        Stream<String> role = Stream.of("ROLE_" + user.getRole().getName());
        Stream<String> permissions = user.getRole().getPermissions().stream().map(p -> p.getName().name());
        return Stream.concat(role, permissions).toArray(String[]::new);
    }
}
