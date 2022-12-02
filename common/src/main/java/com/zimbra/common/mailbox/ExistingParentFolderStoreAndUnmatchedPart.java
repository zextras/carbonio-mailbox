// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.common.mailbox;

/**
 * For path e.g. "/foo/bar/baz/gub" where a mailbox has a Folder at "/foo/bar" but NOT one at "/foo/bar/baz"
 * this class can encapsulate this information where:
 *     parentFolderStore is the folder at path "/foo/bar"
 *     unmatchedPart = "baz/gub".
 */
public class ExistingParentFolderStoreAndUnmatchedPart {
    public FolderStore parentFolderStore;
    public String unmatchedPart;

    public ExistingParentFolderStoreAndUnmatchedPart(FolderStore fstore, String unmatched) {
        this.parentFolderStore = fstore;
        this.unmatchedPart = unmatched;
    }
}
