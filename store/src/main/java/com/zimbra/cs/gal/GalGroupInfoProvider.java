// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.gal;

import com.zimbra.common.localconfig.LC;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.extension.ExtensionUtil;
import com.zimbra.cs.gal.GalGroup.GroupInfo;

public class GalGroupInfoProvider {

    private static GalGroupInfoProvider instance;
    
    public static synchronized GalGroupInfoProvider getInstance() {
        if (instance == null) {
            instance = makeInstance();
        }
        return instance;
    }

    private static GalGroupInfoProvider makeInstance() {
        GalGroupInfoProvider provider = null;
        String className = LC.zimbra_class_galgroupinfoprovider.value();
        if (className != null && !className.equals("")) {
            try {
                try {
                    provider = (GalGroupInfoProvider) Class.forName(className).newInstance();
                } catch (ClassNotFoundException cnfe) {
                    // ignore and look in extensions
                    provider = (GalGroupInfoProvider) ExtensionUtil.findClass(className).newInstance();
                }
            } catch (Exception e) {
                ZimbraLog.account.error("could not instantiate GalGroupInfoProvider interface of class '" + className + "'; defaulting to GalGroupInfoProvider", e);
            }
        }
        if (provider == null)
            provider = new GalGroupInfoProvider();
        return provider;
    }
    
    public GroupInfo getGroupInfo(String addr, boolean needCanExpand, Account requestedAcct, Account authedAcct) {
        return GalGroup.getGroupInfo(addr, true, requestedAcct, authedAcct);
    }

}
