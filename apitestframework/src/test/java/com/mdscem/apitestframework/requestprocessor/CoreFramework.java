package com.mdscem.apitestframework.requestprocessor;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.mdscem.apitestframework.fileprocessor.filereader.model.TestCase;

public interface CoreFramework {
    public String createFrameworkTypeTestFileAndexecute(TestCase testCase) throws JsonProcessingException;
}
