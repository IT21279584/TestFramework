package com.mdscem.apitestframework.context;






import com.mdscem.apitestframework.fileprocessor.filereader.model.TestCase;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.*;

public class RepositoryProcessor {

    @SuppressWarnings("unchecked")
    public static <T> T createRepository(Class<T> repositoryInterface) {
        return (T) Proxy.newProxyInstance(
                repositoryInterface.getClassLoader(),
                new Class[]{repositoryInterface},
                new RepositoryInvocationHandler()
        );
    }
}
