// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.client;

public class LmcWiki extends LmcDocument {
	private String mWikiWord;
	private String mContents;
	
	public void setWikiWord(String w) { mWikiWord = w; setName(w); }
	public void setContents(String c) { mContents = c; }
	
	public String getWikiWord() { return mWikiWord; }
	public String getContents() { return mContents; }
	
	public String toString() {
		return "Wiki id=" + mId + " rev=" + mRev + " wikiword=" + mWikiWord +
		" folder=" + mFolder + " lastEditor=" + mLastEditedBy + 
		" lastModifiedDate=" + mLastModifiedDate;
	}
}
