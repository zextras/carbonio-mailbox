// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.mailbox;

import com.zimbra.common.service.ServiceException;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.cs.db.DbMailItem;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mime.ParsedDocument;

public final class WikiItem extends Document {

    WikiItem(Mailbox mbox, UnderlyingData data) throws ServiceException {
        this(mbox, data, false);
    }
    
    WikiItem(Mailbox mbox, UnderlyingData data, boolean skipCache) throws ServiceException {
        super(mbox, data, skipCache);
    }

    public String getWikiWord() {
        return getName();
    }

    public static final String WIKI_CONTENT_TYPE = "text/html; charset=utf-8";

    static WikiItem create(int id, String uuid, Folder folder, String wikiword, ParsedDocument pd, CustomMetadata custom)
            throws ServiceException {
        Metadata meta = new Metadata();
        UnderlyingData data = prepareCreate(Type.WIKI, id, uuid, folder, wikiword, WIKI_CONTENT_TYPE, pd, meta, custom, 0);

        Mailbox mbox = folder.getMailbox();
        data.contentChanged(mbox);
        ZimbraLog.mailop.info("Adding WikiItem %s: id=%d, folderId=%d, folderName=%s.",
            wikiword, data.id, folder.getId(), folder.getName());
        new DbMailItem(mbox).create(data);

        WikiItem wiki = new WikiItem(mbox, data);
        wiki.finishCreation(null);
        pd.setVersion(wiki.getVersion());
        return wiki;
    }
}
