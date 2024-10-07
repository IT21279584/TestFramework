package com.mdscem.apitestframework.fileprocessor.filereader;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class TestCaseGiven {
    private Map<String, String> headers;
    private Map<String, String> pathParam;

}
