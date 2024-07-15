/*
 * SPDX-FileCopyrightText: 2024 Zextras <https://www.zextras.com>
 *
 * SPDX-License-Identifier: CC0-1.0
 */
package com.zimbra.common.util;

import com.zimbra.common.metric.CarbonioCollector;
import io.prometheus.client.GaugeMetricFamily;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.pool.PoolStats;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * For export HttpConnectionPools metrics
 *
 */
public class HttpConnectionManagerMetricsExport extends CarbonioCollector {

    private static final String POOL = "pool";

    /**
     * @return List<MetricFamilySamples>
     */
    @Override
    public List<MetricFamilySamples> collect() {
        List<MetricFamilySamples> mfs = new ArrayList<>();
        GaugeMetricFamily internalGaugeMetricFamily = getGaugeMetricFamily("internal");

        setMetrics(ZimbraHttpConnectionManager.getInternalHttpConnMgr(), internalGaugeMetricFamily);
        mfs.add(internalGaugeMetricFamily);

        GaugeMetricFamily externalGaugeMetricFamily = getGaugeMetricFamily("external");
        setMetrics(ZimbraHttpConnectionManager.getExternalHttpConnMgr(), externalGaugeMetricFamily);
        mfs.add(externalGaugeMetricFamily);
        return mfs;
    }

    /**
     * get Gauge Metric Family
     */
    GaugeMetricFamily getGaugeMetricFamily(String type) {
        return new GaugeMetricFamily(
                "http_connection_" + POOL + "_" + type,
                "Metrics for " + type + " " + POOL,
                Collections.singletonList(POOL));
    }

    /**
     * set metrics
     */
    void setMetrics(ZimbraHttpConnectionManager zimbraHttpConnectionManager, GaugeMetricFamily gaugeMetricFamily) {
        PoolStats poolStats = null;
        if (zimbraHttpConnectionManager != null && zimbraHttpConnectionManager.getHttpConnMgr() instanceof PoolingHttpClientConnectionManager) {
            poolStats = ((PoolingHttpClientConnectionManager) zimbraHttpConnectionManager.getHttpConnMgr()).getTotalStats();
        }
        if (poolStats == null) {
            return;
        }
        gaugeMetricFamily.addMetric(Collections.singletonList("available"), poolStats.getAvailable());
        gaugeMetricFamily.addMetric(Collections.singletonList("leased"), poolStats.getLeased());
        gaugeMetricFamily.addMetric(Collections.singletonList("max"), poolStats.getMax());
        gaugeMetricFamily.addMetric(Collections.singletonList("pending"), poolStats.getPending());
    }
}
