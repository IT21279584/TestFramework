package com.mdscem.apitestframework.constants;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class DirectoryPaths {

    private static final Logger logger = LoggerFactory.getLogger(DirectoryPaths.class);

    @Autowired
    private ResourceLoader resourceLoader;
    @Autowired
    private Environment environment;

    public static String TEST_CASES_DIRECTORY;
    public static String INCLUDES_DIRECTORY;
    public static String FLOWS_DIRECTORY;
    public static String VALIDATION_FILE_PATH;
    public static String FLOW_VALIDATION_PATH;
    public static String CORE_FRAMEWORK_PATH;
    public static String REPORT_DIRECTORY;

    @PostConstruct
    public void initPaths() {
        String BASE_PATH = loadPath("base.directory.path", "./resources/");
        String REPORT_PATH = loadPath("report.directory.path", "./reports/");

        TEST_CASES_DIRECTORY = BASE_PATH + "testcases/";
        INCLUDES_DIRECTORY = BASE_PATH + "includes/";
        FLOWS_DIRECTORY = BASE_PATH + "flows/";
        VALIDATION_FILE_PATH = BASE_PATH + "schema.json";
        FLOW_VALIDATION_PATH = BASE_PATH + "flow-validation.json";
        CORE_FRAMEWORK_PATH = BASE_PATH + "framework-config.json";
        REPORT_DIRECTORY = REPORT_PATH;

        logger.info("Directory paths initialized successfully.");
    }

    private String loadPath(String propertyName, String defaultPath) {
        String pathFromEnv = environment.getProperty(propertyName);
        if (pathFromEnv != null) {
            logger.info("Loaded '{}' from environment: {}", propertyName, pathFromEnv);
            return pathFromEnv;
        }

        Properties properties = new Properties();
        try {
            Resource resource = resourceLoader.getResource("classpath:application.properties");
            properties.load(resource.getInputStream());
            String pathFromFile = properties.getProperty(propertyName);
            if (pathFromFile != null) {
                logger.info("Loaded '{}' from properties file: {}", propertyName, pathFromFile);
                return pathFromFile;
            }
        } catch (IOException e) {
            logger.warn("Failed to load '{}' from properties file. Falling back to default.", propertyName, e);
        }

        logger.warn("Using default '{}' path: {}", propertyName, defaultPath);
        return defaultPath;
    }
}
