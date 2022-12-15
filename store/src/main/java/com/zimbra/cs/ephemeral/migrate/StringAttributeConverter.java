// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: AGPL-3.0-only

package com.zimbra.cs.ephemeral.migrate;

import com.zimbra.cs.ephemeral.EphemeralInput;
import com.zimbra.cs.ephemeral.EphemeralKey;

public class StringAttributeConverter extends AttributeConverter {

    @Override
    public EphemeralInput convert(String attrName, Object ldapValue) {
        EphemeralKey key = new EphemeralKey(attrName);
        return new EphemeralInput(key, (String) ldapValue);
    }

    @Override
    public boolean isMultivalued() {
        return false;
    }

}
