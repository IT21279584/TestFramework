package com.mdscem.apitestframework.fileprocessor.filereader.model;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
@Data
public class Response {
    private int statusCode;
    private Map<String, String> headers;
    private Map<String, String> cookie;
    private Map<String, String> body;
    private String log;
}
