package com.mdscem.apitestframework.requestprocessor.authhandling;

import static com.mdscem.apitestframework.constants.Constant.BASIC;
import static com.mdscem.apitestframework.constants.Constant.TOKEN;

public class AuthenticationHandlerFactory {
    public static AuthenticationHandler getAuthenticationHandler(String type) {
        if (TOKEN.equalsIgnoreCase(type)) {
            return new TokenAuthenticationHandler();
        } else if (BASIC.equalsIgnoreCase(type)) {
            return new BasicAuthenticationHandler();
        }
        throw new UnsupportedOperationException("Unsupported authentication type: " + type);
    }
}
