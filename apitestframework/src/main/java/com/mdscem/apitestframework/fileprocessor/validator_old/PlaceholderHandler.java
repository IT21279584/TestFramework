package com.mdscem.apitestframework.fileprocessor.validator_old;

import java.util.Map;

public interface PlaceholderHandler {
    Map<String, Object> loadValuesFromFile(String filePath) throws Exception;
    String replacePlaceholders(String content, Map<String, Object> valueMap) throws Exception;
    String mapToString(Object mapObject) throws Exception;
}
