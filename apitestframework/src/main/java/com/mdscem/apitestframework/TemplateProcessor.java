//package com.mdscem.apitestframework;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.samskivert.mustache.Mustache;
//import com.samskivert.mustache.Template;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//import java.io.StringReader;
//import java.nio.file.Files;
//import java.nio.file.Paths;
//import java.util.Map;
//
//@Service
//public class TemplateProcessor {
//
//    @Autowired
//    private Mustache.Compiler mustacheCompiler;
//
//    public TestCase processTemplate(String templateFile, Map<String, Object> values) throws Exception {
//        // Load the YAML/JSON template as a string
//        String templateString = new String(Files.readAllBytes(Paths.get(templateFile)));
//
//        // Compile the template
//        Template template = mustacheCompiler.compile(new StringReader(templateString));
//
//        // Execute the template with the dynamic values
//        String renderedTemplate = template.execute(values);
//
//        // Convert the rendered template (YAML/JSON) into a TestCase POJO
//        ObjectMapper mapper = new ObjectMapper();
//        TestCase testCase = mapper.readValue(renderedTemplate, TestCase.class);
//
//        return testCase;
//    }
//}
