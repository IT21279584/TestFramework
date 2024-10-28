package com.mdscem.apitestframework.fileprocessor;

import com.mdscem.apitestframework.context.TestCaseParser;
import com.mdscem.apitestframework.context.TestCaseRepository;
import com.mdscem.apitestframework.fileprocessor.filereader.FileConfigLoader;
import com.mdscem.apitestframework.fileprocessor.filereader.model.TestCase;
import com.mdscem.apitestframework.fileprocessor.validator.PlaceholderReplacer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

import java.util.List;

import static com.mdscem.apitestframework.constants.Constant.VALUE_PATH;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class }, scanBasePackages = "com.mdscem.apitestframework")
public class ApiTestMainTest implements CommandLineRunner {

    @Autowired
    private PlaceholderReplacer placeholderReplacer;

    @Autowired
    private TestCaseParser testCaseParser;

    @Autowired
    private TestCaseRepository repository;

    public static void main(String[] args) {
        SpringApplication.run(ApiTestMainTest.class, args);
    }
    @Override
    public void run(String... args) throws Exception {

        // Read the test cases file content
        String content = FileConfigLoader.readFile(FileConfigLoader.loadTestCasesFiles());
        System.out.println("Original content : \n" + content);

        // Define the path to the values.yaml file
        String valuePath = VALUE_PATH;

        // Perform placeholder replacement
        String modifiedContent = placeholderReplacer.replacePlaceholders(content, valuePath);

        // Output the modified content
        System.out.println("Modified content after placeholder replacement : \n" + modifiedContent);

        List<TestCase> testCases = testCaseParser.parseTestCases(modifiedContent);

        //Iterate and save each test case into the repository
        for (TestCase testCase : testCases) {
            repository.save(testCase);
            System.out.println("Saved Test Case: " + testCase.getTestCaseId());
        }


        TestCase retrievedTestCase = repository.findById("1")
                .orElseThrow(() -> new IllegalArgumentException("Test case with ID 1 not found"));
        System.out.println("Found Test Case: " + retrievedTestCase.getTestCaseId());

        repository.deleteById("1");
        System.out.println("Test case with ID 1 deleted.");

    }
}
