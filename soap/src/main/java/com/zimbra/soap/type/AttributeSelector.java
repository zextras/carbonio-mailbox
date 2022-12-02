// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.type;

public interface AttributeSelector {
    public String getAttrs();
    public AttributeSelector setAttrs(String attrs);
    public AttributeSelector addAttrs(String attr);
    public AttributeSelector addAttrs(String ... attrNames);
    public AttributeSelector addAttrs(Iterable<String> attrs);
}
