// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

/*
 * Created on May 5, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package com.zimbra.cs.object;

/**
 * @author schemers
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class MatchedObject {
	
	private ObjectHandler mHandler;
	private String mMatchedText;
	
	public MatchedObject(ObjectHandler handler, String matchedText) {
		mHandler = handler;
		mMatchedText = matchedText;
	}
	
	public ObjectHandler getHandler() {
		return mHandler;
	}
	
	public String getMatchedText() {
		return mMatchedText;
	}
	
	public String toString() {
		return "MatchedObject: {handler:"+mHandler+", matchedText:"+mMatchedText+"}";
	}
}
