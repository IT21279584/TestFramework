package com.mdscem.apitestframework.fileprocessor.filereader;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mdscem.apitestframework.constants.Constant;
import com.mdscem.apitestframework.constants.DirectoryPaths;
import com.mdscem.apitestframework.fileprocessor.TestCaseProcessor;
import com.mdscem.apitestframework.fileprocessor.filereader.model.TestCase;
import com.mdscem.apitestframework.fileprocessor.validator.SchemaValidation;
import com.mdscem.apitestframework.fileprocessor.validator.TestCaseReplacer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

@Component
public class FlowContentReader {
    private static final Logger logger = LogManager.getLogger(FlowContentReader.class);
    @Autowired
    private TestCasesReader testCasesReader;
    @Autowired
    private SchemaValidation schemaValidation;
    @Autowired
    private TestCaseProcessor testCaseProcessor;
    @Autowired
    private TestCaseReplacer testCaseReplacer;
    @Autowired
    @Qualifier("yamlMapper")
    private ObjectMapper yamlMapper;


    // Get all flow files from the directory
    public List<Path> getFlowFilesFromDirectory(Path flowPath) {
        List<Path> flowFiles = new ArrayList<>();

        if (Files.isDirectory(flowPath)) {
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(flowPath, "*"+ Constant.YAML_EXTENTION)) {
                for (Path file : stream) {
                    flowFiles.add(file);
                }
            } catch (IOException e) {
                logger.error("Error processing directory " + flowPath + ": " + e.getMessage());
            }
        } else {
            logger.error("Invalid path. not a directory.");
        }
        return flowFiles;
    }

    public List<JsonNode> getFlowContentAsJsonNodes(Path flowPath) throws IOException {
        List<JsonNode> flowContentsList = new ArrayList<>();
        JsonNode flowsNode = yamlMapper.readTree(flowPath.toFile());
        JsonNode validateFlowNode = schemaValidation.validateTestcase(flowsNode, DirectoryPaths.FLOW_VALIDATION_PATH);

        for (JsonNode singleFlow : validateFlowNode) {
            flowContentsList.add(singleFlow);
        }
        return flowContentsList;
    }

    //read the testcases, when the testcase map not inside the testcases(flow process)
    public TestCase readTestCase(String testCaseName) throws IOException {

        // Load include files and combine them into one node
        List<JsonNode> includeNodes = testCasesReader.loadFilesFromDirectory();
        JsonNode combinedValuesNode = testCaseProcessor.combineNodes(includeNodes);

        String testCaseFilePath = DirectoryPaths.TEST_CASES_DIRECTORY + "/" + testCaseName + Constant.YAML_EXTENTION;

        //Read the testcase file
        JsonNode testCaseNode = testCasesReader.readFile(testCaseFilePath);

        // Call to method that replaces placeholders
        JsonNode replaceJsonNode = testCaseReplacer.replacePlaceholder(testCaseNode, combinedValuesNode);

        //Validate TestCase against the testcase schema
        JsonNode schemaValidate = schemaValidation.validateTestcase(replaceJsonNode, DirectoryPaths.VALIDATION_FILE_PATH);

        return testCaseProcessor.jsonNodeToTestCase(schemaValidate);
    }

}
