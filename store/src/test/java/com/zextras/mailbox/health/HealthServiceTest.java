// SPDX-FileCopyrightText: 2023 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: AGPL-3.0-only

package com.zextras.mailbox.health;

import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class HealthServiceTest {

  private final ServiceDependency serviceDependency = Mockito.mock(ServiceDependency.class);
  private final ServiceDependency serviceDependency2 = Mockito.mock(ServiceDependency.class);

  private final HealthService healthService = new HealthService(
      List.of(serviceDependency, serviceDependency2));

  @Test
  void readyShouldReturnFalseWhenOneHealthCheckFailing() {
    Mockito.when(serviceDependency.isReady()).thenReturn(true);
    Mockito.when(serviceDependency2.isReady()).thenReturn(false);

    Assertions.assertFalse(healthService.isReady());
  }

  @Test
  void readyShouldReturnFalseWhenAllHealthCheckFailing() {
    Mockito.when(serviceDependency.isReady()).thenReturn(false);
    Mockito.when(serviceDependency2.isReady()).thenReturn(false);

    Assertions.assertFalse(healthService.isReady());
  }

  @Test
  void readyShouldReturnTrueWhenAllOk() {
    Mockito.when(serviceDependency.isReady()).thenReturn(true);
    Mockito.when(serviceDependency2.isReady()).thenReturn(true);

    Assertions.assertTrue(healthService.isReady());
  }

  @Test
  void liveShouldReturnFalseWhenOneHealthCheckFailing() {
    Mockito.when(serviceDependency.isLive()).thenReturn(true);
    Mockito.when(serviceDependency2.isLive()).thenReturn(false);

    Assertions.assertFalse(healthService.isLive());
  }

  @Test
  void liveShouldReturnFalseWhenAllHealthCheckFailing() {
    Mockito.when(serviceDependency.isLive()).thenReturn(false);
    Mockito.when(serviceDependency2.isLive()).thenReturn(false);

    Assertions.assertFalse(healthService.isLive());
  }

  @Test
  void liveShouldReturnTrueWhenAllOk() {
    Mockito.when(serviceDependency.isLive()).thenReturn(true);
    Mockito.when(serviceDependency2.isLive()).thenReturn(true);

    Assertions.assertTrue(healthService.isLive());
  }

}