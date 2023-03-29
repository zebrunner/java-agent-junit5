package com.zebrunner.agent.junit5.core;

import com.google.gson.Gson;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@RequiredArgsConstructor
public class TestCorrelationData {

    private static final Gson GSON = new Gson();

    private final String thread = Thread.currentThread().getName();

    private final String uniqueId;
    private final String displayName;

    @Override
    public String toString() {
        List<Object> buildParameters = new ArrayList<>(2);
        buildParameters.add(Thread.currentThread().getName());
        buildParameters.add(uniqueId);

        return String.format("[%s]: %s", buildParameters.toArray());
    }

    public String asJsonString() {
        return GSON.toJson(this);
    }

    public static TestCorrelationData fromJsonString(String json) {
        return GSON.fromJson(json, TestCorrelationData.class);
    }

}
