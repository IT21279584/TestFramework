package com.mdscem.apitestframework.context;


import com.mdscem.apitestframework.fileprocessor.filereader.model.TestCase;

import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

public interface TestCaseRepository {

    Optional<TestCase> findById(String id);

    List<TestCase> findAll();

    void save(TestCase testCase);

    void deleteById(String id);

}
