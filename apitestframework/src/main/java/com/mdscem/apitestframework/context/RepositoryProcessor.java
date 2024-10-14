package com.mdscem.apitestframework.context;






import com.mdscem.apitestframework.fileprocessor.filereader.model.TestCase;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.*;

public class RepositoryProcessor {

    private static final Map<String, TestCase> testCaseStorage = new HashMap<>();

    @SuppressWarnings("unchecked")
    public static <T> T createRepository(Class<T> repositoryInterface) {
        return (T) Proxy.newProxyInstance(
                repositoryInterface.getClassLoader(),
                new Class[]{repositoryInterface},
                new RepositoryInvocationHandler()
        );
    }

    private static class RepositoryInvocationHandler implements InvocationHandler {

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
}
