package com.mdscem.apitestframework.fileprocessor.filereader;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.NoArgsConstructor;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@NoArgsConstructor
public class TestCaseLoader {

    private static final Logger logger = Logger.getLogger(TestCaseLoader.class);

    private String fileName;
    private FileReaderContext fileReaderContext;
    private ObjectMapper objectMapper;

    public TestCaseLoader(String fileName) {
        this.fileName = fileName;
        this.fileReaderContext = selectFileReader(fileName);
        this.checkFileType(fileName);
    }

    private FileReaderContext selectFileReader(String fileName) {
        IFileReader iFileReader;

        if (fileName.endsWith(".json")) {
            iFileReader = new JsonFileReader();
        } else if (fileName.endsWith(".yaml") || fileName.endsWith(".yml")) {
            iFileReader = new YamlFileReader();
        } else {
            logger.error("Unsupported file type: " + fileName);
            throw new IllegalArgumentException("Unsupported file type " + fileName);
        }
        logger.info("FileReader successfully selected for: " + fileName);
        return new FileReaderContext(iFileReader);
    }

    public String checkFileType(String fileName){
        if (fileName.endsWith(".json")) {
            return "json";
        } else if (fileName.endsWith(".yaml") || fileName.endsWith(".yml")) {
            return "yaml";
        } else {
            logger.error("Unsupported file type: " + fileName);
            throw new IllegalArgumentException("Unsupported file type " + fileName);
        }
    }

    public String loadTestCases() {

        String fileContent = null;
        try {
            Path filePath = Paths.get(fileName);
            if (!Files.exists(filePath)) {
                logger.error("Could not find the file: " + fileName);
            }

            fileContent = new String(Files.readAllBytes(filePath));

//            loadedTestCases = fileReaderContext.loadTestCases(fileContent);

        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("New Test Cases " + fileContent);
        System.out.println("+++++++++++++++++ " + fileContent.toString());
        return fileContent.toString();
    }

}