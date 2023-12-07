// SPDX-FileCopyrightText: 2023 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: AGPL-3.0-only

package com.zextras.mailbox.health;

public interface HealthCheck {

  /**
   * A readiness signal indicates that an application is ready to process requests.
   *
   * @return true if the application is ready to receive requests otherwise false
   */
  boolean isReady();


  /**
   * The liveliness endpoint is used to check if an application is running and responsive If the
   * liveliness check fails, it means that the application is unhealthy or dead and should be
   * restarted.
   *
   * @return true if the application is healthy and responsive otherwise false
   */
  boolean isLive();

}
