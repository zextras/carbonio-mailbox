// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.service.mail;

import java.util.List;

import com.google.common.collect.Lists;

public class CopyActionResult extends ItemActionResult {

    protected final List<String> mCreatedIds = Lists.newArrayList();

    public CopyActionResult() {
        super();
    }

    public CopyActionResult(List<String> ids, List<String> createdIds) {
        super();
        setSuccessIds(ids);
    }

    public List<String> getCreatedIds() {
        return mCreatedIds;
    }

    public void addCreatedId(String createdId) {
        this.mCreatedIds.add(createdId);
    }

    public void setCreatedIds(List<String> createdIds) {
        this.mCreatedIds.clear();
        appendCreatedIds(createdIds);
    }

    public void appendCreatedIds(List<String> createdIds) {
        this.mCreatedIds.addAll(createdIds);
    }

    public void appendCreatedIds(ItemActionResult iar) {
        if (iar instanceof CopyActionResult) {
            appendCreatedIds(((CopyActionResult)iar).getCreatedIds());
        }
    }
}
