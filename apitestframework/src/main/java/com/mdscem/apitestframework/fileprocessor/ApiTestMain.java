package com.mdscem.apitestframework.fileprocessor;

import com.mdscem.apitestframework.fileprocessor.filereader.FileConfigLoader;
import com.mdscem.apitestframework.fileprocessor.validator.PlaceholderReplacer;
import org.apache.log4j.BasicConfigurator;

public class ApiTestMain {

    public static void main(String[] args) throws Exception {
        // Initialize basic log4j configuration
        BasicConfigurator.configure();

        // Read the test cases file content
        String content = FileConfigLoader.readFile(FileConfigLoader.loadTestCasesFiles());
        System.out.println("Original content:");
        System.out.println(content);

        // Define the path to the values.yaml file
        String valuePath = "/home/kmedagoda/Documents/Kavinda Final/final TestFramework/TestFramework/apitestframework/src/main/resources/values.yaml";

        // Initialize the PlaceholderReplacer
        PlaceholderReplacer placeholderReplacer = new PlaceholderReplacer();

        // Perform placeholder replacement
        String modifiedContent = placeholderReplacer.replacePlaceholders(content, valuePath);

        // Output the modified content
        System.out.println("Modified content after placeholder replacement:");
        System.out.println(modifiedContent);
    }
}
