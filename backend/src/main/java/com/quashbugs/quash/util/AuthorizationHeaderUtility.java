package com.quashbugs.quash.util;

import org.springframework.stereotype.Component;

@Component
public class AuthorizationHeaderUtility {

    public String extractTokenFromHeader(String authorizationHeader) {
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            return authorizationHeader.substring(7);
        }
        return null;
    }
}
