// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.util.tnef.mapi;

/**
 * @author Gren Elliot
 */
public enum AppointmentStateFlags {
  ASF_MEETING(0x00000001),
  ASF_RECEIVED(0x00000002),
  ASF_CANCELED(0x00000004);

  private final int MapiPropValue;

  AppointmentStateFlags(int propValue) {
    MapiPropValue = propValue;
  }

  public int mapiPropValue() {
    return MapiPropValue;
  }
}
