package com.quashbugs.quash.exceptions;

import org.springframework.security.authentication.InsufficientAuthenticationException;

public class TokenExpiredAuthenticationException extends InsufficientAuthenticationException {
    public TokenExpiredAuthenticationException(String msg) {
        super(msg);
    }
}
