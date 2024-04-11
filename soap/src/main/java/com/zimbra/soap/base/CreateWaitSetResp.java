// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.base;

import java.util.List;

import com.zimbra.soap.type.IdAndType;

public interface CreateWaitSetResp {
    void setErrors(Iterable<IdAndType> errors);
    CreateWaitSetResp addError(IdAndType error);
    List<IdAndType> getErrors();
    CreateWaitSetResp setWaitSetId(String wsid);
    String getWaitSetId();
    CreateWaitSetResp setDefaultInterests(String defInterests);
    String getDefaultInterests();
    CreateWaitSetResp setSequence(int seq);
    int getSequence();
}
