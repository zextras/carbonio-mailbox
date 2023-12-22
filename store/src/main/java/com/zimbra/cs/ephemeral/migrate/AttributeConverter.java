// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.ephemeral.migrate;

import com.zimbra.cs.ephemeral.EphemeralInput;

public abstract class AttributeConverter {

    public abstract EphemeralInput convert(String attrName, Object ldapValue);
    public abstract boolean isMultivalued();

}
