package com.zextras.metric.mailbox;

import com.zimbra.cs.stats.ZimbraPerf;
import io.prometheus.client.Collector;
import io.prometheus.client.CounterMetricFamily;
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
    final CounterMetricFamily soapHitMetricFamily =
        new CounterMetricFamily(
            "soap_api_count", "Number of SOAP APIs request", List.of("requestName"));
    final CounterMetricFamily soapDurationMetricFamily =
        new CounterMetricFamily(
            "soap_api_duration_avg",
            "Average duration of SOAP APIs request",
            List.of("requestName"));
    ZimbraPerf.SOAP_TRACKER_PROMETHEUS
        .getCounters()
        .forEach(
            (trackerName, counter) -> {
              soapHitMetricFamily.addMetric(List.of(trackerName), counter.getCount());
              soapDurationMetricFamily.addMetric(List.of(trackerName), counter.getAverage());
              counter.reset();
            });
    return List.of(soapHitMetricFamily, soapDurationMetricFamily);
  }
}
