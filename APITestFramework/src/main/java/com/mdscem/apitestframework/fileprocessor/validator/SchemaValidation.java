package com.mdscem.apitestframework.fileprocessor.validator;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mdscem.apitestframework.constants.Constant;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class SchemaValidation {
    @Autowired
    private ObjectMapper objectMapper;

    public JsonNode validateTestcase(JsonNode jsonNode, String schemaPath) throws IOException {
        JsonSchemaFactory jsonSchemaFactory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V7);
        JsonNode schemaNode = objectMapper.readTree(new File(schemaPath));
        JsonSchema schema = jsonSchemaFactory.getSchema(schemaNode);

        Set<ValidationMessage> validationErrors = schema.validate(jsonNode);

        if (validationErrors.isEmpty()) {
            return jsonNode;
        } else {
            // Collect all error messages into a single string
            String errorDetails = validationErrors.stream()
                    .map(ValidationMessage::getMessage)
                    .collect(Collectors.joining(Constant.SEMI_COLON));
            throw new RuntimeException("JSON validation failed with errors: " + errorDetails);
        }
    }
}
