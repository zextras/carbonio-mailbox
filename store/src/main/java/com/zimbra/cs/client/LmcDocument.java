// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.client;

public class LmcDocument {
	protected String mId;
	protected String mName;
	protected String mContentType;
	protected String mRev;
	protected String mFolder;
	protected String mLastEditedBy;
	protected String mLastModifiedDate;
	protected String mAid;
	protected String mRestUrl;
	protected String mFragment;
	protected String mCreator;
	protected String mCreateDate;
	
	public void setID(String id)             { mId = id; }
	public void setName(String filename)     { mName = filename; }
	public void setContentType(String ct)    { mContentType = ct; }
	public void setRev(String rev)           { mRev = rev; }
	public void setFolder(String folder)     { mFolder = folder; }
	public void setLastEditor(String str)    { mLastEditedBy = str; }
	public void setLastModifiedDate(String d)  { mLastModifiedDate = d; }
	public void setAttachmentId(String aid)  { mAid = aid; }
	public void setRestUrl(String url)       { mRestUrl = url; }
	public void setFragment(String f)        { mFragment = f; }
	public void setCreator(String cr)        { mCreator = cr; }
	public void setCreateDate(String cd)     { mCreateDate = cd; }
	
	public String getID()               { return mId; }
	public String getName()             { return mName; }
	public String getContentType()      { return mContentType; }
	public String getRev()              { return mRev; }
	public String getFolder()           { return mFolder; }
	public String getLastEditor()       { return mLastEditedBy; }
	public String getLastModifiedDate() { return mLastModifiedDate; }
	public String getAttachmentId()     { return mAid; }
	public String getRestUrl()          { return mRestUrl; }
	public String getFragment()         { return mFragment; }
	public String getCreator()          { return mCreator; }
	public String getCreateDate()       { return mCreateDate; }
	
	public String toString() {
		return "Document id=" + mId + " rev=" + mRev + " filename=" + mName +
		" ct=" + mContentType + " folder=" + mFolder + " lastEditor=" + mLastEditedBy + 
		" lastModifiedDate=" + mLastModifiedDate + " restUrl=" + mRestUrl;
	}
}
