package com.mdscem.apitestframework.context;


import com.mdscem.apitestframework.fileprocessor.filereader.model.TestCase;

import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TestCaseRepository {

    TestCase findByName(String testCaseName);
    List<TestCase> findAll();
    TestCase save(String testCaseName, TestCase testCase);
    void deleteById(String id);



}
