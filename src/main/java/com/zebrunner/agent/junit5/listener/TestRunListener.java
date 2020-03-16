package com.zebrunner.agent.junit5.listener;

import com.zebrunner.agent.junit5.adapter.JUnit5Adapter;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.launcher.TestExecutionListener;
import org.junit.platform.launcher.TestIdentifier;
import org.junit.platform.launcher.TestPlan;

/**
 * Zebrunner Agent Listener implementation tracking JUnit 5 test run events
 */
public class TestRunListener implements TestExecutionListener {

    private final JUnit5Adapter adapter;

    public TestRunListener() {
        this.adapter = new JUnit5Adapter();
    }

    @Override
    public void testPlanExecutionStarted(TestPlan testPlan) {
        adapter.registerRunStart(testPlan);
    }

    @Override
    public void testPlanExecutionFinished(TestPlan testPlan) {
        adapter.registerRunFinish(testPlan);
    }

    @Override
    public void dynamicTestRegistered(TestIdentifier testIdentifier) {
        System.out.println("Dynamic test registered: " + testIdentifier);
    }

    @Override
    public void executionStarted(TestIdentifier testIdentifier) {
        adapter.registerTestStart(testIdentifier);
    }

    @Override
    public void executionFinished(TestIdentifier testIdentifier, TestExecutionResult testExecutionResult) {
        adapter.registerTestFinish(testIdentifier, testExecutionResult);
    }

    @Override
    public void executionSkipped(TestIdentifier testIdentifier, String reason) {
        adapter.registerTestSkipped(testIdentifier, reason);
    }

}
