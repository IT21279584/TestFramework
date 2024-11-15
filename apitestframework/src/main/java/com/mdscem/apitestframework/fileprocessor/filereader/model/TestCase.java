package com.mdscem.apitestframework.fileprocessor.filereader.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
@Data@JsonIgnoreProperties(ignoreUnknown = true) // Ignore unrecognized fields
public class TestCase {
    private String testCaseName;
    private String baseUri;
    private Map<String, String> auth;
    private Request request;
    private Response response;
    private Map<String, Object> capture;
    private int delay;

}
