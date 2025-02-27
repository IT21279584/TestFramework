package com.mdscem.apitestframework.fileprocessor.validator;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mdscem.apitestframework.TestExecutor;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.xml.bind.ValidationException;
import java.io.File;
import java.io.IOException;
import java.util.Set;


@Component
public class SchemaValidation {
    private static final Logger logger = LogManager.getLogger(SchemaValidation.class);
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
            for (ValidationMessage error : validationErrors) {
                logger.error(error.getMessage());
            }
            try {
                throw new ValidationException("JSON validation failed.");
            } catch (ValidationException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
