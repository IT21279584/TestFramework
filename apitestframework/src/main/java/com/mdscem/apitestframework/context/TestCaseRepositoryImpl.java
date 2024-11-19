package com.mdscem.apitestframework.context;

import com.mdscem.apitestframework.fileprocessor.filereader.model.TestCase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public class TestCaseRepositoryImpl implements TestCaseRepository {

    @Autowired
    private FlowContext context;

    @Override
    public void saveTestCase(String testCaseName, TestCase testCase) {
        context.getTestCaseMap().put(testCaseName, testCase);
    }

    @Override
    public void deleteById(String id) {
        context.getTestCaseMap().remove(id);
    }

    @Override
    public TestCase findByName(String testCaseName) {
        return context.testCaseMap.get(testCaseName);
    }

    @Override
    public List<TestCase> findAll() {
        return new ArrayList<>(context.getTestCaseMap().values());
    }

    @Override
    public void saveFlow(String flowName, Flow flow) {
        context.getFlowMap().put(flowName, flow);
    }

    @Override
    public List<Flow> findAllFlows() {
        return new ArrayList<>(context.getFlowMap().values());
    }
}
