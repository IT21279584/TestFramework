package com.mdscem.apitestframework.fileprocessor.filereader.model;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class Given {
    private Map<String, String> headers;
    private Map<String, String> pathParam;
    private boolean log;

}
