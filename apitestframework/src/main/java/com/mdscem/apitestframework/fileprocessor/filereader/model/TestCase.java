package com.mdscem.apitestframework.fileprocessor.filereader.model;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
@Data
public class TestCase {
    private String testCaseId;
    private String baseUri;
    private Map<String, String> auth;
    private Request request;
    private Response response;
    private Map<String, String> dataCapture;
    private int delay;
    private String next;

}
