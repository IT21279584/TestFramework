package com.mdscem.apitestframework.fileprocessor.validator;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PlaceholderHandlerConfig {
    @Bean
    public PlaceholderHandler jsonPlaceholderHandler() {
        return new JsonPlaceholderHandler();
    }

    @Bean
    public PlaceholderHandler yamlPlaceholderHandler() {
        return new YamlPlaceholderHandler();
    }

    @Bean
    public PlaceholderReplacer placeholderReplacer(){
        return new PlaceholderReplacer();
    }
}
