// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.client;

import com.zimbra.common.account.ProvisioningConstants;
import com.zimbra.soap.account.type.LicenseAttr;
import com.zimbra.soap.account.type.LicenseInfo;

import java.util.HashMap;


public class ZLicenses {

    private HashMap<String, String> licenseMap = new HashMap<String, String>();
    private final String LICENSE_VOICE = "VOICE";
    private final String LICENSE_SMIME = "SMIME";
    private final String LICENSE_TOUCHCLIENT = "TOUCHCLIENT";

    public ZLicenses(LicenseInfo licenses) {

        if (licenses != null) {
            for (LicenseAttr attr : licenses.getAttrs()) {
                licenseMap.put(attr.getName(), attr.getContent());
            }
        }
    }

    private String get(String name) {
        return licenseMap.get(name);
    }

    public boolean getBool(String name) {
        return ProvisioningConstants.TRUE.equals(get(name));
    }

    public boolean getVoice() {
        return getBool(LICENSE_VOICE);
    }

    public boolean getSmime() {
        return getBool(LICENSE_SMIME);
    }

    public boolean getTouchClient() {
        return getBool(LICENSE_TOUCHCLIENT);
    }
}
