// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.util.tnef.mapi;

/**
 * @author gren
 *     <p>See MS-OXOCAL. Indicates which optional parts of ExceptionInfo are present.
 */
public enum ExceptionInfoOverrideFlag {
  ARO_SUBJECT(0x0001),
  ARO_MEETINGTYPE(0x0002),
  ARO_REMINDERDELTA(0x0004),
  ARO_REMINDER(0x0008), // Indicates that ReminderSet field is present
  ARO_LOCATION(0x0010),
  ARO_BUSYSTATUS(0x0020),
  ARO_ATTACHMENT(0x0040),
  ARO_SUBTYPE(0x0080),
  ARO_APPTCOLOR(0x0100),
  ARO_EXCEPTION_BODY(0x0200);

  private final int MapiPropValue;

  ExceptionInfoOverrideFlag(int propValue) {
    MapiPropValue = propValue;
  }

  public int mapiPropValue() {
    return MapiPropValue;
  }
}
