package com.mdscem.apitestframework.context;

import com.mdscem.apitestframework.fileprocessor.filereader.model.TestCase;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.*;

public class TestCaseRepositoryImpl implements TestCaseRepository {

    @Autowired
    private TestCaseContext context;

    @Override
    public void save(TestCase testCase) {
        // Directly access the testCaseStorage map from the context
        context.testCaseMap.put(testCase.getTestCaseId(), testCase);
        System.out.println("Saved Test Case: " + testCase.getTestCaseId());
    }

    @Override
    public void deleteById(String id) {
        // Directly access the testCaseStorage map from the context
        context.testCaseMap.remove(id);
        System.out.println("Deleted Test Case with ID: " + id);
    }

    @Override
    public Optional<TestCase> findById(String id) {
        // Directly access the testCaseStorage map from the context
        return Optional.ofNullable(context.testCaseMap.get(id));
    }

    @Override
    public List<TestCase> findAll() {
        // Directly access the testCaseStorage map from the context
        return new ArrayList<>(context.testCaseMap.values());
    }
}
