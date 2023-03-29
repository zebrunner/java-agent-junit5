package com.zebrunner.agent.junit5.core;

import lombok.extern.slf4j.Slf4j;
import org.junit.platform.engine.UniqueId;

@Slf4j
public class UniqueIdHelper {

    static final String CLASS_TYPE = "class";
    static final String TEST_FACTORY_TYPE = "test-factory";
    static final String TEST_TEMPLATE_TYPE = "test-template";
    static final String TEST_TEMPLATE_INVOCATION = "test-template-invocation";

    public static boolean isClass(String uniqueId) {
        return getLastSegment(uniqueId).getType().equals(CLASS_TYPE);
    }

    public static boolean isTestFactory(String uniqueId) {
        return getLastSegment(uniqueId).getType().equals(TEST_FACTORY_TYPE);
    }

    public static boolean isTestTemplate(String uniqueId) {
        return getLastSegment(uniqueId).getType().equals(TEST_TEMPLATE_TYPE);
    }

    private static UniqueId.Segment getLastSegment(String uniqueId) {
        return UniqueId.parse(uniqueId).getLastSegment();
    }

    public static boolean isTestTemplateInvocation(UniqueId uniqueId) {
        return uniqueId.getLastSegment().getType().equals(TEST_TEMPLATE_INVOCATION);
    }

    public static Integer getLastSegmentValueAsIntOrNull(UniqueId uniqueId) {
        try {
            return Integer.valueOf(uniqueId.getLastSegment().getValue().replace("#", ""));
        } catch (NumberFormatException e) {
            log.warn("Unable to parse numeric value from last segment. UniqueId: {}", uniqueId, e);
            return null;
        }
    }

    public static String getParentIdOrNull(String uniqueId) {
        int delimiterLastIndex = uniqueId.lastIndexOf("/");

        return delimiterLastIndex != -1
                ? uniqueId.substring(0, delimiterLastIndex)
                : null;
    }

}
