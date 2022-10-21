// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.doc.soap;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import java.util.List;

public class ChoiceNode implements DescriptionNode {
  static final String name = "{CHOICE NODE}";
  private DescriptionNode parent;
  private List<DescriptionNode> children = Lists.newArrayList();
  private boolean singleChild;

  public ChoiceNode(boolean canHaveMultipleChildren) {
    this.singleChild = !canHaveMultipleChildren;
  }

  public boolean isSingleChild() {
    return singleChild;
  }

  public String getHtmlDescription() {
    if (Strings.isNullOrEmpty(name)) {
      return "";
    }
    StringBuilder desc = new StringBuilder();
    writeDescription(desc, 1);
    return desc.toString();
  }

  public void addChild(DescriptionNode child) {
    children.add(child);
  }

  @Override
  public void writeDescription(StringBuilder desc, int depth) {
    XmlElementDescription.writeRequiredIndentation(desc, true, depth);
    if (singleChild) {
      desc.append("Choose one of");
    } else {
      desc.append("List of any of");
    }
    desc.append(": {<br />\n");
    for (DescriptionNode child : getChildren()) {
      child.writeDescription(desc, depth + 1);
    }
    XmlElementDescription.writeRequiredIndentation(desc, true, depth);
    desc.append("}<br />\n");
  }

  @Override
  public List<DescriptionNode> getChildren() {
    return children;
  }

  @Override
  public DescriptionNode getParent() {
    return parent;
  }

  @Override
  public String getSummary() {
    return name;
  }

  @Override
  public String getDescription() {
    return "";
  }

  /** A Choice node is a pseudo node that doesn't contribute anything to XPath */
  @Override
  public String getXPath() {
    return (parent == null) ? "" : parent.getXPath();
  }

  @Override
  public String xmlLinkTargetName() {
    return (parent == null) ? "" : parent.xmlLinkTargetName();
  }

  @Override
  public String tableLinkTargetName() {
    return (parent == null) ? "" : parent.tableLinkTargetName();
  }
}
