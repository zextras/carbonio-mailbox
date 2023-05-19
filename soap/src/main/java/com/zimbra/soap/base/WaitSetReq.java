// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.base;

import java.util.List;

import com.zimbra.soap.type.Id;
import com.zimbra.soap.type.WaitSetAddSpec;

public interface WaitSetReq {
    void setBlock(Boolean block);
    void setDefaultInterests(String defaultInterests);
    void setTimeout(Long timeout);
    void setAddAccounts(Iterable<WaitSetAddSpec> addAccounts);
    WaitSetReq addAddAccount(WaitSetAddSpec addAccount);
    void setUpdateAccounts(Iterable<WaitSetAddSpec> updateAccounts);
    WaitSetReq addUpdateAccount(WaitSetAddSpec updateAccount);
    void setRemoveAccounts(Iterable<Id> removeAccounts);
    WaitSetReq addRemoveAccount(Id removeAccount);
    String getWaitSetId();
    String getLastKnownSeqNo();
    Boolean getBlock();
    String getDefaultInterests();
    Long getTimeout();
    List<WaitSetAddSpec> getAddAccounts();
    List<WaitSetAddSpec> getUpdateAccounts();
    List<Id> getRemoveAccounts();
    boolean getExpand();
    void setExpand(Boolean expand);
}
