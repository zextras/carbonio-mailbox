// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.util.tnef.mapi;

/**
 * The <code>TaskMode</code> class relates to the PidLidTaskMode MAPI Property
 * which is documented in MS-OXOTASK as specifying the assignment status of a
 * task object.
 *
 * @author Gren Elliot
 */
public enum TaskMode {
    TASK_NOT_ASSIGNED         (0x00000000),   // The Task object is not assigned.
    TASK_REQUEST              (0x00000001),   // The Task object is embedded in a task request.
    TASK_ACCEPTED_BY_ASSIGNEE (0x00000002),   // The Task object has been accepted by the task assignee.
    TASK_REJECTED_BY_ASSIGNEE (0x00000003),   // The Task object was rejected by the task assignee.
    TASK_UPDATE               (0x00000004),   // The Task object is embedded in a task update.
    TASK_SELF_DELEGATED       (0x00000005);   // The Task object was assigned to the task assigner (self-delegation).

    private final int MapiPropValue;

    TaskMode(int propValue) {
        MapiPropValue = propValue;
    }

    public int mapiPropValue() {
        return MapiPropValue;
    }

}
