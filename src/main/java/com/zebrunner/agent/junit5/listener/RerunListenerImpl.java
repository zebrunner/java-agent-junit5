package com.zebrunner.agent.junit5.listener;

import com.zebrunner.agent.core.listener.RerunListener;
import com.zebrunner.agent.core.registrar.RunContextHolder;
import com.zebrunner.agent.core.registrar.domain.TestDTO;
import com.zebrunner.agent.junit5.core.RunContextService;
import com.zebrunner.agent.junit5.core.TestCorrelationData;
import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.util.List;

public class RerunListenerImpl implements RerunListener, ExecutionCondition {

    private static final ConditionEvaluationResult ENABLED = ConditionEvaluationResult.enabled(null);
    private static final ConditionEvaluationResult DISABLED = ConditionEvaluationResult.disabled(null);

    @Override
    public void onRerun(List<TestDTO> tests) {
        for (TestDTO test : tests) {
            TestCorrelationData testCorrelationData = TestCorrelationData.fromJsonString(test.getCorrelationData());

            RunContextService.addTestCorrelationData(testCorrelationData, test.getId());
        }
    }

    @Override
    public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext context) {
        return !RunContextHolder.isRerun() || RunContextService.isEligibleForRerun(context)
                ? ENABLED
                : DISABLED;
    }

}
