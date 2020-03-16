package com.zebrunner.agent.junit5.adapter;

import com.zebrunner.agent.core.registrar.Status;
import com.zebrunner.agent.core.registrar.TestFinishDescriptor;
import com.zebrunner.agent.core.registrar.TestRunFinishDescriptor;
import com.zebrunner.agent.core.registrar.TestRunRegistrar;
import com.zebrunner.agent.core.registrar.TestRunStartDescriptor;
import com.zebrunner.agent.core.registrar.TestStartDescriptor;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.MethodSource;
import org.junit.platform.launcher.TestIdentifier;
import org.junit.platform.launcher.TestPlan;

import java.lang.reflect.Method;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

/**
 * Adapter used to convert JUnit 5 test domain to Zebrunner Agent domain
 */
public class JUnit5Adapter {

    private static final String CLASS = "class";
    private static final String DYNAMIC_TEST = "dynamic-test";
    private static final String PARAMETERIZED_TEST = "test-template-invocation";

    private static final TestRunRegistrar registrar = TestRunRegistrar.stdoutRegistrar();

    public void registerRunStart(TestPlan testPlan) {
//        System.out.println(Thread.currentThread().getName());
        // TestPlan is MUTABLE - we can't rely on hash code
        OffsetDateTime startedAt = OffsetDateTime.now();
        String name = "JUnit 5 test run from " + startedAt.toInstant() + " (UTC)";
        TestRunStartDescriptor testRunStartDescriptor = new TestRunStartDescriptor(name, "junit5", startedAt, name);

        registrar.start(testRunStartDescriptor);
    }

    public void registerRunFinish(TestPlan testPlan) {
        TestRunFinishDescriptor finishDescriptor = new TestRunFinishDescriptor(OffsetDateTime.now());
        registrar.finish(finishDescriptor);
    }

    public void registerTestStart(TestIdentifier testIdentifier) {
        if (testIdentifier.isTest()) {
            OffsetDateTime startedAt = OffsetDateTime.now();
            String uuid = testIdentifier.getUniqueId();
            List<UniqueId.Segment> idSegments = UniqueId.parse(uuid).getSegments();
            MethodSource source = (MethodSource) testIdentifier.getSource().get(); // additional validation here?
            boolean dynamic = containsSegment(DYNAMIC_TEST, idSegments);
            boolean parameterized = containsSegment(PARAMETERIZED_TEST, idSegments);
            String testName = buildTestName(testIdentifier, dynamic || parameterized);

//            test.setUuid(uuid);
//            test.setName(testName);
//            test.setClassName(source.getClassName());
//            test.setMethodName(source.getMethodName());
//            test.setTags(testIdentifier.getTags().stream().map(TestTag::getName).collect(Collectors.toSet()));
//            test.setStartedAt(LocalDateTime.now());
//            if (dynamic) {
//                test.setDynamic(true);
//                int index = Integer.parseInt(getSegmentValue(DYNAMIC_TEST, idSegments).replace("#", "")); //remove leading # symbol to only keep number
//                test.setDynamicIndex(index);
//            }
//
//            OwnershipResolver<TestIdentifier> ownershipResolver = new OwnershipResolver<>(new JUnit5OwnershipAdapter());
//            String ownerName = ownershipResolver.retrieveOwnerName(testIdentifier);
//            test.setOwnerName(ownerName);


            Class<?> klass = null;
            try {
                klass = Class.forName(source.getClassName());
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            Class<?>[] parameterClasses = Arrays.stream(source.getMethodParameterTypes().split(","))
                                                .map(String::trim)
                                                .map((Function<String, Class<?>>) this::getClassForName)
                                                .filter(Objects::nonNull)
                                                .toArray(Class[]::new);
            Method method = null;
            try {
                method = klass.getDeclaredMethod(source.getMethodName(), parameterClasses);
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            }
            TestStartDescriptor testStartDescriptor = new TestStartDescriptor(uuid, testName, startedAt, klass, method);
            registrar.startTest(uuid, testStartDescriptor);
        }
    }

    public void registerTestFinish(TestIdentifier testIdentifier, TestExecutionResult result) {
        if (testIdentifier.isTest()) {
            OffsetDateTime endedAt = OffsetDateTime.now();
            String uid = testIdentifier.getUniqueId();

            UniqueId uniqueId = UniqueId.parse(uid);
            List<UniqueId.Segment> idSegments = uniqueId.getSegments();

//            Test test = new JUnit5Test();
//
//            test.setUuid(UUID.randomUUID().toString());
//            test.setClassName(getSegmentValue(CLASS, idSegments));
//            test.setEndedAt(LocalDateTime.now());
//
//            TestExecutionResult.Status status = result.getStatus();
//            Result resultStatus = null;
            TestExecutionResult.Status status = result.getStatus();
            Status resultStatus = null;
            String message = null;
            switch (status) {
                case SUCCESSFUL:
                    resultStatus = Status.PASSED;
                    break;
                case FAILED:
                    if (result.getThrowable().isPresent()) {
                        message = result.getThrowable().get().getMessage();
                    }
                    resultStatus = Status.FAILED;
                    break;
                case ABORTED:
                    resultStatus = Status.ABORTED;
                    break;
            }

            TestFinishDescriptor testFinishDescriptor = new TestFinishDescriptor(resultStatus, endedAt, message);
            registrar.finishTest(uid, testFinishDescriptor);
        }
    }

    public void registerTestSkipped(TestIdentifier testIdentifier, String reason) {
        if (testIdentifier.isTest()) {
            OffsetDateTime endedAt = OffsetDateTime.now();
            UniqueId uniqueId = UniqueId.parse(testIdentifier.getUniqueId());
            List<UniqueId.Segment> idSegments = uniqueId.getSegments();

//        JUnit5Test test = new JUnit5Test();
//        test.setUuid(UUID.randomUUID().toString());
//        test.setName(testIdentifier.getDisplayName());
//        test.setClassName(getSegmentValue(CLASS, idSegments));
//        test.setMessage(reason);
//        test.setResult(Result.SKIPPED);

            TestFinishDescriptor result = new TestFinishDescriptor(Status.SKIPPED, endedAt, reason);

            registrar.finishTest(testIdentifier.getUniqueId(), result);
        }
    }

    private String buildTestName(TestIdentifier testIdentifier, boolean hasManyInvocations) {
        String testName = testIdentifier.getDisplayName();
        if (hasManyInvocations) {
            testName = testIdentifier.getLegacyReportingName();
        } else if (testIdentifier.getDisplayName().equals(testIdentifier.getLegacyReportingName()) && testIdentifier.getSource().isPresent()) {
            testName = ((MethodSource) testIdentifier.getSource().get()).getMethodName();
        }
        return testName;
    }

    private String getSegmentValue(String type, List<UniqueId.Segment> segments) {
        return segments.stream()
                       .filter(segment -> segment.getType().equals(type))
                       .map(UniqueId.Segment::getValue)
                       .findAny()
                       .orElse(null);
    }

    private boolean containsSegment(String type, List<UniqueId.Segment> segments) {
        return segments.stream().anyMatch(segment -> segment.getType().equals(type));
    }

    private Class<?> getClassForName(String className) {
        Class<?> klass = null;
        if (className != null && className.length() != 0) {
            try {
                klass = Class.forName(className);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(String.format("Test class for name '%s' does not exist", className));
            }
        }
        return klass;
    }

}
