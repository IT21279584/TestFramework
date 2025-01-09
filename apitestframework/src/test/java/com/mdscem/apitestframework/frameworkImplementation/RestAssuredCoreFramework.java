package com.mdscem.apitestframework.frameworkImplementation;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mdscem.apitestframework.requestprocessor.validation.AssertJValidation;
import com.mdscem.apitestframework.fileprocessor.filereader.model.TestCase;
import com.mdscem.apitestframework.fileprocessor.filereader.model.Request;
import com.mdscem.apitestframework.requestprocessor.CaptureContext;
import com.mdscem.apitestframework.requestprocessor.CaptureReplacer;
import com.mdscem.apitestframework.requestprocessor.CaptureValidation;
import com.mdscem.apitestframework.requestprocessor.CoreFramework;

import com.mdscem.apitestframework.requestprocessor.authhandling.AuthenticationHandler;
import com.mdscem.apitestframework.requestprocessor.authhandling.AuthenticationHandlerFactory;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class RestAssuredCoreFramework implements CoreFramework {

    private List<TestCase> testcaseList;
    private ObjectMapper objectMapper = new ObjectMapper();
    @Autowired
    private CaptureContext captureContext;
    @Autowired
    private CaptureValidation captureValidation;
    @Autowired
    private CaptureReplacer captureReplacer;

    public void testcaseInitializer(List<TestCase> testcaseList) throws JsonProcessingException {
        this.testcaseList = testcaseList;
        captureContext.getCaptureMap();

        for (TestCase testCase : testcaseList) {
            // Validate captured data or prerequisites if needed
            captureValidation.processCaptures(testCase);
            TestCase replcaedTestCase = captureReplacer.replaceParameterPlaceholders(testCase);
            // Authenticate if necessary
            String result = createFrameworkTypeTestFileAndexecute(replcaedTestCase);


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

        System.out.println("My Response : " + response.prettyPrint());

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
                // Replace placeholders in header values
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
            // Replace placeholders in the body
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

    private void validateResponse(TestCase testCase, Response response) throws JsonProcessingException {

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
        captureReplacer.updateCapturesFromResponse(res);
        System.out.println("My after captureMap " + captureContext.getCaptureMap());
    }

    // Helper method to validate the response body
    private void validateResponseBody(TestCase testCase, Response response) throws JsonProcessingException {
        try {
            // Convert expected response body to JSON string
            String expectedBody = objectMapper.writeValueAsString(testCase.getResponse().getBody());

            // Parse both expected and actual JSON bodies
            JsonNode expectedJsonNode = objectMapper.readTree(expectedBody);
            JsonNode actualJsonNode = objectMapper.readTree(response.getBody().asString());

            // Remove unwanted fields from both JSON objects
            removeUnwantedFields((ObjectNode) expectedJsonNode);
            removeUnwantedFields((ObjectNode) actualJsonNode);


            expectedJsonNode.fields().forEachRemaining(entry -> {
                String fieldName = entry.getKey();
                JsonNode expectedValue = entry.getValue();

                // Handle `assertJ` keyword for dynamic validation
                if (expectedValue.isTextual() && expectedValue.asText().startsWith("{{assertJ")) {
                    // Extract the method chain for AssertJ
                    String assertJExpression = expectedValue.asText();
                    String methodChain = assertJExpression.substring(assertJExpression.indexOf("assertJ") + 7, assertJExpression.lastIndexOf("}")).trim();

                    // Prepare the object to assert
                    JsonNode actualFieldValueNode = actualJsonNode.get(fieldName);
                    try {
                        if (actualFieldValueNode.isInt()) {
                            Integer actualValue = actualFieldValueNode.asInt();
                            AssertJValidation.executeAssertions(Assertions.assertThat(actualValue), methodChain.split("\\."));
                        } else if (actualFieldValueNode.isLong()) {
                            Long actualValue = actualFieldValueNode.asLong();
                            AssertJValidation.executeAssertions(Assertions.assertThat(actualValue), methodChain.split("\\."));
                        } else if (actualFieldValueNode.isTextual()) {
                            String actualValue = actualFieldValueNode.asText();
                            AssertJValidation.executeAssertions(Assertions.assertThat(actualValue), methodChain.split("\\."));
                        } else if (actualFieldValueNode.isBoolean()) {
                            Boolean actualValue = actualFieldValueNode.asBoolean();
                            AssertJValidation.executeAssertions(Assertions.assertThat(actualValue), methodChain.split("\\."));
                        } else if (actualFieldValueNode.isDouble()) {
                            Double actualValue = actualFieldValueNode.asDouble();
                            AssertJValidation.executeAssertions(Assertions.assertThat(actualValue), methodChain.split("\\."));
                        } else if (actualFieldValueNode.isFloat()) {
                            Float actualValue = actualFieldValueNode.floatValue();
                            AssertJValidation.executeAssertions(Assertions.assertThat(actualValue), methodChain.split("\\."));
                        } else if (actualFieldValueNode.isArray()) {
                            // Handle arrays as JsonNode arrays
                            List<JsonNode> actualValue = new ArrayList<>();
                            actualFieldValueNode.forEach(actualValue::add);
                            AssertJValidation.executeAssertions(Assertions.assertThat(actualValue), methodChain.split("\\."));
                        } else if (actualFieldValueNode.isShort()) {
                            Short actualValue = (short) actualFieldValueNode.asInt(); // Casting Int to Short
                            AssertJValidation.executeAssertions(Assertions.assertThat(actualValue), methodChain.split("\\."));
                        } else {
                            throw new IllegalArgumentException("Unsupported type for field: " + fieldName);
                        }
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }

                } else {
                    JsonNode actualValue = actualJsonNode.get(fieldName);
                    Assert.assertEquals(expectedValue, actualValue);
                }
            });

        } catch (Exception e) {
            throw e;
        }
    }

    // Helper method to remove unwanted fields from JSON
    private void removeUnwantedFields(ObjectNode jsonNode) {
        jsonNode.remove("createdAt");
        jsonNode.remove("updatedAt");
        jsonNode.remove("token");
    }
}