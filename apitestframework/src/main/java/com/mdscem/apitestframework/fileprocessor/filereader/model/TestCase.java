package com.mdscem.apitestframework.fileprocessor.filereader.model;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class TestCase {
    private String testCaseId;
    private String baseUri;
    private Map<String, String> auth;
    private Given given;
    private When when;
    private Then then;
    private Map<String, String> body;
    private Map<String, String> dataCapture;

    private String logLevel;
    private int delay;
    private String next;

}
