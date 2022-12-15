// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.util.tnef.mapi;

/**
 *
 * @author gren
 */
public enum BusyStatus {
    FREE (0x00000000),
    TENTATIVE (0x00000001),
    BUSY (0x00000002),
    OOF (0x00000003);

    private final int MapiPropValue;

    BusyStatus(int propValue) {
        MapiPropValue = propValue;
    }

    public int mapiPropValue() {
        return MapiPropValue;
    }

}
