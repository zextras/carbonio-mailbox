package com.zextras.mailbox.domain.usecase.metric;

import com.zextras.mailbox.metric.Metrics;
import io.prometheus.client.Collector;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class MetricsTest {

    @Test
    void test_register() {
        Collector collector = Mockito.mock(Collector.class);
        Metrics.register(collector);
        Assertions.assertDoesNotThrow(() -> Metrics.COLLECTOR_REGISTRY.unregister(collector));
    }
}
