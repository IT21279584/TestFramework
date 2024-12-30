package com.mdscem.apitestframework.frameworkImplementation;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
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

import java.time.LocalDate;
import java.util.regex.Matcher;

import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

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

            // Use JsonAssert for validation
//            org.skyscreamer.jsonassert.JSONAssert.assertEquals(
//                    expectedJsonNode.toString(),
//                    actualJsonNode.toString(),
//                    false // Set to true for strict validation
//            );

            // Iterate over expected fields and validate
            expectedJsonNode.fields().forEachRemaining(entry -> {

                String fieldName = entry.getKey();
                JsonNode expectedValue = entry.getValue();

                // Handle `assertJ` keyword for dynamic validation
                if (expectedValue.isTextual() && expectedValue.asText().startsWith("{{assertJ")) {
                    validateWithAssertJ(fieldName, expectedValue.asText(), actualJsonNode);
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

    // Helper method to validate using assertJ keyword
    private void validateWithAssertJ(String fieldName, String assertJExpression, JsonNode actualJsonNode) {
        try {
            // Define patterns for extracting range and type
            final String RANGE_PATTERN = "range\\((\\d+),\\s*(\\d+)\\)";
            final String TYPE_PATTERN = "type\\((\\w+)\\)";
            final String ALLOW_NULL_PATTERN = "allowNull\\((true|false)\\)";

            // Variables to hold extracted values
            Integer minRange = null;
            Integer maxRange = null;
            String type = null;

            Matcher rangeMatcher = Pattern.compile(RANGE_PATTERN).matcher(assertJExpression);
            Matcher typeMatcher = Pattern.compile(TYPE_PATTERN).matcher(assertJExpression);
            Matcher allowNullMatcher = Pattern.compile(ALLOW_NULL_PATTERN).matcher(assertJExpression);

            if (allowNullMatcher.find()) {
                boolean allowNull = false;
                allowNull = Boolean.parseBoolean(allowNullMatcher.group(1));
                validateValue(allowNull, fieldName);
            }


            // Extract range values from the expression
            if (rangeMatcher.find()) {
                minRange = Integer.parseInt(rangeMatcher.group(1));
                maxRange = Integer.parseInt(rangeMatcher.group(2));
            }

            // Extract type information from the expression
            if (typeMatcher.find()) {
                type = typeMatcher.group(1);
            }

            // Get the actual value of the field from the JSON node
            JsonNode actualValueNode = actualJsonNode.get(fieldName);
            if (actualValueNode == null) {
                throw new IllegalArgumentException("Expected field '" + fieldName + "' is missing in the response");
            }

            // Validate based on the specified type
            validateType( fieldName,  type,  actualValueNode);
            // Validate the range
            if (minRange != null && maxRange != null) {
                validateRange(fieldName,minRange, maxRange, actualValueNode);
            }

        } catch (Exception e) {
            throw new RuntimeException("Error parsing assertJ expression: " + assertJExpression + " for field: " + fieldName + ". " + e.getMessage(), e);
        }
    }

    private void validateType(String fieldName, String type, JsonNode actualValueNode) {
        if(type != null){
            switch (type.toLowerCase()) {
                case "int":
                    assertThat(actualValueNode.isInt())
                            .as("Field '%s' should be of type int", fieldName)
                            .isTrue();
                    break;
                case "string":
                    assertThat(actualValueNode.isTextual())
                            .as("Field '%s' should be of type string", fieldName)
                            .isTrue();
                    break;
                case "boolean":
                    assertThat(actualValueNode.isBoolean())
                            .as("Field '%s' should be of type boolean", fieldName)
                            .isTrue();
                    break;
                case "double":
                    assertThat(actualValueNode.isDouble())
                            .as("Field '%s' should be of type double", fieldName)
                            .isTrue();
                    break;
                case "float":
                    assertThat(actualValueNode.isFloat())
                            .as("Field '%s' should be of type float", fieldName)
                            .isTrue();
                    break;
                case "date":
                    try {
                        assertThat(actualValueNode.isTextual())
                                .as("Field '%s' should be a valid date string", fieldName)
                                .isTrue();
                        assertThat(LocalDate.parse(actualValueNode.asText()))
                                .as("Field '%s' is not a valid date", fieldName);
                    } catch (Exception e) {
                        throw new IllegalArgumentException("Field '" + fieldName + "' is not a valid date");
                    }
                    break;
                case "array":
                    assertThat(actualValueNode.isArray())
                            .as("Field '%s' should be of type array", fieldName)
                            .isTrue();
                    break;
                default:
                    throw new IllegalArgumentException("Unsupported type: " + type + " in AssertJ validation");
            }
        }
    }

    private void validateRange(String fieldName, int minRange, int maxRange, JsonNode actualValueNode) {
        // Validate range if specified
            if (!actualValueNode.isInt()) {
                throw new IllegalArgumentException("Field '" + fieldName + "' is not of type int for range validation");
            }
            int actualValue = actualValueNode.asInt();
            assertThat(actualValue)
                    .as("Validation for field: " + fieldName)
                    .isBetween(minRange, maxRange);

    }

    private  void validateValue(boolean allowNull, String fieldName){
            if (allowNull) {
                return; // Log a warning if necessary
            } else {
                throw new IllegalArgumentException("Expected field '" + fieldName + "' is missing in the response");
            }


    }
}