package com.mdscem.apitestframework.context;




import com.mdscem.apitestframework.fileprocessor.filereader.model.TestCase;

import java.util.List;
import java.util.Optional;

@Repository
public interface TestCaseRepository {

    @FindById
    Optional<TestCase> findById(String id);

    @FindAll
    List<TestCase> findAll();

    @Save
    void save(TestCase testCase);

    @Delete
    void deleteById(String id);

}
