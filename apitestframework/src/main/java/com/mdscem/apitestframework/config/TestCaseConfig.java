package com.mdscem.apitestframework.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.mdscem.apitestframework.context.FlowRepositoryImpl;
import com.mdscem.apitestframework.context.TestCaseRepository;
import com.mdscem.apitestframework.context.TestCaseRepositoryImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class TestCaseConfig {
    @Bean
    public TestCaseRepository testCaseRepository() {
        return new TestCaseRepositoryImpl();
    }
    @Bean
    public TestCaseRepository flowRepository() {
        return new FlowRepositoryImpl();
    }
    @Bean(name = "yamlMapper")
    public ObjectMapper yamlMapper() {
        return new ObjectMapper(new YAMLFactory());
    }
    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

}
