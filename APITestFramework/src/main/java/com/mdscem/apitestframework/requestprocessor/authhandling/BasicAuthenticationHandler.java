package com.mdscem.apitestframework.requestprocessor.authhandling;

import com.mdscem.apitestframework.constants.Constant;
import io.restassured.specification.RequestSpecification;

import java.util.Map;

public class BasicAuthenticationHandler implements AuthenticationHandler {
    @Override
    public void applyAuthentication(RequestSpecification requestSpec, Map<String, String> authData) {
        String username = authData.get(Constant.USERNAME);
        String password = authData.get(Constant.PASSWORD);
        if (username != null && password != null) {
            requestSpec.auth().preemptive().basic(username, password);
        }
    }
}
