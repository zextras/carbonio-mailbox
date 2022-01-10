// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.qa.unittest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.runner.Description;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;

public class TestResults extends RunListener {
    public enum TestStatus {SUCCESS,FAILURE,SKIPPED,IGNORED};
    private long lastTestStartTime;
    private List<String> errorMessages; // Support multiple errors if using ErrorCollector
    private List<Result> results = Lists.newArrayList();
    private Map<String,TestStatus> statusMap = new HashMap<String,TestStatus>();

    public static class Result {
        public final String className;
        public final String methodName;
        public final long execMillis;
        public final String errorMessage;
        public final TestStatus status;

        private Result(String className, String methodName, long execMillis, String errorMessage, TestStatus status) {
            this.className = className;
            this.methodName = methodName;
            this.execMillis = execMillis;
            this.errorMessage = errorMessage;
            this.status = status;
        }
    }

    public List<Result> getResults(TestStatus s) {
        List<Result> list = Lists.newArrayList();
        for (Result result : this.results) {
            if (result.status == s) {
                list.add(result);
            }
        }
        return list;
    }

    @Override
    public void testStarted(Description description) throws Exception {
        lastTestStartTime = System.currentTimeMillis();
        errorMessages = Lists.newArrayListWithExpectedSize(1);
        statusMap.put(description.getClassName(), TestStatus.SUCCESS);
    }

    @Override
    public void testFinished(Description desc) throws Exception {
        String errs;
        if (errorMessages.isEmpty()) {
            errs = null;
        } else {
            errs = Joiner.on('\n').join(errorMessages);
        }
        results.add(new Result(desc.getClassName(), desc.getMethodName(),
            System.currentTimeMillis() - lastTestStartTime, errs, statusMap.get(desc.getClassName())));
    }

    @Override
    public void testFailure(Failure failure) throws Exception {
        String className = failure.getDescription().getClassName();
        statusMap.put(className, TestStatus.FAILURE);
        errorMessages.add(failure.getMessage());
    }

    @Override
    public void testAssumptionFailure(Failure failure) {
        String className = failure.getDescription().getClassName();
        statusMap.put(className, TestStatus.SKIPPED);
        errorMessages.add(failure.getMessage());
    }
}