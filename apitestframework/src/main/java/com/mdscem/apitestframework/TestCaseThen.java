package com.mdscem.apitestframework;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TestCaseThen {
    private int statusCode;
    private TestCaseHeaders headers;
    private TestCaseCookie cookie;
}
