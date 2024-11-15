package com.mdscem.apitestframework.context;

public interface TestCaseRepository <T extends Testable> {

    Testable findByName(String name);

    void save(String name, T testable);

@Repository
public interface TestCaseRepository {

    TestCase findByName(String testCaseName);
    List<TestCase> findAll();
    void save(String testCaseName, TestCase testCase);
    void deleteById(String id);



}
