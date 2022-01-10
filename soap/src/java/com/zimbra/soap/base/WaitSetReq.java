// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.base;

import java.util.List;

import com.zimbra.soap.type.Id;
import com.zimbra.soap.type.WaitSetAddSpec;

public interface WaitSetReq {
    public void setBlock(Boolean block);
    public void setDefaultInterests(String defaultInterests);
    public void setTimeout(Long timeout);
    public void setAddAccounts(Iterable <WaitSetAddSpec> addAccounts);
    public WaitSetReq addAddAccount(WaitSetAddSpec addAccount);
    public void setUpdateAccounts(Iterable <WaitSetAddSpec> updateAccounts);
    public WaitSetReq addUpdateAccount(WaitSetAddSpec updateAccount);
    public void setRemoveAccounts(Iterable <Id> removeAccounts);
    public WaitSetReq addRemoveAccount(Id removeAccount);
    public String getWaitSetId();
    public String getLastKnownSeqNo();
    public Boolean getBlock();
    public String getDefaultInterests();
    public Long getTimeout();
    public List<WaitSetAddSpec> getAddAccounts();
    public List<WaitSetAddSpec> getUpdateAccounts();
    public List<Id> getRemoveAccounts();
    public boolean getExpand();
    public void setExpand(Boolean expand);
}
