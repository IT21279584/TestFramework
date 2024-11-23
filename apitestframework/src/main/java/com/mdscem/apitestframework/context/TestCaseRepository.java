package com.mdscem.apitestframework.context;

public interface TestCaseRepository <T extends Testable> {

    Testable findByName(String name);

    void save(String name, T testable);

    void deleteById(String id);
}
