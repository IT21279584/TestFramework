package com.mdscem.apitestframework.context;


import com.mdscem.apitestframework.fileprocessor.filereader.model.TestCase;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FlowRepository {

    void save(String flowName, Flow flow);
    List<Flow> findAll();


}
