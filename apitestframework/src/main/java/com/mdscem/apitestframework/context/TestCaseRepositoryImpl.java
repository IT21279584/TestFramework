package com.mdscem.apitestframework.context;

import com.mdscem.apitestframework.fileprocessor.filereader.model.TestCase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public class TestCaseRepositoryImpl implements TestCaseRepository {

    @Autowired
    private TestContext context;

    @Override
    public TestCase save(String testCaseName, TestCase testCase) {
        context.getTestCaseMap().put(testCaseName, testCase);
        return testCase;
    }

    @Override
    public void deleteById(String id) {
        context.getTestCaseMap().remove(id);
    }

    @Override
    public TestCase findByName(String testCaseName) {
        return context.getTestCaseMap().get(testCaseName);
    }

    @Override
    public List<TestCase> findAll() {
        return new ArrayList<>(context.getTestCaseMap().values());
    }
}
