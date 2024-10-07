package com.mdscem.apitestframework;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TestCase {
    private String testCaseId;
    private String baseUri;
    private AuthInfo auth;
    private TestCaseGiven given;
    private TestCaseWhen when;
    private TestCaseThen then;
    private Body body;
    private DataCapture datacapture;
    private boolean log;
    private String logLevel;
    private int delay;
    private String next;

}
