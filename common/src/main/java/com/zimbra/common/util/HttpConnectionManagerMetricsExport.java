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

    /**
     * @return List<MetricFamilySamples>
     */
    @Override
    public List<MetricFamilySamples> collect() {
        List<MetricFamilySamples> mfs = new ArrayList<>();
        GaugeMetricFamily internalGaugeMetricFamily = new GaugeMetricFamily(
                "http_connection_pool_internal",
                "Metrics for internal pool",
                Collections.singletonList("pool"));
        PoolStats internalStats = null;

        ZimbraHttpConnectionManager internalHttpConnMgr = ZimbraHttpConnectionManager.getInternalHttpConnMgr();
        if (internalHttpConnMgr != null && internalHttpConnMgr.getHttpConnMgr() instanceof PoolingHttpClientConnectionManager) {
            internalStats = ((PoolingHttpClientConnectionManager) internalHttpConnMgr.getHttpConnMgr()).getTotalStats();
        }

        internalGaugeMetricFamily.addMetric(Collections.singletonList("available"), internalStats == null ? -1 :  internalStats.getAvailable());
        internalGaugeMetricFamily.addMetric(Collections.singletonList("leased"),  internalStats == null ? -1 :  internalStats.getLeased());
        internalGaugeMetricFamily.addMetric(Collections.singletonList("max"),  internalStats == null ? -1 :  internalStats.getMax());
        internalGaugeMetricFamily.addMetric(Collections.singletonList("pending"),  internalStats == null ? -1 :  internalStats.getPending());
        mfs.add(internalGaugeMetricFamily);
        GaugeMetricFamily externalGaugeMetricFamily = new GaugeMetricFamily(
                "http_connection_pool_external",
                "Metrics for external pool",
                Collections.singletonList("pool"));
        mfs.add(externalGaugeMetricFamily);

        PoolStats externalStats = null;
        ZimbraHttpConnectionManager externalHttpConnMgr = ZimbraHttpConnectionManager.getExternalHttpConnMgr();
        if (externalHttpConnMgr != null && externalHttpConnMgr.getHttpConnMgr() instanceof PoolingHttpClientConnectionManager) {
            externalStats = ((PoolingHttpClientConnectionManager) externalHttpConnMgr.getHttpConnMgr()).getTotalStats();
        }

        externalGaugeMetricFamily.addMetric(Collections.singletonList("available"), externalStats == null ? -1 :  externalStats.getAvailable());
        externalGaugeMetricFamily.addMetric(Collections.singletonList("leased"),  externalStats == null ? -1 :  externalStats.getLeased());
        externalGaugeMetricFamily.addMetric(Collections.singletonList("max"),  externalStats == null ? -1 :  externalStats.getMax());
        externalGaugeMetricFamily.addMetric(Collections.singletonList("pending"),  externalStats == null ? -1 :  externalStats.getPending());

        return mfs;
    }
}
