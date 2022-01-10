// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.doc.soap;

public enum OccurrenceSpec {
    REQUIRED, // one and only one 1:1 = ""
    OPTIONAL, // zero or one 0:1 = "?"
    REQUIRED_MORE, // one or more 1:* = "+"
    OPTIONAL_MORE;  // zero or more 0:* = "*"

    private static final String OCCURRENCE_REQUIRED_STR = ""; // one and only one 1:1 = ""
    private static final String OCCURRENCE_OPTIONAL_STR = "?"; // zero or one 0:1 = "?"
    private static final String OCCURRENCE_REQUIRED_MORE_STR = "+"; // one or more 1:* = "+"
    private static final String OCCURRENCE_OPTIONAL_MORE_STR = "*"; // zero or more 0:* = "*"

    public String getOccurrenceAsString() {
        switch(this) {
            case OPTIONAL: {
                return OCCURRENCE_OPTIONAL_STR;
            }
            case REQUIRED_MORE: {
                return OCCURRENCE_REQUIRED_MORE_STR;
            }
            case OPTIONAL_MORE: {
                return OCCURRENCE_OPTIONAL_MORE_STR;
            }
        }
        return OCCURRENCE_REQUIRED_STR;
    }
}
