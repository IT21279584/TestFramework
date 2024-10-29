package com.mdscem.apitestframework.fileprocessor.fileprocessorconfig;

import com.mdscem.apitestframework.fileprocessor.filereader.TestCasesToJsonNodeReader;
import com.mdscem.apitestframework.fileprocessor.validator.TestCaseReplacer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PlaceholderHandlerConfig {

    @Bean
    public TestCaseReplacer testCaseReplacer(){
        return new TestCaseReplacer();
    }

    @Bean
    public TestCasesToJsonNodeReader testCasesToJsonNodeReader(){
        return new TestCasesToJsonNodeReader();
    }
}
