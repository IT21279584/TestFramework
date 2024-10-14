package com.mdscem.apitestframework.context;


import com.mdscem.apitestframework.fileprocessor.filereader.model.TestCase;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public  class RepositoryInvocationHandler implements InvocationHandler {

    private static final Map<String, TestCase> testCaseStorage = new HashMap<>();

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (method.isAnnotationPresent(Save.class)) {
            TestCase testCase = (TestCase) args[0];
            testCaseStorage.put(testCase.getTestCaseId(), testCase);
            System.out.println("Saved Test Case: " + testCase.getTestCaseId());
            return null;
        } else if (method.isAnnotationPresent(Delete.class)) {
            String id = (String) args[0];
            testCaseStorage.remove(id);
            System.out.println("Deleted Test Case with ID: " + id);
            return null;
        } else if (method.isAnnotationPresent(FindById.class)) {
            String id = (String) args[0];
            return Optional.ofNullable(testCaseStorage.get(id));
        } else if (method.isAnnotationPresent(FindAll.class)) {
            return new ArrayList<>(testCaseStorage.values());
        }
        return null;
    }
}