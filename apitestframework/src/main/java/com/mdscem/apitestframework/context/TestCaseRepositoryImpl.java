package com.mdscem.apitestframework.context;

import com.mdscem.apitestframework.fileprocessor.filereader.model.TestCase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class TestCaseRepositoryImpl implements TestCaseRepository {

    @Autowired
    private TestContext context;

    @Override
    public Testable findByName(String name) {
        return context.getTestCaseMap().get(name);
    }

    @Override
    public void save(String name, Testable testable) {
        context.getTestCaseMap().put(name, (TestCase) testable);
    }

    @Override
    public void deleteById(String id) {
        context.getTestCaseMap().remove(id);
    }
}
