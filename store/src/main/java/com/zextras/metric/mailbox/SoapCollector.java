package com.zextras.metric.mailbox;

import com.zimbra.cs.stats.ZimbraPerf;
import io.prometheus.client.Collector;
import io.prometheus.client.CounterMetricFamily;
import java.util.Arrays;
import java.util.List;

public class SoapCollector extends Collector {

  @Override
  public List<MetricFamilySamples> collect() {
    final CounterMetricFamily soapCounterMetricFamily =
        new CounterMetricFamily(
            "soap_api_counter", "Number of SOAP APIs request", List.of("requestName"));
    // TODO: register for each API
    soapCounterMetricFamily.addMetric(
        List.of("AuthRequest"),
        ZimbraPerf.SOAP_TRACKER_PROMETHEUS.getCounter("AuthRequest").getCount());
    return Arrays.asList(soapCounterMetricFamily);
  }
}
