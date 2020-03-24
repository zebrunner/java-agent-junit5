package com.zebrunner.agent.junit5.core;

import com.zebrunner.agent.core.registrar.RerunContextHolder;
import com.zebrunner.agent.core.rest.domain.TestDTO;
import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.platform.engine.UniqueId;

import java.util.List;
import java.util.function.Predicate;

public class RerunCondition implements ExecutionCondition {

    private static final String MSG_TEST_TO_RERUN = "Test is needed to rerun";
    private static final String MSG_TEST_NOT_TO_RERUN = "Test is not needed to rerun";
    private static final String MSG_IS_NOT_TEST = "Is not test instance";

    private static final String METHOD_EXTENSION_CONTEXT_CLASSNAME = "org.junit.jupiter.engine.descriptor.MethodExtensionContext";
    private static final ConditionEvaluationResult ENABLED = ConditionEvaluationResult.enabled("Is not rerun");

    @Override
    public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext context) {
        return RerunContextHolder.isRerun() ? processRerun(context) : ENABLED;
    }

    private static ConditionEvaluationResult processRerun(ExtensionContext context) {
        boolean enabled;
        String reason;

        boolean isMethodContext = isMethodContext(context);
        if (isMethodContext) {
            boolean testToRerun = isTestToRerun(context);

            enabled = testToRerun;
            reason = testToRerun ? MSG_TEST_TO_RERUN : MSG_TEST_NOT_TO_RERUN;
        } else {
            enabled = true;
            reason = MSG_IS_NOT_TEST;
        }

        return buildConditionEvaluationResult(enabled, reason);
    }

    private static boolean isTestToRerun(ExtensionContext context) {
        List<TestDTO> tests = RerunContextHolder.getTests();
        Predicate<TestDTO> predicateToRerun;

        List<UniqueId.Segment> segments = UniqueId.parse(context.getUniqueId()).getSegments();
        boolean testFactory = SegmentResolver.isTestFactory(segments);
        if (testFactory) {
            predicateToRerun = test -> {
                List<UniqueId.Segment> testUuidSegments = UniqueId.parse(test.getUuid()).getSegments();
                boolean isDynamic = SegmentResolver.isDynamic(testUuidSegments);
                return isDynamic
                        && test.getClassName().equals(context.getRequiredTestClass().getName())
                        && test.getMethodName().equals(context.getRequiredTestMethod().getName());
            };
        } else {
            predicateToRerun = test -> context.getUniqueId().equals(test.getUuid());
        }
        return tests.stream()
                    .anyMatch(predicateToRerun);
    }

    private static boolean isMethodContext(ExtensionContext context) {
        Class<?> methodExtensionContextClass = getMethodExtensionContextClass();
        return methodExtensionContextClass.isAssignableFrom(context.getClass());
    }

    private static ConditionEvaluationResult buildConditionEvaluationResult(boolean enabled, String reason) {
        return enabled ? ConditionEvaluationResult.enabled(reason) : ConditionEvaluationResult.disabled(reason);
    }

    private static Class<?> getMethodExtensionContextClass() {
        Class<?> result;
        try {
            result = Class.forName(METHOD_EXTENSION_CONTEXT_CLASSNAME);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Rerun functionality is not available. " + e.getMessage());
        }
        return result;
    }
}
