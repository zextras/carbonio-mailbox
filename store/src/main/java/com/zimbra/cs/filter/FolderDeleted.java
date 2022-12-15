// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.filter;

import org.apache.jsieve.parser.SieveNode;
import org.apache.jsieve.parser.generated.Node;

/**
 * Disables filter rules that reference a folder that was deleted. 
 */
public class FolderDeleted
extends SieveVisitor {

    private String mDeletedFolderPath;
    private Node mIfNode;
    private boolean mModified = false;
    
    public FolderDeleted(String deletedFolderPath) {
        mDeletedFolderPath = FolderRenamer.prefixWithSlash(deletedFolderPath);
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
    protected void visitFileIntoAction(Node node, VisitPhase phase, RuleProperties props,
                                       String folderPath, boolean copy) {
        if (phase != VisitPhase.begin) {
            return;
        }
        folderPath = FolderRenamer.prefixWithSlash(folderPath);
        String ifNodeName = getNodeName(mIfNode);
        if (folderPath.startsWith(mDeletedFolderPath) && "if".equals(ifNodeName)) {
            ((SieveNode) mIfNode).setName("disabled_if");
            mModified = true;
        }
    }
    
    
}
