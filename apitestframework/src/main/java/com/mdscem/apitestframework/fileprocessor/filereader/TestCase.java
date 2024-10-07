package com.mdscem.apitestframework.fileprocessor.filereader;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class TestCase {
    private String testCaseId;
    private String baseUri;
    private AuthInfo auth;
    private TestCaseGiven given;
    private TestCaseWhen when;
    private TestCaseThen then;
    private Map<String, String> body;
    private DataCapture datacapture;
    private boolean log;
    private String logLevel;
    private int delay;
    private String next;

}
