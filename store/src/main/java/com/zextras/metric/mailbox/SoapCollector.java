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
            "soap_exec_count", "Number of SOAP APIs request", List.of("command"));
    final CounterMetricFamily soapDurationMetricFamily =
        new CounterMetricFamily(
            "soap_exec_ms_avg", "Average duration of SOAP APIs request in ms", List.of("command"));
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
