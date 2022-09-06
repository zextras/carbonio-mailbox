// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.base;

import com.zimbra.soap.type.AccountWithModifications;
import com.zimbra.soap.type.IdAndType;
import java.util.List;

public interface WaitSetResp {
  public void setCanceled(Boolean canceled);

  public void setSeqNo(String seqNo);

  public void setSignalledAccounts(Iterable<AccountWithModifications> signalledAccounts);

  public WaitSetResp addSignalledAccount(AccountWithModifications signalledAccount);

  public void setErrors(Iterable<IdAndType> errors);

  public WaitSetResp addError(IdAndType error);

  public String getWaitSetId();

  public Boolean getCanceled();

  public String getSeqNo();

  public List<AccountWithModifications> getSignalledAccounts();

  public List<IdAndType> getErrors();

  public void setWaitSetId(String waitSetId);
}
