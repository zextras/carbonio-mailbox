// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.extension;

import com.zimbra.cs.extension.ZimbraExtension;

/**
 * Simple extension for testing.
 *
 * @author ysasaki
 */
public class SimpleExtension implements ZimbraExtension {
    private boolean initialized = false;
    private boolean destroyed = false;

    public String getName() {
        return "simple";
    }

    public void init() {
        initialized = true;
    }

    public void destroy() {
        destroyed = true;
    }

    boolean isInitialized() {
        return initialized;
    }

    boolean isDestroyed() {
        return destroyed;
    }

}
