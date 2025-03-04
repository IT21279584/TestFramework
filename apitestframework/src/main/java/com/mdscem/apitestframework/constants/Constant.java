package com.mdscem.apitestframework.constants;

public interface Constant {

    public static final String PATH_PARAM = "pathParam";
    public static final String QUERY_PARAM = "queryParam";
    public static final String DELAY = "delay";
    public static final String RESPONSE = "response";
    public static final String TESTCASE_NAME = "name";
    public static final String REQUEST = "request";
    public static final String CAPTURE = "capture";
    public static final String FRAMEWORK = "framework";
    public static final String REPORT_NAME = "API Test Report";
    public static final String CHECK = "check";
    public static final String TYPE = "type";
    public static final String ALL = "ALL";
    public static final String TESTCASE = "testCase";
    public static final  String RESTASSURED = "restassured";
    public static final String TOKEN = "BEARER_TOKEN";
    public static final String BASIC = "BASIC_AUTH";
    public static final String USERNAME = "username";
    public static final String PASSWORD = "password";
    public static final String JSON_EXTENTION =".json";
    public static final String YAML_EXTENTION = ".yaml";
    public static final String YML_EXTENTION = ".yml";
    public static final String INCLUDE_KEYWORD = "{{include ";
    public static final String END_DOUBLE_CURLY_BRACKET = "}}";
    public static final String START_DOUBLE_CURLY_BRACKET = "{{";
    public static final String END_CURLY_BRACKET = "}";
    public static final String PARAM_PATTERN = "\\{\\{param (\\w+)\\}}";
    public final static String CAPTURE_PATTERN = "\\{\\{use (\\w+)\\.(\\w+)}}";
    public static final String SPLITTER = "\\.";
    public static final String SEMI_COLON = ";";
    public static final String START_ROUND_BRACKET = "(";
    public static final String END_ROUND_BRACKET = ")";
    public static final String TRUE = "true";
    public static final String FALSE = "false";
    public static final String START_SQUARE_BRACKET = "[";
    public static final String END_SQUARE_BRACKET = "]";
    public static final String COMMA = ",";
    public static final String DOT = ".";
    public static final String CLASS = ".class";
    public static final String CLASS_LANG = "java.lang.";
    public static final String BACKSLASH = "\"";
    public static final String DIGITS = "\\d+";
    public static final String ONE_OR_MORE_DIGITS = "\\d+\\.\\d+";
    public static final String PLACEHOLDER_VALIDATOR = "\\{\\{.*\\}\\}";
}
