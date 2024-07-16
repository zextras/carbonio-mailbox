/*
 * SPDX-FileCopyrightText: 2024 Zextras <https://www.zextras.com>
 *
 * SPDX-License-Identifier: CC0-1.0
 */
package com.zextras.mailbox.metric;

import com.zimbra.common.util.HttpConnectionManagerMetricsExport;
import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.hotspot.DefaultExports;

/**
 * For register DefaultExports and project specific exports
 */
public final class CarbonioMetricRegisterer {

    private CarbonioMetricRegisterer() {}
    /**
     *
     * @param registry CollectorRegistry
     */
    public static void register(CollectorRegistry registry) {
        DefaultExports.register(registry);
        registry.register(new HttpConnectionManagerMetricsExport());
    }
}
