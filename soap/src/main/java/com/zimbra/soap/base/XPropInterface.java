// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.base;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

@XmlAccessorType(XmlAccessType.NONE)
public interface XPropInterface {
    XPropInterface createFromNameAndValue(String name, String value);
    String getName();
    String getValue();
    void setXParamInterfaces(Iterable<XParamInterface> xParams);
    void addXParamInterface(XParamInterface xParam);
    List<XParamInterface> getXParamInterfaces();
}
