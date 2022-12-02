// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.dav.service.method;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.dom4j.Document;
import org.dom4j.Element;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.dav.DavContext;
import com.zimbra.cs.dav.DavContext.Depth;
import com.zimbra.cs.dav.DavElements;
import com.zimbra.cs.dav.DavException;
import com.zimbra.cs.dav.DavProtocol;
import com.zimbra.cs.dav.LockMgr;
import com.zimbra.cs.dav.LockMgr.LockScope;
import com.zimbra.cs.dav.LockMgr.LockType;
import com.zimbra.cs.dav.property.LockDiscovery;
import com.zimbra.cs.dav.service.DavMethod;

public class Lock extends DavMethod {
	public static final String LOCK  = "LOCK";
	@Override
    public String getName() {
		return LOCK;
	}
	@Override
    public void handle(DavContext ctxt) throws DavException, IOException, ServiceException {
	    LockMgr lockmgr = LockMgr.getInstance();
	    LockMgr.Lock lock = null;
	    if (ctxt.hasRequestMessage()) {
	        DavContext.Depth depth = ctxt.getDepth();
	        if (depth == Depth.one)
	            throw new DavException("invalid depth", HttpServletResponse.SC_BAD_REQUEST, null);
	        String d = (depth == Depth.zero) ? "0" : depth.toString();

	        LockMgr.LockScope scope = LockScope.shared;
	        LockMgr.LockType type = LockType.write;

	        Document req = ctxt.getRequestMessage();
	        Element top = req.getRootElement();
	        if (!top.getName().equals(DavElements.P_LOCKINFO))
	            throw new DavException("msg "+top.getName()+" not allowed in LOCK", HttpServletResponse.SC_BAD_REQUEST, null);

	        Element e = top.element(DavElements.E_LOCKSCOPE);
	        @SuppressWarnings("unchecked")
	        List<Element> ls = e.elements();
	        for (Element v : ls) {
	            if (v.getQName().equals(DavElements.E_EXCLUSIVE))
	                scope = LockScope.exclusive;
	            else if (v.getQName().equals(DavElements.E_SHARED))
	                scope = LockScope.shared;
	            else
	                throw new DavException("unrecognized scope element "+v.toString(), HttpServletResponse.SC_BAD_REQUEST, null);
	        }
	        e = top.element(DavElements.E_LOCKTYPE);
	        @SuppressWarnings("unchecked")
	        List<Element> lt = e.elements();
	        for (Element v : lt) {
	            if (v.getQName().equals(DavElements.E_WRITE))
	                type = LockType.write;
	            else
	                throw new DavException("unrecognized type element "+v.toString(), HttpServletResponse.SC_BAD_REQUEST, null);
	        }
	        String owner;
	        e = top.element(DavElements.E_OWNER);
	        if (e != null && e.elementIterator(DavElements.E_HREF).hasNext()) {
	            Element ownerElem = (Element)e.elementIterator(DavElements.E_HREF).next();
	            owner = ownerElem.getText();
	        } else {
	            owner = ctxt.getAuthAccount().getName();
	        }

	        lock = lockmgr.createLock(ctxt, owner, ctxt.getUri(), type, scope, d);
	        ctxt.getResponse().addHeader(DavProtocol.HEADER_LOCK_TOKEN, lock.toLockTokenHeader());
        } else { // refresh lock
            String token = ctxt.getRequest().getHeader(DavProtocol.HEADER_IF);
            if (token == null) {
                throw new DavException("no request body",
                        HttpServletResponse.SC_BAD_REQUEST, null);
            }
            token = token.trim();
            int len = token.length();
            if (token.charAt(0) == '(' && token.charAt(len - 1) == ')') {
                token = token.substring(1, len - 1);
            }
            List<LockMgr.Lock> locks = lockmgr.getLocks(ctxt.getUri());
            for (LockMgr.Lock l : locks) {
                if (l.token.equals(LockMgr.Lock.parseLockTokenHeader(token))) {
                    l.extendExpiration();
                    lock = l;
                    break;
                }
            }
            if (lock == null) {
                throw new DavException("Lock does not exist",
                        HttpServletResponse.SC_PRECONDITION_FAILED, null);
            }
        }
		ctxt.getDavResponse().addProperty(ctxt, new LockDiscovery(lock));
		ctxt.setStatus(HttpServletResponse.SC_OK);
		sendResponse(ctxt);
	}
}
