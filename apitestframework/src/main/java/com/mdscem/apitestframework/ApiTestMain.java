package com.mdscem.apitestframework;

import com.mdscem.apitestframework.fileprocessor.TestCaseProcessor;
import com.mdscem.apitestframework.fileprocessor.filereader.TestCasesToJsonNodeReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

import java.io.IOException;
import java.util.List;

import static com.mdscem.apitestframework.constants.Constant.TEST_CASES_DIRECTORY;
import static com.mdscem.apitestframework.constants.Constant.VALUE_FILE_PATH;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class ApiTestMain implements CommandLineRunner {

    @Autowired
    private TestCaseProcessor testCaseProcessor;

    @Autowired
    private TestCasesToJsonNodeReader testCasesToJsonNodeReader;

    @Value("${value.directory.path}")
    private String valueDirectory;

    @Override
    public void run(String... args) {
        try {
            // Load test case file paths from fileconfig.json
            List<String> testCaseFilePaths = testCasesToJsonNodeReader.loadTestCaseFilePathsFromDirectory(TEST_CASES_DIRECTORY);

            // Process each test case file individually
            for (String filePath : testCaseFilePaths) {
                testCaseProcessor.processTestCases(filePath, valueDirectory);
            }

        } catch (IOException e) {
            System.err.println("Error occurred while loading files: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("Unexpected error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
