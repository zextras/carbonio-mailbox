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
import java.util.Locale;
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
      final Collection<String> requestedIds =
          cosIds == null || cosIds.isEmpty() ? allCosIds(provisioning) : cosIds;

      final Map<String, String> requestedByLowercase = new LinkedHashMap<>();
      final Map<String, Long> counts = new LinkedHashMap<>();
      for (final String cosId : requestedIds) {
        requestedByLowercase.put(cosId.toLowerCase(Locale.ROOT), cosId);
        counts.put(cosId, 0L);
      }

      for (final Domain domain : provisioning.getAllDomains()) {
        for (final CountAccountByCos countByCos :
            provisioning.countAccount(domain).getCountAccountByCos()) {
          final String cosId = countByCos.getCosId();
          if (cosId == null) {
            continue;
          }
          final String requested = requestedByLowercase.get(cosId.toLowerCase(Locale.ROOT));
          if (requested != null) {
            counts.merge(requested, countByCos.getCount(), Long::sum);
          }
        }
      }
      return counts;
    });
  }

  private static List<String> allCosIds(Provisioning provisioning) throws ServiceException {
    return provisioning.getAllCos().stream().map(Cos::getId).toList();
  }
}
