// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.doc.soap;

/**
 * Used for table entries describing either XML attributes or elements
 */
public interface XmlUnit {
    String getTableKeyColumnContents();
    String getName();
    OccurrenceSpec getOccurrence();
    String getDescriptionForTable();
}
