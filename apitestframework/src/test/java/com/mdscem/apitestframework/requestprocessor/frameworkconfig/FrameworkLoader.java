package com.mdscem.apitestframework.requestprocessor.frameworkconfig;

import com.mdscem.apitestframework.frameworkImplementation.RestAssuredCoreFramework;
import com.mdscem.apitestframework.requestprocessor.CoreFramework;
import com.mdscem.apitestframework.requestprocessor.FrameworkAdapter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class FrameworkLoader {
    private static final Logger logger = LogManager.getLogger(FrameworkLoader.class);

    public CoreFramework loadFrameworkFromConfig() throws IOException {
        String frameworkType = FrameworkAdapter.loadFrameworkTypeFromConfig();
        logger.info("Framework loaded from config: " + frameworkType);

        switch (frameworkType.toLowerCase()) {
            case "restassured":
                return new RestAssuredCoreFramework();
            default:
                throw new IllegalArgumentException("Unsupported framework type: " + frameworkType);
        }
    }
}