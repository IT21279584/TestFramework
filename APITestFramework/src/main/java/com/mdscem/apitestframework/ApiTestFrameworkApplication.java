package com.mdscem.apitestframework;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class ApiTestFrameworkApplication implements CommandLineRunner {
	@Autowired
	private TestExecutor executor;

	public static void main(String[] args) {
		SpringApplication.run(ApiTestFrameworkApplication.class, args);
	}

	@Override
	public void run(String... args) {
		executor.execute();
	}
}
