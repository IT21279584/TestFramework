package com.mdscem.apitestframework.fileprocessor.filereader;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static com.mdscem.apitestframework.constants.Constant.MULTIPLE_FILE_PATH;

public class FileConfigLoader {
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final Logger logger = Logger.getLogger(FileConfigLoader.class);

    JsonNode config;

    public FileConfigLoader(String configFilePath) {
        try {
            String data = new String(Files.readAllBytes(Paths.get(configFilePath)));
            ObjectMapper objectMapper = new ObjectMapper();
            config = objectMapper.readTree(data);
        } catch (IOException e) {
            e.printStackTrace();

            throw new RuntimeException("Failed to load configuration file: " + configFilePath);
        }
    }

    public static List<String> loadTestCasesFiles() throws Exception {

        FileConfigLoader configLoader = new FileConfigLoader(MULTIPLE_FILE_PATH);

        JsonNode testCaseFilesNode = configLoader.config;
        List<String> testCaseFiles = new ArrayList<>();

        if (testCaseFilesNode != null && testCaseFilesNode.isArray()) {
            Iterator<JsonNode> elements = testCaseFilesNode.elements();
            while (elements.hasNext()) {
                testCaseFiles.add(elements.next().asText());
            }
        } else {
            throw new RuntimeException("Configuration root is not an array. Please provide an array of test case file paths.");
        }

        return testCaseFiles;
    }


    public static JsonNode readFile(List<String> testCaseFiles) {
        StringBuilder allTestCases = new StringBuilder();

        try {
            for (String testCaseFile : testCaseFiles) {
                TestCaseLoader testCaseLoader = new TestCaseLoader(testCaseFile);
                String testCases = testCaseLoader.loadTestCases();
                if (testCases != null) {
                    allTestCases.append(testCases).append(System.lineSeparator());
                }
            }

            // Convert the accumulated string into a JsonNode
            return objectMapper.readTree(allTestCases.toString());
        } catch (Exception e) {
            e.printStackTrace();
            return null; // or handle error accordingly
        }
    }


}