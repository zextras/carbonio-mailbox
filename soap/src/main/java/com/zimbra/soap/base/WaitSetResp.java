// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.base;

import java.util.List;

import com.zimbra.soap.type.AccountWithModifications;
import com.zimbra.soap.type.IdAndType;

public interface WaitSetResp {
    void setCanceled(Boolean canceled);
    void setSeqNo(String seqNo);
    void setSignalledAccounts(Iterable<AccountWithModifications> signalledAccounts);
    WaitSetResp addSignalledAccount(AccountWithModifications signalledAccount);
    void setErrors(Iterable<IdAndType> errors);
    WaitSetResp addError(IdAndType error);
    String getWaitSetId();
    Boolean getCanceled();
    String getSeqNo();
    List<AccountWithModifications> getSignalledAccounts();
    List<IdAndType> getErrors();
    void setWaitSetId(String waitSetId);
}
