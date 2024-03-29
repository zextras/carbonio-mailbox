// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.service.mail;

import java.util.ArrayList;
import java.util.List;

public class DeleteActionResult extends ItemActionResult {

    protected List<String> mNonExistentIds;

    public DeleteActionResult() {
        mNonExistentIds = new ArrayList<>();
    }

    public DeleteActionResult(List<String> ids, List<String> nonExistentIds) {
        super();
        mNonExistentIds = nonExistentIds;
        setSuccessIds(ids);
    }

    public List<String> getNonExistentIds() {
        return mNonExistentIds;
    }

    public void setNonExistentIds(List<String> mNonExistentIds) {
        this.mNonExistentIds = mNonExistentIds;
    }

    public void appendNonExistentIds(List<String> nonExistentIds) {
        this.mNonExistentIds.addAll(nonExistentIds);
    }

    public void appendNonExistentId(String nonExistentId) {
        this.mNonExistentIds.add(nonExistentId);
    }

    public void appendNonExistentIds(ItemActionResult iar) {
        if (iar instanceof DeleteActionResult) {
            appendNonExistentIds(((DeleteActionResult)iar).getNonExistentIds());
        }
    }
}
