/*
 * SPDX-FileCopyrightText: 2026 Zextras <https://www.zextras.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.zextras.mailbox.api.rest.service;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.Cos;
import com.zimbra.cs.account.Provisioning;
import io.vavr.control.Try;
import java.util.function.Supplier;

public class CosService {

  private final Supplier<Provisioning> provisioningSupplier;

  public CosService(Supplier<Provisioning> provisioningSupplier) {
    this.provisioningSupplier = provisioningSupplier;
  }

  /** Fails with {@code NOT_FOUND} when no COS carries that id. */
  public Try<Cos> getCos(String cosId) {
    return Try.of(() -> {
      final Cos cos = provisioningSupplier.get().getCosById(cosId);
      if (cos == null) {
        throw ServiceException.NOT_FOUND("No such COS with ID: " + cosId);
      }
      return cos;
    });
  }
}
