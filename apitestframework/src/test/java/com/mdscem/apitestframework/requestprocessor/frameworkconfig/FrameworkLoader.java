package com.mdscem.apitestframework.requestprocessor.frameworkconfig;

import com.mdscem.apitestframework.frameworkImplementation.RestAssuredCoreFramework;
import com.mdscem.apitestframework.requestprocessor.CoreFramework;
import com.mdscem.apitestframework.requestprocessor.FrameworkAdapter;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class FrameworkLoader {

    public CoreFramework loadFrameworkFromConfig() throws IOException {
        String frameworkType = FrameworkAdapter.loadFrameworkTypeFromConfig();
        System.out.println("Framework loaded from config: " + frameworkType);

        switch (frameworkType.toLowerCase()) {
            case "restassured":
                return new RestAssuredCoreFramework();
            default:
                throw new IllegalArgumentException("Unsupported framework type: " + frameworkType);
        }
    }
}