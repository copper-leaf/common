package com.eden.common.processor;

import com.eden.orchid.api.options.annotations.Option;
import org.junit.jupiter.api.Test;

public class TestOptionsExtractorProcessor {

    @Option
    public String testField;

    @Option("somethingElseEntirely")
    public int testField2;

    @Option("somethingElseEntirely2")
    public int testField3;

    @Option("somethingElseEntirely2")
    public String testField4;

    @Test
    public void testProcessor() {

    }

    public void setTestField2(int testField2) {

    }

    public void setSomethingElseEntirely(int testField2) {

    }
}
