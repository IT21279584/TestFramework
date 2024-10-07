package com.mdscem.apitestframework.fileprocessor.fileinterpreter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mdscem.apitestframework.fileprocessor.filereader.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class FileInterpreter {

    public static List<TestCase> interpret(JsonNode validatedJson) {
        List<TestCase> testCases = new ArrayList<>();

        // Check if the validated JSON is an array of test cases
        if (validatedJson.isArray()) {
            for (JsonNode testNode : validatedJson) {
                TestCase testCase = new TestCase();
                ObjectMapper mapper = new ObjectMapper();

                try {
                    // Map basic fields
                    testCase.setTestCaseId(testNode.path("testCaseId").asText());
                    testCase.setBaseUri(testNode.path("baseUri").asText());

                    // Set auth details
                    AuthInfo authInfo = mapper.treeToValue(testNode.path("auth"), AuthInfo.class);
                    testCase.setAuth(authInfo);

                    // Set 'given' section
                    TestCaseGiven given = new TestCaseGiven();

                    // Set headers in 'given'
                    Map<String, String> headers = new HashMap<>();
                    JsonNode headersNode = testNode.path("given").path("headers");
                    if (headersNode.isObject()) {
                        Iterator<Map.Entry<String, JsonNode>> fields = headersNode.fields();
                        while (fields.hasNext()) {
                            Map.Entry<String, JsonNode> field = fields.next();
                            headers.put(field.getKey(), field.getValue().asText());
                        }
                    }
                    given.setHeaders(headers);

                    // Set path parameters in 'given'
                    Map<String, String> pathParams = new HashMap<>();
                    JsonNode pathParamsNode = testNode.path("given").path("pathParam");
                    if (pathParamsNode.isObject()) {
                        Iterator<Map.Entry<String, JsonNode>> fields = pathParamsNode.fields();
                        while (fields.hasNext()) {
                            Map.Entry<String, JsonNode> field = fields.next();
                            pathParams.put(field.getKey(), field.getValue().asText());
                        }
                    }
                    given.setPathParam(pathParams);

                    testCase.setGiven(given);

                    // Set 'when' section
                    TestCaseWhen when = new TestCaseWhen();
                    when.setMethod(testNode.path("when").path("method").asText());
                    when.setUri(testNode.path("when").path("path").asText());
                    testCase.setWhen(when);

                    // Set 'then' section
                    TestCaseThen then = new TestCaseThen();
                    then.setStatusCode(testNode.path("then").path("statusCode").asInt());

                    // Set headers in 'then'
                    Map<String, String> thenHeaders = new HashMap<>();
                    JsonNode thenHeadersNode = testNode.path("then").path("headers");
                    if (thenHeadersNode.isObject()) {
                        Iterator<Map.Entry<String, JsonNode>> fields = thenHeadersNode.fields();
                        while (fields.hasNext()) {
                            Map.Entry<String, JsonNode> field = fields.next();
                            thenHeaders.put(field.getKey(), field.getValue().asText());
                        }
                    }
                    then.setHeaders(thenHeaders);

                    // Set cookies in 'then'
                    Map<String, String> cookies = new HashMap<>();
                    JsonNode cookiesNode = testNode.path("then").path("cookie");
                    if (cookiesNode.isObject()) {
                        Iterator<Map.Entry<String, JsonNode>> fields = cookiesNode.fields();
                        while (fields.hasNext()) {
                            Map.Entry<String, JsonNode> field = fields.next();
                            cookies.put(field.getKey(), field.getValue().asText());
                        }
                    }
                    then.setCookie(cookies);


                    // Set data capture
                    DataCapture dataCapture = new DataCapture();
                    dataCapture.setVarDeptId(testNode.path("datacapture").path("var_dept_id").asText());
                    testCase.setDatacapture(dataCapture);

                    // Set delay and next action
                    testCase.setDelay(testNode.path("delay").asInt());
                    testCase.setNext(testNode.path("next").asText());

                    // Action Execution: Add the test case to the list
                    testCases.add(testCase);

                } catch (Exception e) {
                    System.err.println("Error while interpreting test case: " + e.getMessage());
                }
            }
        } else {
            System.err.println("The validated JSON is not an array of test cases.");
        }

        return testCases;
    }
}
