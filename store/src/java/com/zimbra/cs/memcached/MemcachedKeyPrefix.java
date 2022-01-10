// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.memcached;

/**
 * List of all memcached key prefixes used by ZCS
 */
public final class MemcachedKeyPrefix {

    private static final String DELIMITER = ":";

    public static final String CALENDAR_LIST        = "zmCalsList" + DELIMITER;
    public static final String CTAGINFO             = "zmCtagInfo" + DELIMITER;
    public static final String CALDAV_CTAG_RESPONSE = "zmCtagResp" + DELIMITER;
    public static final String CAL_SUMMARY          = "zmCalSumry" + DELIMITER;

    public static final String EFFECTIVE_FOLDER_ACL = "zmEffFolderACL" + DELIMITER;

    public static final String MBOX_FOLDERS_TAGS    = "zmFldrsTags" + DELIMITER;

    public static final String MBOX_MAILITEM        = "zmMailItem" + DELIMITER;

    public static final String IMAP                 = "zmImap" + DELIMITER;

    public static final String WATCHED_ITEMS        = "zmWatch" + DELIMITER;

    public static final String SYNC_STATE           = "zmSync" + DELIMITER;

    private MemcachedKeyPrefix() {
    }

}
