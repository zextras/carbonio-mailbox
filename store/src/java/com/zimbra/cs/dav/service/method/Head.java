// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.dav.service.method;

import java.io.IOException;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.dav.DavContext;
import com.zimbra.cs.dav.DavException;

public class Head extends Get {
	public static final String HEAD  = "HEAD";
	public String getName() {
		return HEAD;
	}
	protected boolean returnContent() {
		return false;
	}
	public void handle(DavContext ctxt) throws DavException, IOException, ServiceException {
		super.handle(ctxt);
		int cl = ctxt.getRequestedResource().getContentLength();
		if (cl > 0)
			ctxt.getResponse().setContentLength(cl);
	}
}
