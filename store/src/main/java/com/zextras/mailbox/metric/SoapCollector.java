/*
 * SPDX-FileCopyrightText: 2023 Zextras <https://www.zextras.com>
 *
 * SPDX-License-Identifier: CC0-1.0
 */

package com.zextras.mailbox.metric;

import com.zimbra.cs.stats.ZimbraPerf;
import io.prometheus.client.Collector;
import io.prometheus.client.SummaryMetricFamily;
import java.util.List;

public class SoapCollector extends Collector {

  /**
   * Collects metrics about number of calls and average duration for SOAP APIs. It also resets the
   * counters after collection.
   *
   * @return a list of soap hit and duration metrics
   */
  @Override
  public List<MetricFamilySamples> collect() {
    final SummaryMetricFamily soapHitMetricFamily =
        new SummaryMetricFamily(
            "soap_exec_ms",
            "Summary of SOAP APIs request duration in millisecond",
            List.of("command"));
    ZimbraPerf.SOAP_TRACKER_PROMETHEUS
        .getCounters()
        .forEach(
            (trackerName, counter) -> {
              soapHitMetricFamily.addMetric(
                  List.of(trackerName), counter.getCount(), counter.getTotal());
            });
    return List.of(soapHitMetricFamily);
  }
}
