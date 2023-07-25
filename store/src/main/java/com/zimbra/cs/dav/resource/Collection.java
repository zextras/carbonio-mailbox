// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.dav.resource;

import com.zimbra.cs.service.FileUploadServlet;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import com.zimbra.common.mime.MimeConstants;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.cs.dav.DavContext;
import com.zimbra.cs.dav.DavElements;
import com.zimbra.cs.dav.DavException;
import com.zimbra.cs.dav.property.Acl;
import com.zimbra.cs.dav.property.CalDavProperty;
import com.zimbra.cs.dav.service.DavServlet;
import com.zimbra.cs.mailbox.Folder;
import com.zimbra.cs.mailbox.MailItem;
import com.zimbra.cs.mailbox.MailServiceException;
import com.zimbra.cs.mailbox.Mailbox;

/**
 * RFC 2518bis section 5.
 *
 * Collections map to mailbox folders.
 *
 */
public class Collection extends MailItemResource {

    protected MailItem.Type view;
    protected int mMailboxId;

    public Collection(DavContext ctxt, Folder f) throws DavException, ServiceException {
        super(ctxt, f);
        setCreationDate(f.getDate());
        setLastModifiedDate(f.getChangeDate());
        setProperty(DavElements.P_DISPLAYNAME, f.getName());
        setProperty(DavElements.P_GETCONTENTLENGTH, "0");
        mId = f.getId();
        this.view = f.getDefaultView();
        this.type = f.getType();
        addProperties(Acl.getAclProperties(this, f));
        mMailboxId = f.getMailboxId();
        if (this.isCalendarHomeSet()) {
            addProperty(CalDavProperty.getSupportedCalendarComponentSets());
        }
    }

    public Collection(String name, String acct) throws DavException {
        super(name, acct);
        long now = System.currentTimeMillis();
        setCreationDate(now);
        setLastModifiedDate(now);
        setProperty(DavElements.P_DISPLAYNAME, name.substring(1));
        setProperty(DavElements.P_GETCONTENTLENGTH, "0");
        try {
            addProperties(Acl.getAclProperties(this, null));
        } catch (ServiceException se) {
        }
    }

    @Override
    public String getContentType(DavContext ctxt) {
        if (ctxt.isWebRequest())
            return MimeConstants.CT_TEXT_PLAIN;
        return MimeConstants.CT_TEXT_XML;
    }

    @Override
    public boolean hasContent(DavContext ctxt) {
        return true;
    }

    @Override
    public InputStream getContent(DavContext ctxt) throws IOException, DavException {
        if (ctxt.isWebRequest())
            return new ByteArrayInputStream(getTextContent(ctxt).getBytes("UTF-8"));
        return new ByteArrayInputStream(getPropertiesAsText(ctxt).getBytes("UTF-8"));
    }

    @Override
    public boolean isCollection() {
        return true;
    }

    @Override
    public java.util.Collection<DavResource> getChildren(DavContext ctxt) throws DavException {
        ArrayList<DavResource> children = new ArrayList<DavResource>();

        try {
            ctxt.setCollectionPath(getUri());
            List<MailItem> items = getChildrenMailItem(ctxt);
            for (MailItem item : items) {
                DavResource rs = UrlNamespace.getResourceFromMailItem(ctxt, item);
                if (rs != null)
                    children.add(rs);
            }
        } catch (ServiceException e) {
            ZimbraLog.dav.error("can't get children from folder: id="+mId, e);
        }
        // this is where we add the phantom folder for attachment browser.
        if (isRootCollection()) {
            children.add(new Collection(UrlNamespace.ATTACHMENTS_PREFIX, getOwner()));
        }
        return children;
    }


    public MailItem.Type getDefaultView() {
        return view;
    }

    private List<MailItem> getChildrenMailItem(DavContext ctxt) throws DavException,ServiceException {
        Mailbox mbox = getMailbox(ctxt);

        List<MailItem> ret = new ArrayList<MailItem>();

        // XXX aggregate into single call
        ret.addAll(mbox.getItemList(ctxt.getOperationContext(), MailItem.Type.FOLDER, mId));
        ret.addAll(mbox.getItemList(ctxt.getOperationContext(), MailItem.Type.CONTACT, mId));
        return ret;
    }

    public Collection mkCol(DavContext ctxt, String name) throws DavException {
        return mkCol(ctxt, name, view);
    }

    public Collection mkCol(DavContext ctxt, String name, MailItem.Type view) throws DavException {
        try {
            Mailbox mbox = getMailbox(ctxt);
            Folder.FolderOptions fopt = new Folder.FolderOptions().setDefaultView(view);
            Folder f = mbox.createFolder(ctxt.getOperationContext(), name, mId, fopt);
            return (Collection)UrlNamespace.getResourceFromMailItem(ctxt, f);
        } catch (ServiceException e) {
            if (e.getCode().equals(MailServiceException.ALREADY_EXISTS))
                throw new DavException("item already exists", HttpServletResponse.SC_CONFLICT, e);
            else if (e.getCode().equals(ServiceException.PERM_DENIED))
                throw new DavException("permission denied", HttpServletResponse.SC_FORBIDDEN, e);
            else
                throw new DavException("can't create", HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e);
        }
    }

    public DavResource createItem(DavContext ctxt, String name) throws DavException, IOException {
        Mailbox mbox = null;
        try {
            mbox = getMailbox(ctxt);
        } catch (ServiceException e) {
            throw new DavException("cannot get mailbox",
                HttpServletResponse.SC_INTERNAL_SERVER_ERROR, null);
        }

        FileUploadServlet.Upload upload = ctxt.getUpload();
        String ctype = upload.getContentType();
        return createVCard(ctxt, name);
    }

    protected String relativeUrlForChild(String user, String baseName)
    throws DavException, ServiceException {
        return new StringBuilder(getHref()).append('/').append(baseName).toString();
    }

    protected String fullUrlForChild(String user, String basename) throws DavException, ServiceException {
        StringBuilder url = new StringBuilder();
        url.append(DavServlet.getDavUrl(user)).append(mPath).append("/").append(basename);
        return url.toString();
    }

    protected DavResource createVCard(DavContext ctxt, String name) throws DavException, IOException {
        return AddressObject.create(ctxt, name, this, true);
    }

    @Override
    public void delete(DavContext ctxt) throws DavException {
        String user = ctxt.getUser();
        String path = ctxt.getPath();
        if (user == null || path == null)
            throw new DavException("invalid uri", HttpServletResponse.SC_NOT_FOUND, null);
        try {
            Mailbox mbox = getMailbox(ctxt);
            mbox.delete(ctxt.getOperationContext(), mId, type);
        } catch (ServiceException e) {
            throw new DavException("cannot get item", HttpServletResponse.SC_NOT_FOUND, e);
        }
    }

    protected boolean isRootCollection() {
        return (mId == Mailbox.ID_FOLDER_USER_ROOT);
    }

    protected boolean isCalendarHomeSet() {
        return isRootCollection();
    }
}
