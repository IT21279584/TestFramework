package com.mdscem.apitestframework.context;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class FlowRepositoryImpl implements TestCaseRepository {

    @Autowired
    private TestContext context;

    @Override
    public Flow findByName(String name) {
        return context.getFlowMap().get(name);
    }

    @Override
    public void save(String name, Testable testable) {
        context.getFlowMap().put(name, (Flow) testable);
    }

    @Override
    public void deleteById(String id) {
        context.getFlowMap().remove(id);
    }
}
