// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.extension;

/**
 * Optional interface for Zimbra Extensions -- if ZimbraExtension instance inherits from this
 * interface, it will be called during boot
 */
public interface ZimbraExtensionPostInit {
    /**
     * Called at the end of the server boot process, after all server subsystems
     * are up and running.
     */
    public void postInit();

}
