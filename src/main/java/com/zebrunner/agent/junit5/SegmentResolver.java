package com.zebrunner.agent.junit5;

import org.junit.platform.engine.UniqueId;

import java.util.List;

public class SegmentResolver {

    private static final String DYNAMIC_TEST = "dynamic-test";
    private static final String TEST_FACTORY = "test-factory";
    private static final String PARAMETERIZED_TEST = "test-template-invocation";

    public static boolean isDynamic(List<UniqueId.Segment> segments) {
        return containsSegment(DYNAMIC_TEST, segments);
    }

    public static boolean isTestFactory(List<UniqueId.Segment> segments) {
        return containsSegment(TEST_FACTORY, segments);
    }

    public static boolean isParameterizedTest(List<UniqueId.Segment> segments) {
        return containsSegment(PARAMETERIZED_TEST, segments);
    }

    private static boolean containsSegment(String type, List<UniqueId.Segment> segments) {
        return segments.stream().anyMatch(segment -> segment.getType().equals(type));
    }

    private String getSegmentValue(String type, List<UniqueId.Segment> segments) {
        return segments.stream()
                       .filter(segment -> segment.getType().equals(type))
                       .map(UniqueId.Segment::getValue)
                       .findAny()
                       .orElse(null);
    }
}
