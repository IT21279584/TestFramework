package com.mdscem.apitestframework.requestprocessor.authhandling;

import io.restassured.specification.RequestSpecification;

import java.util.Map;

public class BasicAuthenticationHandler implements AuthenticationHandler {
    @Override
    public void applyAuthentication(RequestSpecification requestSpec, Map<String, String> authData) {
        String username = authData.get("username");
        String password = authData.get("password");
        if (username != null && password != null) {
            requestSpec.auth().preemptive().basic(username, password);
        }
    }
}
