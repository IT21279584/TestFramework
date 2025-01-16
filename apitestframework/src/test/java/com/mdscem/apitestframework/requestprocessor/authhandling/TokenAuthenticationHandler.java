package com.mdscem.apitestframework.requestprocessor.authhandling;


import io.restassured.specification.RequestSpecification;

import java.util.Map;

import static com.mdscem.apitestframework.constants.Constant.TOKEN;

public class TokenAuthenticationHandler implements AuthenticationHandler {
    @Override
    public void applyAuthentication(RequestSpecification requestSpec, Map<String, String> authData) {
        String token = authData.get(TOKEN);
        if (token != null) {
            requestSpec.auth().oauth2(token);
        }
    }
}
