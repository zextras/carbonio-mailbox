// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.ephemeral.migrate;


public abstract class MultivaluedAttributeConverter extends AttributeConverter {

    @Override
    public boolean isMultivalued() {
        return true;
    }
}
