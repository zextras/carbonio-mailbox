// SPDX-FileCopyrightText: 2023 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: AGPL-3.0-only

package com.zextras.mailbox.tracing;

import com.google.inject.AbstractModule;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator;
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.exporter.jaeger.JaegerGrpcSpanExporter;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor;
import io.opentelemetry.sdk.trace.samplers.Sampler;
import io.opentelemetry.semconv.resource.attributes.ResourceAttributes;

/**
 * Provides a {@link OpenTelemetry} instance and registers it globally.
 *
 * @author davidefrison
 */
public class MailboxTracingModule extends AbstractModule {

  public static final String SERVICE_NAME = "mailbox";

  @Override
  protected void configure() {
    super.configure();
    Resource resource = Resource.getDefault()
        .merge(Resource.create(Attributes.of(ResourceAttributes.SERVICE_NAME, SERVICE_NAME)));
    JaegerGrpcSpanExporter jaegerExporter = JaegerGrpcSpanExporter.builder()
        .build();
    SdkTracerProvider sdkTracerProvider = SdkTracerProvider.builder()
        .addSpanProcessor(BatchSpanProcessor.builder(jaegerExporter).build())
        .setResource(resource)
        .setSampler(Sampler.alwaysOn())
        .build();
    bind(OpenTelemetry.class).toInstance(OpenTelemetrySdk.builder()
        .setTracerProvider(sdkTracerProvider)
        .setPropagators(ContextPropagators.create(W3CTraceContextPropagator.getInstance()))
        .buildAndRegisterGlobal());
  }
}
