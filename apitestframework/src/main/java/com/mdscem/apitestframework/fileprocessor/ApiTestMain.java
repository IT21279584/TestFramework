package com.mdscem.apitestframework.fileprocessor;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.mdscem.apitestframework.fileprocessor.filereader.TestCasesToJsonNodeReader;
import com.mdscem.apitestframework.fileprocessor.validator.TestCaseReplacer;
import com.mdscem.apitestframework.fileprocessor.validator_old.PlaceholderReplacer;
import org.apache.log4j.BasicConfigurator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

import java.io.IOException;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class ApiTestMain implements CommandLineRunner {

    @Autowired
    TestCaseReplacer testCaseReplacer;

    @Autowired
    TestCasesToJsonNodeReader testCasesToJsonNodeReader;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public static void main(String[] args) throws Exception {
        SpringApplication.run(ApiTestMain.class);
    }

    @Override
    public void run(String... args) throws Exception {
        // Initialize basic log4j configuration
        BasicConfigurator.configure();

        try {
            // Load test case and values files
            JsonNode testCaseNode = testCasesToJsonNodeReader.loadFileAsJsonNode("/home/kmedagoda/Documents/Kavinda Final/final TestFramework/JsonNode test/TestFramework/apitestframework/src/main/resources/testcases1.json");
            JsonNode valuesNode = testCasesToJsonNodeReader.loadFileAsJsonNode("/home/kmedagoda/Documents/Kavinda Final/final TestFramework/JsonNode test/TestFramework/apitestframework/src/main/resources/values.yaml");

            System.out.println("================ values  " + valuesNode);
            System.out.println("================ test cases " + testCaseNode);

            JsonNode finalResult = testCaseReplacer.replacePlaceholdersInNode(testCaseNode,valuesNode);

            System.out.println("Final Result JSON Node:");
            System.out.println(finalResult.toPrettyString());

        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
