// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.type;

public interface SearchHit {
    void setId(String id);
    void setSortField(String sortField);
    String getId();
    String getSortField();
}
