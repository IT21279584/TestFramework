package com.mdscem.apitestframework.context;


import com.mdscem.apitestframework.fileprocessor.filereader.model.TestCase;

import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TestCaseRepository {

    TestCase findByName(String id);
    List<TestCase> findAll();
    void saveTestCase(String testCaseName, TestCase testCase);
    void deleteById(String id);
    void saveFlow(String flowName, Flow flow);
    List<Flow> findAllFlows();


}
