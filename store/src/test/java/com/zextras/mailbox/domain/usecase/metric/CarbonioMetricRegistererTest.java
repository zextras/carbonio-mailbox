package com.zextras.mailbox.domain.usecase.metric;

import com.zextras.mailbox.metric.CarbonioMetricRegisterer;
import com.zimbra.common.util.HttpConnectionManagerMetricsExport;
import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.hotspot.BufferPoolsExports;
import io.prometheus.client.hotspot.ClassLoadingExports;
import io.prometheus.client.hotspot.GarbageCollectorExports;
import io.prometheus.client.hotspot.MemoryAllocationExports;
import io.prometheus.client.hotspot.MemoryPoolsExports;
import io.prometheus.client.hotspot.StandardExports;
import io.prometheus.client.hotspot.ThreadExports;
import io.prometheus.client.hotspot.VersionInfoExports;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class CarbonioMetricRegistererTest {


    @Test
    void test_register_registers_StandardExports() {
        CollectorRegistry collectorRegistry = Mockito.mock(CollectorRegistry.class);
        CarbonioMetricRegisterer.register(collectorRegistry);
        Mockito.verify(collectorRegistry, Mockito.times(1))
                .register(Mockito.any(StandardExports.class));

    }

    @Test
    void test_register_registers_MemoryPoolsExports() {
        CollectorRegistry collectorRegistry = Mockito.mock(CollectorRegistry.class);
        CarbonioMetricRegisterer.register(collectorRegistry);
        Mockito.verify(collectorRegistry, Mockito.times(1))
                .register(Mockito.any(MemoryPoolsExports.class));
    }

    @Test
    void test_register_registers_MemoryAllocationExports() {
        CollectorRegistry collectorRegistry = Mockito.mock(CollectorRegistry.class);
        CarbonioMetricRegisterer.register(collectorRegistry);
        Mockito.verify(collectorRegistry, Mockito.times(1))
                .register(Mockito.any(MemoryAllocationExports.class));
    }

    @Test
    void test_register_registers_BufferPoolsExports() {
        CollectorRegistry collectorRegistry = Mockito.mock(CollectorRegistry.class);
        CarbonioMetricRegisterer.register(collectorRegistry);
        Mockito.verify(collectorRegistry, Mockito.times(1))
                .register(Mockito.any(BufferPoolsExports.class));
    }

    @Test
    void test_register_registers_GarbageCollectorExports() {
        CollectorRegistry collectorRegistry = Mockito.mock(CollectorRegistry.class);
        CarbonioMetricRegisterer.register(collectorRegistry);
        Mockito.verify(collectorRegistry, Mockito.times(1))
                .register(Mockito.any(GarbageCollectorExports.class));
    }

    @Test
    void test_register_registers_ThreadExports() {
        CollectorRegistry collectorRegistry = Mockito.mock(CollectorRegistry.class);
        CarbonioMetricRegisterer.register(collectorRegistry);
        Mockito.verify(collectorRegistry, Mockito.times(1))
                .register(Mockito.any(ThreadExports.class));
    }

    @Test
    void test_register_registers_ClassLoadingExports() {
        CollectorRegistry collectorRegistry = Mockito.mock(CollectorRegistry.class);
        CarbonioMetricRegisterer.register(collectorRegistry);
        Mockito.verify(collectorRegistry, Mockito.times(1))
                .register(Mockito.any(ClassLoadingExports.class));
    }

    @Test
    void test_register_registers_VersionInfoExports() {
        CollectorRegistry collectorRegistry = Mockito.mock(CollectorRegistry.class);
        CarbonioMetricRegisterer.register(collectorRegistry);
        Mockito.verify(collectorRegistry, Mockito.times(1))
                .register(Mockito.any(VersionInfoExports.class));
    }

    @Test
    void test_register_registers_HttpConnectionManagerMetricsExport() {
        CollectorRegistry collectorRegistry = Mockito.mock(CollectorRegistry.class);
        CarbonioMetricRegisterer.register(collectorRegistry);
        Mockito.verify(collectorRegistry, Mockito.times(1))
                .register(Mockito.any(HttpConnectionManagerMetricsExport.class));
    }
}
