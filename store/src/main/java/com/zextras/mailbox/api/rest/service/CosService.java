/*
 * SPDX-FileCopyrightText: 2026 Zextras <https://www.zextras.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.zextras.mailbox.api.rest.service;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.Cos;
import com.zimbra.cs.account.Domain;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.Provisioning.CountAccountResult.CountAccountByCos;
import io.vavr.control.Try;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class CosService {

  private final Supplier<Provisioning> provisioningSupplier;

  public CosService(Supplier<Provisioning> provisioningSupplier) {
    this.provisioningSupplier = provisioningSupplier;
  }

  public Try<Cos> getCos(String cosId) {
    return Try.of(() -> {
      final Cos cos = provisioningSupplier.get().getCosById(cosId);
      if (cos == null) {
        throw ServiceException.NOT_FOUND("No such COS with ID: " + cosId);
      }
      return cos;
    });
  }

  public Try<Map<String, Long>> countCos(Collection<String> cosIds) {
    return Try.of(() -> {
      final Provisioning provisioning = provisioningSupplier.get();
      final Map<String, Long> counts = new LinkedHashMap<>();
      for (final String cosId :
          cosIds == null || cosIds.isEmpty() ? allCosIds(provisioning) : cosIds) {
        counts.put(cosId, 0L);
      }

      for (final Domain domain : provisioning.getAllDomains()) {
        for (final CountAccountByCos countByCos :
            provisioning.countAccount(domain).getCountAccountByCos()) {
          counts.computeIfPresent(
              countByCos.getCosId(), (id, count) -> count + countByCos.getCount());
        }
      }
      return counts;
    });
  }

  private static List<String> allCosIds(Provisioning provisioning) throws ServiceException {
    return provisioning.getAllCos().stream().map(Cos::getId).toList();
  }
}
