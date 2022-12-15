// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.json.jackson;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.module.SimpleModule;

/**
 * Module that augments basic module to handle differences between
 * Zimbra-style JSON and standard Jackson-style JSON.
 */
public class ZimbraJsonModule extends SimpleModule {
    /**
     * 
     */
    private static final long serialVersionUID = -4809416138833975969L;
    private final static Version VERSION = new Version(0, 1, 0, null);

    public ZimbraJsonModule() {
        super("ZimbraJsonModule", VERSION);
    }

    @Override
    public void setupModule(SetupContext context) {
        // Need to modify BeanSerializer that is used
        context.addBeanSerializerModifier(new ZimbraBeanSerializerModifier());
    }
}
