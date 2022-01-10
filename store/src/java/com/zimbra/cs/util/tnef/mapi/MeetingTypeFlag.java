// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.util.tnef.mapi;

/**
 * The <code>MeetingTypeFlag</code> enum is used to represent flags
 * in the PidLidMeetingType MAPI property.
 * 
 * @author Gren Elliot
 *
 */
public enum MeetingTypeFlag {
    MTG_EMPTY                (0x00000000), // Unspecified.
    MTG_REQUEST              (0x00000001), // Initial meeting request.
    MTG_FULL                 (0x00010000), // Full update.
    MTG_INFO                 (0x00020000), // Informational update.
    MTG_OUTOFDATE            (0x00080000), // A newer Meeting Request object or Meeting Update object was
                                           // received after this one. For more details, see section 3.1.5.2.
    MTG_DELEGATORCOPY        (0x00100000); // This is set on the delegator's copy when a delegate will handle
                                           // meeting-related objects.

    private final int mapiFlagBit;

    MeetingTypeFlag(int flagPos) {
        mapiFlagBit = flagPos;
    }

    public int mapiFlagBit() {
        return mapiFlagBit;
    }


}
