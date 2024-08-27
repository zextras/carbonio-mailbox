package com.zimbra.common.util;

import io.prometheus.client.GaugeMetricFamily;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;


class HttpConnectionManagerMetricsExportTest {

    private HttpConnectionManagerMetricsExport export;

    @BeforeEach
    void setUp() {
        export = new HttpConnectionManagerMetricsExport();
    }
    @Test
    void test_collect() {
        Assertions.assertDoesNotThrow(() ->export.collect());
    }

    @Test
    void test_setMetrics_when_zimbraHttpConnectionManager_is_null_then_do_nothing() {
        GaugeMetricFamily gaugeMetricFamily = Mockito.mock(GaugeMetricFamily.class);
        export.setMetrics(null, gaugeMetricFamily);
        Mockito.verify(gaugeMetricFamily, Mockito.times(0))
                .addMetric(Mockito.anyList(), Mockito.anyInt());
    }

    @Test
    void test_setMetrics_when_zimbraHttpConnectionManager_is_not_instanceof_PoolingHttpClientConnectionManager_then_do_nothing() {
        GaugeMetricFamily gaugeMetricFamily = Mockito.mock(GaugeMetricFamily.class);
        ZimbraHttpConnectionManager zimbraHttpConnectionManager = Mockito.mock(ZimbraHttpConnectionManager.class);
        export.setMetrics(zimbraHttpConnectionManager, gaugeMetricFamily);
        Mockito.verify(gaugeMetricFamily, Mockito.times(0))
                .addMetric(Mockito.anyList(), Mockito.anyInt());
    }
}
