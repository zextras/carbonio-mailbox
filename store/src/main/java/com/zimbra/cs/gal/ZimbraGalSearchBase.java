// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.gal;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.Domain;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.gal.GalOp;

public class ZimbraGalSearchBase {

  public static enum PredefinedSearchBase {
    DOMAIN,
    SUBDOMAINS,
    ROOT;
  };

  public static String getSearchBaseRaw(Domain domain, GalOp galOp) throws ServiceException {
    String sb;
    if (galOp == GalOp.sync) {
      sb = domain.getAttr(Provisioning.A_zimbraGalSyncInternalSearchBase);
      if (sb == null) {
        sb =
            domain.getAttr(
                Provisioning.A_zimbraGalInternalSearchBase, PredefinedSearchBase.DOMAIN.name());
      }
    } else {
      sb =
          domain.getAttr(
              Provisioning.A_zimbraGalInternalSearchBase, PredefinedSearchBase.DOMAIN.name());
    }
    return sb;
  }

  public static String getSearchBase(Domain domain, GalOp galOp) throws ServiceException {
    String sb = getSearchBaseRaw(domain, galOp);
    return domain.getGalSearchBase(sb);
  }
}
