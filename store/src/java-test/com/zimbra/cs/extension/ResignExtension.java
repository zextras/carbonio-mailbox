// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.extension;

import com.zimbra.cs.extension.ExtensionException;
import com.zimbra.cs.extension.ZimbraExtension;

/**
 * Test extension that lets the framework disable this extension.
 *
 * @author ysasaki
 */
public class ResignExtension implements ZimbraExtension {

    private static boolean destroyed = false;

    public String getName() {
        return "resign";
    }

    public void init() throws ExtensionException {
        destroyed = false;
        throw new ExtensionException("voluntarily resigned");
    }

    public void destroy() {
        destroyed = true;
    }

    static boolean isDestroyed() {
        return destroyed;
    }

}
