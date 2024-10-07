package com.mdscem.apitestframework.fileprocessor.filereader;

import com.fasterxml.jackson.databind.JsonNode;

public interface IFileReader {
    JsonNode readTestCases(String content);
}
