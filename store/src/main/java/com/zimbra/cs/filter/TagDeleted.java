// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.filter;

import org.apache.jsieve.parser.SieveNode;
import org.apache.jsieve.parser.generated.Node;

/** Disables any filter rules that reference a deleted tag. */
public class TagDeleted extends SieveVisitor {

  private String mDeletedTagName;
  private Node mIfNode;
  private boolean mModified = false;

  public TagDeleted(String deletedTagName) {
    mDeletedTagName = deletedTagName;
  }

  public boolean modified() {
    return mModified;
  }

  @Override
  protected void visitNode(Node node, VisitPhase phase, RuleProperties props) {
    if (phase != VisitPhase.begin) {
      return;
    }
    String name = getNodeName(node);
    if ("if".equals(name) || "disabled_if".equals(name)) {
      // Remember the top-level node so we can modify it later.
      mIfNode = node;
    }
  }

  @Override
  protected void visitTagAction(Node node, VisitPhase phase, RuleProperties props, String tagName) {
    if (phase != VisitPhase.begin) {
      return;
    }
    String ifNodeName = getNodeName(mIfNode);
    if (tagName.equals(mDeletedTagName) && "if".equals(ifNodeName)) {
      ((SieveNode) mIfNode).setName("disabled_if");
      mModified = true;
    }
  }
}
