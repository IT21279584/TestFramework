package com.mdscem.apitestframework.requestprocessor.authhandling;

import com.mdscem.apitestframework.constants.Constant;
import org.springframework.stereotype.Component;

@Component
public class AuthenticationHandlerFactory {
    public AuthenticationHandler getAuthenticationHandler(String type) {
        if (Constant.TOKEN.equalsIgnoreCase(type)) {
            return new TokenAuthenticationHandler();
        } else if (Constant.BASIC.equalsIgnoreCase(type)) {
            return new BasicAuthenticationHandler();
        }
        throw new UnsupportedOperationException("Unsupported authentication type: " + type);
    }
}
