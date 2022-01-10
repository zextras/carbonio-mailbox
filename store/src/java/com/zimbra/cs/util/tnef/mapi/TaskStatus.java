// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.util.tnef.mapi;

/**
 * The <code>TaskStatus</code> class relates to the PidLidTaskStatus MAPI Property
 * which is documented in MS-OXOTASK as specifying the status of the user's progress
 * on the task.
 *
 * @author Gren Elliot
 */
public enum TaskStatus {
    NOT_STARTED       (0x00000000),   // User has not started work on the task (PidLidPercentComplete == 0)
    IN_PROGRESS       (0x00000001),   // User's work is in progress (0 < PidLidPercentComplete < 1.0)
    COMPLETE          (0x00000002),   // User's work on task is complete (PidLidPercentComplete == 1.0)
    WAITING_ON_OTHER  (0x00000003),   // User is waiting on somebody else.
    DEFERRED          (0x00000004);   // User has deferred work on the task.

    private final int MapiPropValue;

    TaskStatus(int propValue) {
        MapiPropValue = propValue;
    }

    public int mapiPropValue() {
        return MapiPropValue;
    }

}
