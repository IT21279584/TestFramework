package com.mdscem.apitestframework.requestprocessor.authhandling;

import io.restassured.specification.RequestSpecification;

import java.util.Map;

public interface AuthenticationHandler {

    void applyAuthentication(RequestSpecification requestSpec, Map<String, String> authData);
}
