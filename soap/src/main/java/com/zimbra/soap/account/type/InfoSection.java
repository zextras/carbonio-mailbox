// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.account.type;

import com.zimbra.common.service.ServiceException;
import java.util.Arrays;
import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;

@XmlEnum
public enum InfoSection {
    // mbox,prefs,attrs,zimlets,props,idents,sigs,dsrcs,children
    @XmlEnumValue("mbox") mbox,
    @XmlEnumValue("prefs") prefs,
    @XmlEnumValue("attrs") attrs,
    @XmlEnumValue("zimlets") zimlets,
    @XmlEnumValue("props") props,
    @XmlEnumValue("idents") idents,
    @XmlEnumValue("sigs") sigs,
    @XmlEnumValue("dsrcs") dsrcs,
    @XmlEnumValue("children") children;

    public static InfoSection fromString(String s) throws ServiceException {
        try {
            return InfoSection.valueOf(s);
        } catch (IllegalArgumentException e) {
            throw ServiceException.INVALID_REQUEST("invalid sortBy: "+s+", valid values: "+Arrays.asList(InfoSection.values()), e);
        }
    }
}
