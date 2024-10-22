package com.mdscem.apitestframework.context;

import com.mdscem.apitestframework.fileprocessor.filereader.model.TestCase;

import java.lang.reflect.Proxy;
import java.util.Map;

public class RepositoryProcessor {

    private TestCaseRepositoryImpl testCaseStorage;

    // Constructor accepting file path and the shared test case storage
//    public RepositoryProcessor(Map<String, TestCase> sharedStorage) {
//        this.testCaseStorage = new TestCaseRepositoryImpl(sharedStorage); // Initialize with shared storage
//    }
//
//    @SuppressWarnings("unchecked")
//    public <T> T createRepository(Class<T> repositoryInterface) {
//        return (T) Proxy.newProxyInstance(
//                repositoryInterface.getClassLoader(),
//                new Class[]{repositoryInterface},
//                new RepositoryInvocationHandler(testCaseStorage) // Pass storage to handler
//        );
//    }
}
