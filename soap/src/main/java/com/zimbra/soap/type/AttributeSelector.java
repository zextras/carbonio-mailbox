// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.type;

public interface AttributeSelector {
    String getAttrs();
    AttributeSelector setAttrs(String attrs);
    AttributeSelector addAttrs(String attr);
    AttributeSelector addAttrs(String... attrNames);
    AttributeSelector addAttrs(Iterable<String> attrs);
}
