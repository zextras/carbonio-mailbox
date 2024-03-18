// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.account;

import java.io.IOException;
import java.io.StringReader;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import com.zimbra.common.account.SignatureUtil;
import com.zimbra.common.account.ZAttrProvisioning;
import com.zimbra.common.mime.MimeConstants;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.cs.html.BrowserDefang;
import com.zimbra.cs.html.DefangFactory;

public class Signature extends AccountProperty implements Comparable {

    public Signature(Account acct, String name, String id, Map<String, Object> attrs, Provisioning prov) {
        super(acct, name, id, attrs, null, prov);
    }

    @Override
    public EntryType getEntryType() {
        return EntryType.SIGNATURE;
    }

    /**
     * this should only be used internally by the server. it doesn't modify the real id, just
     * the cached one.
     * @param id
     */
    public void setId(String id) {
        mId = id;
        getRawAttrs().put(Provisioning.A_zimbraSignatureId, id);
    }

    public static class SignatureContent {
        private String mMimeType;
        private String mContent;

        public SignatureContent(String mimeType, String content) {
            mMimeType = mimeType;
            mContent = content;
        }

        public String getMimeType() { return mMimeType; }
        public String getContent() { return mContent; }
    }

    public Set<SignatureContent> getContents() {
        Set<SignatureContent> contents = new HashSet<SignatureContent>();
        BrowserDefang defanger = DefangFactory.getDefanger(MimeConstants.CT_TEXT_HTML);
      for (Map.Entry<String, String> stringStringEntry : SignatureUtil.ATTR_TYPE_MAP.entrySet()) {
        Map.Entry entry = (Map.Entry) stringStringEntry;

        String content = getAttr((String) entry.getKey());
        if (content != null) {
          if (entry.getKey().equals(ZAttrProvisioning.A_zimbraPrefMailSignatureHTML)) {

            StringReader reader = new StringReader(content);
            try {
              content = defanger.defang(reader, false);
            } catch (IOException e) {
              ZimbraLog.misc.info("Error sanitizing html signature: %s", content);
            }

          }
          contents.add(new SignatureContent((String) entry.getValue(), content));
        }
      }
        return contents;
    }
}
