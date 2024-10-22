package com.mdscem.apitestframework.context;


import com.mdscem.apitestframework.fileprocessor.filereader.model.TestCase;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;


public  class RepositoryInvocationHandler implements InvocationHandler {

    private final TestCaseRepositoryImpl storage;

    public RepositoryInvocationHandler(TestCaseRepositoryImpl storage) {
        this.storage = storage;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (method.isAnnotationPresent(Save.class)) {
            TestCase testCase = (TestCase) args[0];
            storage.save(testCase);
            return null;
        } else if (method.isAnnotationPresent(Delete.class)) {
            String id = (String) args[0];
            storage.deleteById(id);
            return null;
        } else if (method.isAnnotationPresent(FindById.class)) {
            String id = (String) args[0];
            return storage.findById(id);
        } else if (method.isAnnotationPresent(FindAll.class)) {
            return storage.findAll();
        }
        return null;
    }
}