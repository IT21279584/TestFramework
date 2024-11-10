package com.mdscem.apitestframework.fileprocessor.filereader.model;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
@Data
public class Request {
    private String method;
    private String path;
    private Map<String, String> headers;
    private Map<String, String> pathParam;
    private Map<String, String> queryParam;
    private Map<String, String> body;
    private String log;
}
