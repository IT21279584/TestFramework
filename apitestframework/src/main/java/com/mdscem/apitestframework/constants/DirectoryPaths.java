package com.mdscem.apitestframework.constants;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class DirectoryPaths {

    @Value("${base.directory.path}")
    private  String basePath;

    public static String TEST_CASES_DIRECTORY;
    public static String INCLUDES_DIRECTORY;
    public static String FLOWS_DIRECTORY;
    public static String VALIDATION_FILE_PATH;
    public static String FLOW_VALIDATION_PATH;
    public static String CORE_FRAMEWORK_PATH;


    @PostConstruct
    public void initPaths() {
        TEST_CASES_DIRECTORY = basePath + "testcases/";
        INCLUDES_DIRECTORY = basePath + "includes/";
        FLOWS_DIRECTORY = basePath + "flows/";
        VALIDATION_FILE_PATH = basePath + "schema.json";
        FLOW_VALIDATION_PATH = basePath + "flow.json";
        CORE_FRAMEWORK_PATH = basePath + "framework-config.json";
    }

}
