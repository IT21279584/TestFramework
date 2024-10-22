package com.mdscem.apitestframework.context;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TestConfig {

    @Bean
    public TestCaseContext testCaseContext() {
        return new TestCaseContext();
    }

    @Bean
    public TestCaseRepositoryImpl testCaseRepositoryImpl() {
        return new TestCaseRepositoryImpl();
    }

}
