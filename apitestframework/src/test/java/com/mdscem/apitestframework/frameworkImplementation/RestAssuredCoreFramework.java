package com.mdscem.apitestframework.frameworkImplementation;

import com.mdscem.apitestframework.fileprocessor.filereader.model.TestCase;
import com.mdscem.apitestframework.fileprocessor.filereader.model.Request;
import com.mdscem.apitestframework.requestprocessor.CoreFramework;

import com.mdscem.apitestframework.requestprocessor.authhandling.AuthenticationHandler;
import com.mdscem.apitestframework.requestprocessor.authhandling.AuthenticationHandlerFactory;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Map;

public class RestAssuredCoreFramework implements CoreFramework {

    private ArrayList<TestCase> testcaseList;

    @Test
    public void testcaseInitializer(ArrayList<TestCase> testcaseList) {
        this.testcaseList = testcaseList;

        for (TestCase testCase : testcaseList) {
            // Validate captured data or prerequisites if needed
            // Authenticate if necessary
            String result = createFrameworkTypeTestFileAndexecute(testCase);
            System.out.println("Test Case Execution Result: " + result);
            // Store captured data if necessary
        }
    }

    @Override
    public String createFrameworkTypeTestFileAndexecute(TestCase testCase) {
        RequestSpecification requestSpec = buildRequestSpecification(testCase);

        // Execute the HTTP method
        Response response = executeHttpMethod(
                testCase.getRequest().getMethod(),
                requestSpec,
                testCase.getBaseUri() + testCase.getRequest().getPath()
        );

        // Validate the response
        validateResponse(testCase, response);

        // Return the response as a string (optional)
        return response.getBody().asString();
    }

    private RequestSpecification buildRequestSpecification(TestCase testCase) {
        Request request = testCase.getRequest();

        RequestSpecification requestSpec = RestAssured.given();

        // Add headers
        if (request.getHeaders() != null && !request.getHeaders().isEmpty()) {
            for (Map.Entry<String, String> header : request.getHeaders().entrySet()) {
                requestSpec.header(header.getKey(), header.getValue());
            }
        }

        // Apply authentication using the handler
        if (testCase.getAuth() != null && !testCase.getAuth().isEmpty()) {
            String type = testCase.getAuth().get("type");
            AuthenticationHandler authHandler = AuthenticationHandlerFactory.getAuthenticationHandler(type);
            authHandler.applyAuthentication(requestSpec, testCase.getAuth());
        }


        // Add path parameters
        if (request.getPathParam() != null && !request.getPathParam().isEmpty()) {
            requestSpec.pathParams(request.getPathParam());
        }

        // Add query parameters
        if (request.getQueryParam() != null && !request.getQueryParam().isEmpty()) {
            requestSpec.queryParams(request.getQueryParam());
        }

        // Add request body
        if (request.getBody() != null && !request.getBody().isEmpty()) {
            requestSpec.body(request.getBody());
        }

        // Log request if specified
        if ("ALL".equalsIgnoreCase(request.getLog())) {
            requestSpec.log().all();
        }

        return requestSpec;
    }

    private Response executeHttpMethod(String method, RequestSpecification requestSpec, String url) {
        switch (method.toUpperCase()) {
            case "GET":
                return requestSpec.get(url);
            case "POST":
                return requestSpec.post(url);
            case "PUT":
                return requestSpec.put(url);
            case "DELETE":
                return requestSpec.delete(url);
            case "PATCH":
                return requestSpec.patch(url);
            default:
                throw new UnsupportedOperationException("Unsupported HTTP method: " + method);
        }
    }

    private void validateResponse(TestCase testCase, Response response) {
        // Validate the response status code
        Assert.assertEquals(
                "Status code mismatch",
                testCase.getResponse().getStatusCode(),
                response.getStatusCode()
        );

        // Validate response body if expected
        if (testCase.getResponse().getBody() != null) {
            Assert.assertEquals(
                    "Response body mismatch",
                    testCase.getResponse().getBody().toString(),
                    response.getBody().asString()
            );
        }

        // Capture data if specified
        if (testCase.getCapture() != null) {
            for (Map.Entry<String, Object> entry : testCase.getCapture().entrySet()) {
                String key = entry.getKey();
                Object value = response.jsonPath().get(entry.getValue().toString());
                testCase.getCapture().put(key, value);
            }
        }
    }
}