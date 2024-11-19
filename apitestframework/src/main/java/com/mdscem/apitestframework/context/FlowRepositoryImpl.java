package com.mdscem.apitestframework.context;

import com.mdscem.apitestframework.fileprocessor.filereader.model.TestCase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
public class FlowRepositoryImpl implements FlowRepository {

    @Autowired
    private FlowContext context;

    @Override
    public void save(String flowName, Flow flow) {
        context.getFlowMap().put(flowName, flow);
    }

    @Override
    public List<Flow> findAll() {
        return new ArrayList<>(context.getFlowMap().values());
    }
}
