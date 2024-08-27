package com.zextras.mailbox.servlet;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class MetricsServletModuleTest {

    @Test
    void test_provideCollector() {
        MetricsServletModule module = new MetricsServletModule();
        Assertions.assertNotNull(module.provideCollector());
    }
}
