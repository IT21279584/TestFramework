package com.mdscem.apitestframework.fileprocessor.filereader.model;

import com.mdscem.apitestframework.context.Testable;

import java.util.Map;

public class TestCase implements Testable {
    private String testCaseName;
    private String baseUri;
    private Map<String, String> auth;
    private Request request;
    private Response response;
    private Map<String, Object> capture;
    private int delay;

    public String getTestCaseName() {
        return testCaseName;
    }

    public void setTestCaseName(String testCaseName) {
        this.testCaseName = testCaseName;
    }

    public String getBaseUri() {
        return baseUri;
    }

    public void setBaseUri(String baseUri) {
        this.baseUri = baseUri;
    }

    public Map<String, String> getAuth() {
        return auth;
    }

    public void setAuth(Map<String, String> auth) {
        this.auth = auth;
    }

    public Request getRequest() {
        return request;
    }

    public void setRequest(Request request) {
        this.request = request;
    }

    public Response getResponse() {
        return response;
    }

    public void setResponse(Response response) {
        this.response = response;
    }

    public Map<String, Object> getCapture() {
        return capture;
    }

    public void setCapture(Map<String, Object> capture) {
        this.capture = capture;
    }

    public int getDelay() {
        return delay;
    }

    public void setDelay(int delay) {
        this.delay = delay;
    }
}
