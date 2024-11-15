package com.mdscem.apitestframework.config;


import com.mdscem.apitestframework.context.FlowRepository;
import com.mdscem.apitestframework.context.FlowRepositoryImpl;
import com.mdscem.apitestframework.context.TestCaseRepository;
import com.mdscem.apitestframework.context.TestCaseRepositoryImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TestCaseConfig {
    @Bean
    public TestCaseRepository testCaseRepository() {
        return new TestCaseRepositoryImpl();
    }

    @Bean
    public FlowRepository flowRepository() {
        return new FlowRepositoryImpl();
    }

}
