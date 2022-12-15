// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.filter;

import org.apache.jsieve.parser.SieveNode;
import org.apache.jsieve.parser.generated.Node;

import com.zimbra.common.service.ServiceException;

class FolderRenamer extends SieveVisitor {
    
    String mOldPath;
    String mNewPath;
    boolean mRenamed = false;
    
    FolderRenamer(String oldPath, String newPath) {
        // Make sure paths are always prefixed with a slash.
        mOldPath = prefixWithSlash(oldPath);
        mNewPath = prefixWithSlash(newPath);
    }
    
    boolean renamed() {
        return mRenamed;
    }
    
    @Override
    protected void visitFileIntoAction(Node node, VisitPhase phase, RuleProperties props, String folderPath, boolean copy)
    throws ServiceException {
        if (phase != SieveVisitor.VisitPhase.begin || folderPath == null) {
            return;
        }
        folderPath = prefixWithSlash(folderPath);
        if (folderPath.startsWith(mOldPath)) {
            String newPath = folderPath.replace(mOldPath, mNewPath);
            SieveNode folderNameNode = (SieveNode) getNode(node, 0, 0, 0, 0);
            String escapedName = FilterUtil.escape(newPath);
            folderNameNode.setValue(escapedName);
            mRenamed = true;
        }
    }

    static String prefixWithSlash(String path) {
        if (path == null || path.length() == 0) {
            return path;
        }
        if (!(path.charAt(0) == '/')) {
            return "/" + path;
        }
        return path;
    }

}
