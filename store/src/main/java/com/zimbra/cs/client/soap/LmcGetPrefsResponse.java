// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.client.soap;

import java.util.HashMap;

public class LmcGetPrefsResponse extends LmcSoapResponse {

    // for storing the returned preferences
    private HashMap mPrefMap;

    public HashMap getPrefsMap() { return mPrefMap; }

    public void setPrefsMap(HashMap p) { mPrefMap = p; }

}
