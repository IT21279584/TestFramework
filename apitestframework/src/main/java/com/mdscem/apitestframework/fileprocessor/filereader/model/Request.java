package com.mdscem.apitestframework.fileprocessor.filereader.model;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

public class Request {
    private String method;
    private String path;
    private Map<String, String> headers;

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public Map<String, String> getPathParam() {
        return pathParam;
    }

    public void setPathParam(Map<String, String> pathParam) {
        this.pathParam = pathParam;
    }

    public Map<String, String> getQueryParam() {
        return queryParam;
    }

    public void setQueryParam(Map<String, String> queryParam) {
        this.queryParam = queryParam;
    }

    public Map<String, Object> getBody() {
        return body;
    }

    public void setBody(Map<String, Object> body) {
        this.body = body;
    }

    public String getLog() {
        return log;
    }

    public void setLog(String log) {
        this.log = log;
    }

    private Map<String, String> pathParam;
    private Map<String, String> queryParam;
    private Map<String, Object> body;
    private String log;
}
