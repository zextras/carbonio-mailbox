// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.common.mailbox;

public interface SearchFolderStore extends FolderStore {
    String getQuery();
    /** Returns the set of item types returned by this search, or <code>""</code> if none were specified. */
    String getReturnTypes();
}
