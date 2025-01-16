package com.mdscem.apitestframework.frameworkImplementation;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mdscem.apitestframework.requestprocessor.validation.AssertJValidation;
import com.mdscem.apitestframework.fileprocessor.filereader.model.TestCase;
import com.mdscem.apitestframework.fileprocessor.filereader.model.Request;
import com.mdscem.apitestframework.requestprocessor.CoreFramework;

import com.mdscem.apitestframework.requestprocessor.authhandling.AuthenticationHandler;
import com.mdscem.apitestframework.requestprocessor.authhandling.AuthenticationHandlerFactory;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.mdscem.apitestframework.constants.Constant.*;

@Component
public class RestAssuredCoreFramework implements CoreFramework {
    private static final Logger logger = LogManager.getLogger(RestAssuredCoreFramework.class);
    private ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String createFrameworkTypeTestFileAndexecute(TestCase testCase) throws JsonProcessingException {
        RequestSpecification requestSpec = buildRequestSpecification(testCase);
        logger.info("URL " + testCase.getBaseUri() + testCase.getRequest().getPath());
        // Execute the HTTP method
        Response response = executeHttpMethod(
                HttpMethod.valueOf(testCase.getRequest().getMethod().toUpperCase()), // Convert string to HttpMethod
                requestSpec,
                testCase.getBaseUri() + testCase.getRequest().getPath()
        );


        logger.info("Response : " + response.prettyPrint());

        // Validate the response
        validateResponse(testCase, response);
        return response.asString();
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
            String type = testCase.getAuth().get(TYPE);
            AuthenticationHandler authHandler = AuthenticationHandlerFactory.getAuthenticationHandler(type);
            authHandler.applyAuthentication(requestSpec, testCase.getAuth());
        }

        // Add request body
        if (request.getBody() != null && !request.getBody().isEmpty()) {
            // Replace placeholders in the body
            requestSpec.body(request.getBody());
        }

        // Log request if specified
        if (ALL.equalsIgnoreCase(request.getLog())) {
            requestSpec.log().all();
        }

        return requestSpec;
    }


    private Response executeHttpMethod(HttpMethod method, RequestSpecification requestSpec, String url) {
        switch (method) {
            case GET:
                return requestSpec.get(url);
            case POST:
                return requestSpec.post(url);
            case PUT:
                return requestSpec.put(url);
            case DELETE:
                return requestSpec.delete(url);
            case PATCH:
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

        if (testCase.getResponse().getHeaders() != null) {
            for (Map.Entry<String, String> entry : testCase.getResponse().getHeaders().entrySet()) {
                String expectedHeader = entry.getValue();
                String actualHeader = response.getHeader(entry.getKey());
                Assert.assertEquals("Header " + entry.getKey() + " mismatch", expectedHeader, actualHeader);
            }
        }

        // Validate response cookies
        if (testCase.getResponse().getCookie() != null) {
            for (Map.Entry<String, String> entry : testCase.getResponse().getCookie().entrySet()) {
                String expectedCookie = entry.getValue();
                String actualCookie = response.getCookie(entry.getKey());
                Assert.assertEquals("Cookie " + entry.getKey() + " mismatch", expectedCookie, actualCookie);
            }
        }

        // Validate response body using JsonAssert if expected body is provided
        if (testCase.getResponse().getBody() != null) {
            validateResponseBody(testCase, response);
        }

        // Validate logging
        if (ALL.equalsIgnoreCase(testCase.getResponse().getLog())) {
            response.then().log().all();
        }
    }

    // Helper method to validate the response body
    private void validateResponseBody(TestCase testCase, Response response) throws JsonProcessingException {
        try {
            // Convert expected response body to JSON string
            String expectedBody = objectMapper.writeValueAsString(testCase.getResponse().getBody());

            // Parse both expected and actual JSON bodies
            JsonNode expectedJsonNode = objectMapper.readTree(expectedBody);
            JsonNode actualJsonNode = objectMapper.readTree(response.getBody().asString());


            // Validate only the fields mentioned in the TestCase response
            expectedJsonNode.fields().forEachRemaining(entry -> {
                String fieldName = entry.getKey();
                JsonNode expectedValue = entry.getValue();

                // Handle `assertJ` keyword for dynamic validation
                if (expectedValue.isTextual() && expectedValue.asText().startsWith("{{"+CHECK)) {
                    // Extract the method chain for AssertJ
                    String assertJExpression = expectedValue.asText();
                    String methodChain = assertJExpression.substring(assertJExpression.indexOf(CHECK) + 5, assertJExpression.lastIndexOf("}")).trim();

                    // Prepare the object to assert
                    JsonNode actualFieldValueNode = actualJsonNode.get(fieldName);
                    try {
                        validateWithAssertJ(actualFieldValueNode, methodChain);
                    } catch (Exception e) {
                        throw new RuntimeException("Validation failed for field: " + fieldName, e);
                    }
                } else {
                    // Regular field validation
                    JsonNode actualValue = actualJsonNode.get(fieldName);
                    Assert.assertEquals("Field " + fieldName + " does not match.", expectedValue, actualValue);
                }
            });
        } catch (Exception e) {
            throw new RuntimeException("Error during response validation.", e);
        }
    }

    private void validateWithAssertJ(JsonNode actualFieldValueNode, String methodChain) throws Exception {
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
        } else if (actualFieldValueNode.isArray()) {
            List<JsonNode> actualValue = new ArrayList<>();
            actualFieldValueNode.forEach(actualValue::add);
            AssertJValidation.executeAssertions(Assertions.assertThat(actualValue), methodChain.split("\\."));
        } else {
            throw new IllegalArgumentException("Unsupported type for dynamic validation.");
        }
    }
}