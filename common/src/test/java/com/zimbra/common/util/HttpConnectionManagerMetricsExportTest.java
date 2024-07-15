package com.zimbra.common.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class HttpConnectionManagerMetricsExportTest {
    @Test
    void test_collect() {
        HttpConnectionManagerMetricsExport export = new HttpConnectionManagerMetricsExport();
        Assertions.assertDoesNotThrow(() ->export.collect());
    }
}
