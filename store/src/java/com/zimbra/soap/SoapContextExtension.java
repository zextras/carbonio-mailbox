// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.Element;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.cs.session.SoapSession;

public abstract class SoapContextExtension {
	
	
	private static List<SoapContextExtension> sExtensions = Collections.synchronizedList(new ArrayList<SoapContextExtension>());
	
	public static void register(String name, SoapContextExtension sce) {
		synchronized (sExtensions) {
			ZimbraLog.soap.info("Adding context extension: " + name);
			sExtensions.add(sce);
		}
	}
	
	public static void addExtensionHeaders(Element context, ZimbraSoapContext zsc, SoapSession session) throws ServiceException {
		SoapContextExtension[] exts = null;
		synchronized (sExtensions) {
			exts = new SoapContextExtension[sExtensions.size()];
			sExtensions.toArray(exts); //make a copy so that we keep lock on addExtensionHeader calls
		}
		for (SoapContextExtension sce : exts) {
			sce.addExtensionHeader(context, zsc, session);
		}
	}

	public abstract void addExtensionHeader(Element context, ZimbraSoapContext zsc,  SoapSession session) throws ServiceException;
}
