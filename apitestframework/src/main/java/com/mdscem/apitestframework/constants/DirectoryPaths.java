package com.mdscem.apitestframework.constants;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class DirectoryPaths {

    private static String basePath;

    public static String TEST_CASES_DIRECTORY;
    public static String INCLUDES_DIRECTORY;
    public static String FLOWS_DIRECTORY;
    public static String VALUE_FILE_PATH;

    // Inject base path from application.properties
    @Value("${base.directory.path}")
    public void setBasePath(String basePath) {
        DirectoryPaths.basePath = basePath;
    }

    @PostConstruct
    public void initPaths() {
        TEST_CASES_DIRECTORY = basePath + "testcases/";
        INCLUDES_DIRECTORY = basePath + "includes/";
        FLOWS_DIRECTORY = basePath + "flows/";
        VALUE_FILE_PATH = basePath + "includes/values.yaml";
    }
}
