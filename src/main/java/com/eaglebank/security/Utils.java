package com.eaglebank.security;

import com.eaglebank.model.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class Utils {

    public static String getAuthenticatedUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return (auth != null && auth.getPrincipal() instanceof User)
                ? ((User) auth.getPrincipal()).getId()
                : null;
    }

}
