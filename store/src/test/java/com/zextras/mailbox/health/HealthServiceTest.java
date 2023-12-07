// SPDX-FileCopyrightText: 2023 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: AGPL-3.0-only

package com.zextras.mailbox.health;

import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class HealthServiceTest {

  private final HealthCheck healthCheck1 = Mockito.mock(HealthCheck.class);
  private final HealthCheck healthCheck2 = Mockito.mock(HealthCheck.class);
  private final HealthService healthService = new HealthService(List.of(healthCheck1, healthCheck2));

  @Test
  void shouldReturnFalseWhenOneHealthCheckFailing() {
    Mockito.when(healthCheck1.isReady()).thenReturn(true);
    Mockito.when(healthCheck2.isReady()).thenReturn(false);
    Assertions.assertFalse(healthService.isReady());
  }

  @Test
  void shouldReturnTrueWhenAllOk() {
    Mockito.when(healthCheck1.isReady()).thenReturn(true);
    Mockito.when(healthCheck2.isReady()).thenReturn(true);
    Assertions.assertTrue(healthService.isReady());
  }

}