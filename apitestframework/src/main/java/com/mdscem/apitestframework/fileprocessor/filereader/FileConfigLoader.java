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

    private static final Logger logger = Logger.getLogger(FileConfigLoader.class);

    private JsonNode config;

    public FileConfigLoader(String configFilePath) {
        try {
            String jsonData = new String(Files.readAllBytes(Paths.get(configFilePath)));
            ObjectMapper objectMapper = new ObjectMapper();
            config = objectMapper.readTree(jsonData);
        } catch (IOException e) {
            e.printStackTrace();
            logger.error("Failed to load configuration file: " + configFilePath);
            throw new RuntimeException("Failed to load configuration file: " + configFilePath);
        }
    }

    public JsonNode getTestCaseFiles() {
        if (config.isArray()) {
            return config;
        } else {
            logger.error("Configuration root is not an array. Please provide an array of test case file paths.");
            throw new RuntimeException("Configuration root is not an array. Please provide an array of test case file paths.");
        }
    }

    public static List<String> loadTestCasesFiles(){

        FileConfigLoader configLoader = new FileConfigLoader(MULTIPLE_FILE_PATH);


        JsonNode testCaseFilesNode = configLoader.getTestCaseFiles();
        List<String> testCaseFiles = new ArrayList<>();
        if (testCaseFilesNode != null && testCaseFilesNode.isArray()) {
            Iterator<JsonNode> elements = testCaseFilesNode.elements();
            while (elements.hasNext()) {
                testCaseFiles.add(elements.next().asText());
            }
        }
        return testCaseFiles;
    }


    public static JsonNode readFile(List<String> testCaseFiles) {

       try{
           for (String testCaseFile : testCaseFiles) {
               TestCaseLoader testCaseLoader = new TestCaseLoader(testCaseFile);
               JsonNode testCases = testCaseLoader.loadTestCases();
               if (testCases != null) {
                   return testCases;
               } else {
                   logger.error("Failed to load test cases from file: " + testCaseFile);
               }
           }
       }catch(Exception e){
            e.printStackTrace();
       }
       return null;
    }
}
