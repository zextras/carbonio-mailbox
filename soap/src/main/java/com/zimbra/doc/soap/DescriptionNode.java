// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.doc.soap;

import java.util.List;

public interface DescriptionNode {
    public DescriptionNode getParent();
    public List<DescriptionNode> getChildren();
    public void writeDescription(StringBuilder desc, int depth);
    public String getDescription();
    public String getSummary();
    public String getXPath();
    public String xmlLinkTargetName();
    public String tableLinkTargetName();
}
