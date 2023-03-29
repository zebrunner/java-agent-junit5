package com.zebrunner.agent.junit5.adapter;

import com.zebrunner.agent.core.registrar.RunContextHolder;
import com.zebrunner.agent.core.registrar.TestRunRegistrar;
import com.zebrunner.agent.core.registrar.descriptor.Status;
import com.zebrunner.agent.core.registrar.descriptor.TestFinishDescriptor;
import com.zebrunner.agent.core.registrar.descriptor.TestRunFinishDescriptor;
import com.zebrunner.agent.core.registrar.descriptor.TestRunStartDescriptor;
import com.zebrunner.agent.core.registrar.descriptor.TestStartDescriptor;
import com.zebrunner.agent.junit5.core.RunContextService;
import com.zebrunner.agent.junit5.core.TestCorrelationData;
import com.zebrunner.agent.junit5.core.UniqueIdHelper;
import lombok.extern.slf4j.Slf4j;
import org.junit.platform.commons.util.ExceptionUtils;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.support.descriptor.MethodSource;
import org.junit.platform.launcher.TestIdentifier;
import org.junit.platform.launcher.TestPlan;

import java.time.OffsetDateTime;
import java.util.Map;

/**
 * Adapter used to convert JUnit 5 test domain to Zebrunner Agent domain
 */
@Slf4j
public class JUnit5Adapter {

    private static final TestRunRegistrar registrar = TestRunRegistrar.getInstance();

    private static final Map<TestExecutionResult.Status, Status> STATUS_MAPPER = Map.of(
            TestExecutionResult.Status.SUCCESSFUL, Status.PASSED,
            TestExecutionResult.Status.ABORTED, Status.ABORTED,
            TestExecutionResult.Status.FAILED, Status.FAILED
    );

    public void registerTestRunStart(TestPlan testPlan) {
        OffsetDateTime startedAt = OffsetDateTime.now();
        String name = "JUnit 5 test run [" + startedAt.toInstant() + " (UTC)]";
        TestRunStartDescriptor testRunStartDescriptor = new TestRunStartDescriptor(name, "junit5", startedAt, null);

        registrar.registerStart(testRunStartDescriptor);
    }

    public void registerTestRunFinish(TestPlan testPlan) {
        registrar.registerFinish(new TestRunFinishDescriptor(OffsetDateTime.now()));
    }

    public void registerTestStart(TestIdentifier testIdentifier) {
        if (!testIdentifier.isTest()) {
            return;
        }

        MethodSource methodSource = (MethodSource) testIdentifier.getSource().get();
        TestCorrelationData correlationData = new TestCorrelationData(testIdentifier.getUniqueId(), testIdentifier.getDisplayName());

        TestStartDescriptor testStartDescriptor = new TestStartDescriptor(
                correlationData.asJsonString(),
                testIdentifier.getDisplayName(),
                methodSource.getJavaClass(),
                methodSource.getJavaMethod(),
                this.getArgumentsIndex(testIdentifier)
        );

        if (RunContextHolder.isRerun()) {
            RunContextService.getZebrunnerIdOnRerun(testIdentifier)
                             .ifPresent(testStartDescriptor::setZebrunnerId);
        }

        registrar.registerTestStart(testIdentifier.getUniqueId(), testStartDescriptor);
    }

    private Integer getArgumentsIndex(TestIdentifier testIdentifier) {
        MethodSource methodSource = (MethodSource) testIdentifier.getSource().get();

        return !methodSource.getMethodParameterTypes().isBlank() && UniqueIdHelper.isTestTemplateInvocation(testIdentifier.getUniqueIdObject())
                ? UniqueIdHelper.getLastSegmentValueAsIntOrNull(testIdentifier.getUniqueIdObject())
                : null;
    }

    public void registerTestFinish(TestIdentifier testIdentifier, TestExecutionResult result) {
        if (!testIdentifier.isTest()) {
            return;
        }

        Status resultStatus = STATUS_MAPPER.get(result.getStatus());
        String resultStatusReason = result.getThrowable()
                                          .map(ExceptionUtils::readStackTrace)
                                          .orElse(null);
        TestFinishDescriptor testFinishDescriptor = new TestFinishDescriptor(resultStatus, OffsetDateTime.now(), resultStatusReason);

        registrar.registerTestFinish(testIdentifier.getUniqueId(), testFinishDescriptor);
    }

    public void registerTestSkipped(TestIdentifier testIdentifier, String reason) {
        if (!testIdentifier.isTest()) {
            return;
        }

        TestFinishDescriptor testFinishDescriptor = new TestFinishDescriptor(Status.SKIPPED, OffsetDateTime.now(), reason);

        registrar.registerTestFinish(testIdentifier.getUniqueId(), testFinishDescriptor);
    }

}
