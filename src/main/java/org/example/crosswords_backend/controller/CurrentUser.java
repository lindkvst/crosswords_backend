package org.example.crosswords_backend.controller;

import java.security.Principal;

// Spring Security's name for the logged-in user is the user's id (see AppUserDetailsService), both after
// Basic and after a JWT. Controllers pass the id on, never a name or email from the request body.
final class CurrentUser {

    private CurrentUser() {
    }

    static int id(Principal principal) {
        return Integer.parseInt(principal.getName());
    }
}
