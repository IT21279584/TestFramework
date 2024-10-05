package com.mdscem.apitestframework;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class TestCaseGiven {
    private Map headers;
    private TestCasePathParam pathParam;

}
