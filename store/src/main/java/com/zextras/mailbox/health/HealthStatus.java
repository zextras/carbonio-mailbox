// SPDX-FileCopyrightText: 2023 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zextras.mailbox.health;

/**
 * Contract for checking the health status of a service.
 */
public interface HealthStatus {

  /**
   * Checks if the service is ready to process requests.
   *
   * @return {@code true} if the service is ready to receive requests, otherwise {@code false}
   */
  boolean isReady();

  /**
   * Checks if the service is live and responsive. If the liveliness check fails, it indicates that
   * the service is unhealthy or dead and should be restarted.
   *
   * @return {@code true} if the service is healthy and responsive, otherwise {@code false}
   */
  boolean isLive();
}

