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

package com.zimbra.cs.client.soap;

import com.zimbra.cs.client.soap.LmcSoapResponse;

public class LmcContactActionResponse extends LmcSoapResponse {

	private String mIDList;
	private String mOp;
	
	public String getIDList() { return mIDList; }
	public String getOp() { return mOp; }
	
	public void setIDList(String idList) { mIDList = idList; }
	public void setOp(String op) { mOp = op; }
}
