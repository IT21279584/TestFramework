package com.mdscem.apitestframework.frameworkImplementation;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mdscem.apitestframework.fileprocessor.filereader.model.TestCase;
import com.mdscem.apitestframework.fileprocessor.filereader.model.Request;
import com.mdscem.apitestframework.requestprocessor.CaptureReplacer;
import com.mdscem.apitestframework.requestprocessor.CaptureValidation;
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
    private ObjectMapper objectMapper = new ObjectMapper();

    private CaptureValidation captureValidation = new CaptureValidation();

    private CaptureReplacer captureReplacer = new CaptureReplacer();

    @Test
    public void testcaseInitializer(ArrayList<TestCase> testcaseList) throws JsonProcessingException {
        this.testcaseList = testcaseList;

        for (TestCase testCase : testcaseList) {
            // Validate captured data or prerequisites if needed
            // Authenticate if necessary
            String result = createFrameworkTypeTestFileAndexecute(testCase);
            System.out.println("Test Case Execution Result: " + result);
            // Store captured data if necessary
            captureValidation.processCaptures(testCase);

        }
    }

    @Override
    public String createFrameworkTypeTestFileAndexecute(TestCase testCase) throws JsonProcessingException {
        RequestSpecification requestSpec = buildRequestSpecification(testCase);

        System.out.println("My URL " + testCase.getBaseUri() + testCase.getRequest().getPath());
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

        // Add request body
        if (request.getBody() != null && !request.getBody().isEmpty()) {
            requestSpec.body(request.getBody());
        }

        // Log request if specified
        if ("ALL".equalsIgnoreCase(request.getLog())) {
            requestSpec.log().all();
        }

//        captureValidation.processCaptures(testCase);

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

    private void validateResponse(TestCase testCase, Response response) throws JsonProcessingException {
        // Log the response details
//        logResponseDetails(response);

        // Validate the response status code
        Assert.assertEquals(
                "Status code mismatch",
                testCase.getResponse().getStatusCode(),
                response.getStatusCode()
        );

        // Validate response body using JsonAssert if expected body is provided
        if (testCase.getResponse().getBody() != null) {
            validateResponseBody(testCase, response);
        }

        String res = response.asString();
        // Capture data if specified
        captureValidation.processCaptures(testCase);
        captureReplacer.updateCapturesFromResponse(res);
    }

    // Helper method to log response details
    private void logResponseDetails(Response response) {
        System.out.println("Response Status Code: " + response.getStatusCode());
        System.out.println("Response Headers: " + response.getHeaders());
        System.out.println("Response Body: " + response.getBody().asString());
    }

    // Helper method to validate the response body
    private void validateResponseBody(TestCase testCase, Response response) {
        try {
            // Convert expected response body to JSON string
            String expectedBody = objectMapper.writeValueAsString(testCase.getResponse().getBody());

            // Parse both expected and actual JSON bodies
            JsonNode expectedJsonNode = objectMapper.readTree(expectedBody);
            JsonNode actualJsonNode = objectMapper.readTree(response.getBody().asString());

            // Remove unwanted fields from both JSON objects
            removeUnwantedFields((ObjectNode) expectedJsonNode);
            removeUnwantedFields((ObjectNode) actualJsonNode);

            // Use JsonAssert for validation
            org.skyscreamer.jsonassert.JSONAssert.assertEquals(
                    expectedJsonNode.toString(),
                    actualJsonNode.toString(),
                    false // Set to true for strict validation
            );
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail("Error validating JSON response: " + e.getMessage());
        }
    }

    // Helper method to capture data
    private void captureData(TestCase testCase, Response response) {
        if (testCase.getCapture() != null) {
            for (Map.Entry<String, Object> entry : testCase.getCapture().entrySet()) {
                String key = entry.getKey();
                Object value = response.jsonPath().get(entry.getValue().toString());
                testCase.getCapture().put(key, value);
            }
        }
    }

    // Helper method to remove unwanted fields from JSON
    private void removeUnwantedFields(ObjectNode jsonNode) {
        jsonNode.remove("createdAt");
        jsonNode.remove("updatedAt");
    }
}