package com.mdscem.apitestframework.requestprocessor.frameworkconfig;

import com.mdscem.apitestframework.frameworkImplementation.RestAssuredCoreFramework;
import com.mdscem.apitestframework.requestprocessor.CoreFramework;
import com.mdscem.apitestframework.requestprocessor.FrameworkAdapter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

import static com.mdscem.apitestframework.constants.Constant.RESTASSURED;
@Component
public class FrameworkLoader {
    private static final Logger logger = LogManager.getLogger(FrameworkLoader.class);
    @Autowired
    private FrameworkAdapter frameworkAdapter;
    @Autowired
    private RestAssuredCoreFramework restAssuredCoreFramework;

    public CoreFramework loadFrameworkFromConfig() throws IOException {
        String frameworkType = frameworkAdapter.loadFrameworkTypeFromConfig();
        logger.info("Framework loaded from config: " + frameworkType);

        switch (frameworkType.toLowerCase()) {
            case RESTASSURED:
                return restAssuredCoreFramework;
            default:
                throw new IllegalArgumentException("Unsupported framework type: " + frameworkType);
        }
    }
}