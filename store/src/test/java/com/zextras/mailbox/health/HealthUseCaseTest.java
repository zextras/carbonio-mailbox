// SPDX-FileCopyrightText: 2023 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: AGPL-3.0-only

package com.zextras.mailbox.health;

import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class HealthUseCaseTest {

  private final ServiceDependency serviceDependency = Mockito.mock(ServiceDependency.class);
  private final ServiceDependency serviceDependency2 = Mockito.mock(ServiceDependency.class);

  private final HealthUseCase healthService =
      new HealthUseCase(List.of(serviceDependency, serviceDependency2));

  @Test
  void shouldBeReadyWithoutDependencies() {
    HealthUseCase healthService = new HealthUseCase(List.of());
    Assertions.assertTrue(healthService.isReady());
  }

  @Test
  void shouldNotBeReadyWhenOneHealthCheckFailing() {
    Mockito.when(serviceDependency.isReady()).thenReturn(true);
    Mockito.when(serviceDependency2.isReady()).thenReturn(false);

    Assertions.assertFalse(healthService.isReady());
  }

  @Test
  void shouldNotBeReadyWhenAllHealthCheckFailing() {
    Mockito.when(serviceDependency.isReady()).thenReturn(false);
    Mockito.when(serviceDependency2.isReady()).thenReturn(false);

    Assertions.assertFalse(healthService.isReady());
  }

  @Test
  void shouldBeReadyWhenAllOk() {
    Mockito.when(serviceDependency.isReady()).thenReturn(true);
    Mockito.when(serviceDependency2.isReady()).thenReturn(true);

    Assertions.assertTrue(healthService.isReady());
  }

  @Test
  void shouldNotBeLiveWhenOneHealthCheckFailing() {
    Mockito.when(serviceDependency.isLive()).thenReturn(true);
    Mockito.when(serviceDependency2.isLive()).thenReturn(false);

    Assertions.assertFalse(healthService.isLive());
  }

  @Test
  void shouldNotBeLiveWhenAllHealthCheckFailing() {
    Mockito.when(serviceDependency.isLive()).thenReturn(false);
    Mockito.when(serviceDependency2.isLive()).thenReturn(false);

    Assertions.assertFalse(healthService.isLive());
  }

  @Test
  void shouldBeLiveWhenAllOk() {
    Mockito.when(serviceDependency.isLive()).thenReturn(true);
    Mockito.when(serviceDependency2.isLive()).thenReturn(true);

    Assertions.assertTrue(healthService.isLive());
  }

  @Test
  void produceDependencyHealthSummary() {
    Mockito.when(serviceDependency.getName()).thenReturn("service1");
    Mockito.when(serviceDependency.getType()).thenReturn(ServiceDependency.ServiceType.REQUIRED);
    Mockito.when(serviceDependency.isReady()).thenReturn(true);
    Mockito.when(serviceDependency.isLive()).thenReturn(true);

    Mockito.when(serviceDependency2.getName()).thenReturn("service2");
    Mockito.when(serviceDependency2.getType()).thenReturn(ServiceDependency.ServiceType.OPTIONAL);
    Mockito.when(serviceDependency2.isReady()).thenReturn(false);
    Mockito.when(serviceDependency2.isLive()).thenReturn(false);

    final var summary = healthService.dependenciesHealthSummary();

    Assertions.assertEquals(2, summary.size());
    assertDependencyHealthResult(serviceDependency, summary.get(0));
    assertDependencyHealthResult(serviceDependency2, summary.get(1));
  }

  private static void assertDependencyHealthResult(
      ServiceDependency expected, DependencyHealthResult actual) {
    Assertions.assertEquals(expected.getName(), actual.getName());
    Assertions.assertEquals(expected.getType(), actual.getType());
    Assertions.assertEquals(expected.isReady(), actual.isReady());
    Assertions.assertEquals(expected.isLive(), actual.isLive());
  }
}
