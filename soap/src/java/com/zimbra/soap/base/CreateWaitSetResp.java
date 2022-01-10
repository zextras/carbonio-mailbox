// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.base;

import java.util.List;

import com.zimbra.soap.type.IdAndType;

public interface CreateWaitSetResp {
    public void setErrors(Iterable <IdAndType> errors);
    public CreateWaitSetResp addError(IdAndType error);
    public List<IdAndType> getErrors();
    public CreateWaitSetResp setWaitSetId(String wsid);
    public String getWaitSetId();
    public CreateWaitSetResp setDefaultInterests(String defInterests);
    public String getDefaultInterests();
    public CreateWaitSetResp setSequence(int seq);
    public int getSequence();
}
