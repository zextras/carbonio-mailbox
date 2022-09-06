// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.account;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.auth.twofactor.AppSpecificPasswordData;

public interface AppSpecificPassword {
  public void store() throws ServiceException;

  public void update() throws ServiceException;

  public String getName();

  public String getPassword();

  public void setDateLastUsed(Long date);

  public void setDateCreated(Long date);

  public Long getDateLastUsed();

  public Long getDateCreated();

  public boolean validate(String providedPassword) throws ServiceException;

  public void revoke() throws ServiceException;

  public AppSpecificPasswordData getPasswordData();

  public boolean isExpired();
}
