// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.doc.soap;

import java.util.List;

public interface DescriptionNode {
    DescriptionNode getParent();
    List<DescriptionNode> getChildren();
    void writeDescription(StringBuilder desc, int depth);
    String getDescription();
    String getSummary();
    String getXPath();
    String xmlLinkTargetName();
    String tableLinkTargetName();
}
