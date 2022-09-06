// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.base;

import com.zimbra.soap.type.WaitSetAddSpec;
import java.util.List;

public interface CreateWaitSetReq {
  public void setAccounts(Iterable<WaitSetAddSpec> accounts);

  public CreateWaitSetReq addAccount(WaitSetAddSpec account);

  public String getDefaultInterests();

  public Boolean getAllAccounts();

  public List<WaitSetAddSpec> getAccounts();
}
