package com.zebrunner.agent.junit5.core;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.platform.launcher.TestIdentifier;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class RunContextService {

    private static final Map<TestCorrelationData, Long> TEST_CORRELATION_DATA_TO_TEST_ID = new ConcurrentHashMap<>();

    public static void addTestCorrelationData(TestCorrelationData testCorrelationData, Long testId) {
        if (testCorrelationData != null) {
            TEST_CORRELATION_DATA_TO_TEST_ID.putIfAbsent(testCorrelationData, testId);
        }
    }

    public static Optional<Long> getZebrunnerIdOnRerun(TestIdentifier testIdentifier) {
        return TEST_CORRELATION_DATA_TO_TEST_ID.keySet()
                                               .stream()
                                               .filter(testCorrelationData -> testIdentifier.getUniqueId().equals(testCorrelationData.getUniqueId()))
                                               .map(TEST_CORRELATION_DATA_TO_TEST_ID::get)
                                               .findFirst();
    }

    public static boolean isEligibleForRerun(ExtensionContext extensionContext) {
        String uniqueId = extensionContext.getUniqueId();
        if (UniqueIdHelper.isClass(uniqueId) || UniqueIdHelper.isTestTemplate(uniqueId)) {
            return true;
        }

        if (UniqueIdHelper.isTestFactory(uniqueId)) {
            return isTestFactoryMethodEligibleForRerun(extensionContext);
        }

        return TEST_CORRELATION_DATA_TO_TEST_ID.keySet()
                                               .stream()
                                               .map(TestCorrelationData::getUniqueId)
                                               .anyMatch(testUniqueId -> testUniqueId.equals(uniqueId));
    }

    private static boolean isTestFactoryMethodEligibleForRerun(ExtensionContext extensionContext) {
        String uniqueId = extensionContext.getUniqueId();

        return TEST_CORRELATION_DATA_TO_TEST_ID.keySet()
                                               .stream()
                                               .map(TestCorrelationData::getUniqueId)
                                               .map(UniqueIdHelper::getParentIdOrNull)
                                               .filter(Objects::nonNull)
                                               .anyMatch(parentUniqueId -> uniqueId.equals(parentUniqueId) || uniqueId.equals(UniqueIdHelper.getParentIdOrNull(parentUniqueId)));
    }

}
