package com.mdscem.apitestframework.requestprocessor.authhandling;

public class AuthenticationHandlerFactory {
    public static AuthenticationHandler getAuthenticationHandler(String type) {
        if ("token".equalsIgnoreCase(type)) {
            return new TokenAuthenticationHandler();
        } else if ("Basic".equalsIgnoreCase(type)) {
            return new BasicAuthenticationHandler();
        }
        throw new UnsupportedOperationException("Unsupported authentication type: " + type);
    }
}
