package com.mdscem.apitestframework.fileprocessor.filereader.model;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class Then {
    private int statusCode;
    private Map<String, String> headers;
    private Map<String, String> cookie;
}
