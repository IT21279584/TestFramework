package com.mdscem.apitestframework.requestprocessor.authhandling;

import io.restassured.specification.RequestSpecification;

import java.util.Map;

import static com.mdscem.apitestframework.constants.Constant.PASSWORD;
import static com.mdscem.apitestframework.constants.Constant.USERNAME;

public class BasicAuthenticationHandler implements AuthenticationHandler {
    @Override
    public void applyAuthentication(RequestSpecification requestSpec, Map<String, String> authData) {
        String username = authData.get(USERNAME);
        String password = authData.get(PASSWORD);
        if (username != null && password != null) {
            requestSpec.auth().preemptive().basic(username, password);
        }
    }
}
