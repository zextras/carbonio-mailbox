/*
 * ***** BEGIN LICENSE BLOCK *****
 * Zimbra Collaboration Suite Server
 * Copyright (C) 2004, 2005, 2006 Zimbra, Inc.
 * 
 * The contents of this file are subject to the Yahoo! Public License
 * Version 1.0 ("License"); you may not use this file except in
 * compliance with the License.  You may obtain a copy of the License at
 * http://www.zimbra.com/license.
 * 
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied.
 * ***** END LICENSE BLOCK *****
 */

/*
 * Created on 2004. 7. 21.
 */
package com.zimbra.cs.redolog.op;

import java.io.IOException;

import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mailbox.MailboxManager;
import com.zimbra.cs.redolog.RedoLogInput;
import com.zimbra.cs.redolog.RedoLogOutput;

/**
 * @author jhahm
 */
public class ModifySavedSearch extends RedoableOp {

	private int mSearchId;
    private String mQuery;
    private String mTypes;
    private String mSort;

	public ModifySavedSearch() {
		mSearchId = UNKNOWN_ID;
	}

	public ModifySavedSearch(int mailboxId, int searchId, String query, String types, String sort) {
		setMailboxId(mailboxId);
		mSearchId = searchId;
        mQuery = query != null ? query : "";
        mTypes = types != null ? types : "";
        mSort = sort != null ? sort : "";
	}

	public int getOpCode() {
		return OP_MODIFY_SAVED_SEARCH;
	}

	protected String getPrintableData() {
        StringBuffer sb = new StringBuffer("id=");
        sb.append(mSearchId).append(", query=").append(mQuery);
        sb.append(", types=").append(mTypes).append(", sort=").append(mSort);
        return sb.toString();
	}

	protected void serializeData(RedoLogOutput out) throws IOException {
		out.writeInt(mSearchId);
        out.writeUTF(mQuery);
        out.writeUTF(mTypes);
        out.writeUTF(mSort);
	}

	protected void deserializeData(RedoLogInput in) throws IOException {
		mSearchId = in.readInt();
        mQuery = in.readUTF();
        mTypes = in.readUTF();
        mSort = in.readUTF();
	}

	public void redo() throws Exception {
		int mboxId = getMailboxId();
		Mailbox mailbox = MailboxManager.getInstance().getMailboxById(mboxId);
    	mailbox.modifySearchFolder(getOperationContext(), mSearchId, mQuery, mTypes, mSort);
	}
}
