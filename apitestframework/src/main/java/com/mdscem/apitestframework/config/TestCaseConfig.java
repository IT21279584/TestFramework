package com.mdscem.apitestframework.config;


import com.mdscem.apitestframework.context.TestCaseContext;
import com.mdscem.apitestframework.context.TestCaseRepository;
import com.mdscem.apitestframework.context.TestCaseRepositoryImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TestCaseConfig {

    @Bean
    public TestCaseContext testCaseContext() {
        return new TestCaseContext();
    }

    @Bean
    public TestCaseRepository testCaseRepositoryImpl() {
        return new TestCaseRepositoryImpl();
    }
}
