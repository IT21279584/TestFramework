package com.mdscem.apitestframework;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;



import java.io.PrintWriter;

public class ApitestframeworkApplication {
	public static void main(String[] args) {
//		System.out.println("Starting Spring Boot application...");
//
//		// Initialize Spring ApplicationContext
//		ConfigurableApplicationContext context = SpringApplication.run(ApitestframeworkApplication.class, args);
//		System.out.println("Spring Context Initialized!");
//
//		LauncherDiscoveryRequest request = LauncherDiscoveryRequestBuilder.request()
//				.selectors(selectClass(TestExecutor.class))
//				.build();
//
//		System.out.println("Starting JUnit tests. 566565..");
//
//		Launcher launcher = LauncherFactory.create();
//		SummaryGeneratingListener listener = new SummaryGeneratingListener();
//		launcher.registerTestExecutionListeners(listener);
//		launcher.execute(request);
//
//		System.out.println("JUnit tests execution completed.");
//		TestExecutionSummary summary = listener.getSummary();
//		summary.printTo(new PrintWriter(System.out));
//
//		// Close Spring context after tests
//		context.close();

		SpringApplication.run(ApiTestMain.class);
	}
}
