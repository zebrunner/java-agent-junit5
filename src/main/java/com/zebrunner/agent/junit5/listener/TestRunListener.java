package com.zebrunner.agent.junit5.listener;

import com.zebrunner.agent.junit5.adapter.JUnit5Adapter;
import lombok.extern.slf4j.Slf4j;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.reporting.ReportEntry;
import org.junit.platform.launcher.TestExecutionListener;
import org.junit.platform.launcher.TestIdentifier;
import org.junit.platform.launcher.TestPlan;

/**
 * Zebrunner Agent Listener implementation tracking JUnit 5 test run events
 */
@Slf4j
public class TestRunListener implements TestExecutionListener {

    private final JUnit5Adapter adapter;

    public TestRunListener() {
        this.adapter = new JUnit5Adapter();
    }

    @Override
    public void testPlanExecutionStarted(TestPlan testPlan) {
        log.debug("Beginning TestRunListener -> testPlanExecutionStarted(TestPlan testPlan)");
        adapter.registerTestRunStart(testPlan);
        log.debug("Finishing TestRunListener -> testPlanExecutionStarted(TestPlan testPlan)");
    }

    @Override
    public void testPlanExecutionFinished(TestPlan testPlan) {
        log.debug("Beginning TestRunListener -> testPlanExecutionFinished(TestPlan testPlan)");
        adapter.registerTestRunFinish(testPlan);
        log.debug("Finishing TestRunListener -> testPlanExecutionFinished(TestPlan testPlan)");
    }

    @Override
    public void dynamicTestRegistered(TestIdentifier testIdentifier) {
        log.debug("Beginning TestRunListener -> dynamicTestRegistered(TestIdentifier testIdentifier)");
        log.debug("Finishing TestRunListener -> dynamicTestRegistered(TestIdentifier testIdentifier)");
    }

    @Override
    public void executionSkipped(TestIdentifier testIdentifier, String reason) {
        log.debug("Beginning TestRunListener -> dynamicTestRegistered(TestIdentifier testIdentifier)");
        adapter.registerTestSkipped(testIdentifier, reason);
        log.debug("Finishing TestRunListener -> dynamicTestRegistered(TestIdentifier testIdentifier)");
    }

    @Override
    public void executionStarted(TestIdentifier testIdentifier) {
        log.debug("Beginning TestRunListener -> executionStarted(TestIdentifier testIdentifier)");
        adapter.registerTestStart(testIdentifier);
        log.debug("Finishing TestRunListener -> executionStarted(TestIdentifier testIdentifier)");
    }

    @Override
    public void executionFinished(TestIdentifier testIdentifier, TestExecutionResult testExecutionResult) {
        log.debug("Beginning TestRunListener -> executionFinished(TestIdentifier testIdentifier, TestExecutionResult testExecutionResult)");
        adapter.registerTestFinish(testIdentifier, testExecutionResult);
        log.debug("Finishing TestRunListener -> executionFinished(TestIdentifier testIdentifier, TestExecutionResult testExecutionResult)");
    }

    @Override
    public void reportingEntryPublished(TestIdentifier testIdentifier, ReportEntry entry) {
        log.debug("Beginning TestRunListener -> reportingEntryPublished(TestIdentifier testIdentifier, ReportEntry entry)");
        log.debug("Finishing TestRunListener -> reportingEntryPublished(TestIdentifier testIdentifier, ReportEntry entry)");
    }

}
