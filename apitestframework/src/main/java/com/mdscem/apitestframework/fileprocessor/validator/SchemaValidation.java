package com.mdscem.apitestframework.fileprocessor.validator;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mdscem.apitestframework.fileprocessor.TestCaseProcessor;
import com.mdscem.apitestframework.fileprocessor.filereader.model.TestCase;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.xml.bind.ValidationException;
import java.io.File;
import java.io.IOException;
import java.util.Set;

import static com.mdscem.apitestframework.constants.Constant.VALIDATION_FILE_PATH;

@Component
public class SchemaValidation {
    @Autowired
    private static TestCaseProcessor testCaseProcessor;

    public static TestCase validateTestcase(JsonNode jsonNode) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonSchemaFactory jsonSchemaFactory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V7);

        JsonNode schemaNode = objectMapper.readTree(new File(VALIDATION_FILE_PATH));

        JsonSchema schema = jsonSchemaFactory.getSchema(schemaNode);

        Set<ValidationMessage> validationErrors = schema.validate(jsonNode);

        if (validationErrors.isEmpty()) {
            return testCaseProcessor.jsonNodeToTestCase(jsonNode);
        } else {
            System.out.println("JSON is not valid. Errors:");
            for (ValidationMessage error : validationErrors) {
                System.out.println(error.getMessage());
            }
            try {
                throw new ValidationException("JSON validation failed.");
            } catch (ValidationException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
