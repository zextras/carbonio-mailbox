// SPDX-FileCopyrightText: 2025 Zextras <https://www.zextras.com>
// SPDX-License-Identifier: GPL-2.0-only

package com.zextras.mailbox.util;

import com.google.common.collect.Maps;
import com.zimbra.common.mime.MimeConstants;
import com.zimbra.cs.account.ldap.LdapProvisioning;
import com.zimbra.cs.ldap.LdapException;
import com.zimbra.cs.ldap.unboundid.UBIDLdapClient;
import com.zimbra.cs.ldap.unboundid.UBIDLdapPoolConfig;
import com.zimbra.cs.mime.MimeTypeInfo;
import com.zimbra.cs.mime.MockMimeTypeInfo;
import com.zimbra.cs.mime.handler.MessageRFC822Handler;
import com.zimbra.cs.mime.handler.TextCalendarHandler;
import com.zimbra.cs.mime.handler.TextHtmlHandler;
import com.zimbra.cs.mime.handler.TextPlainHandler;
import com.zimbra.cs.mime.handler.UnknownTypeHandler;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Mock Provisioning with explicit MIME handling logic for testing.
 */
public final class LdapProvisioningWithMockMime extends LdapProvisioning {
    private final Map<String, List<MimeTypeInfo>> mimeConfig = Maps.newHashMap();

    public static LdapProvisioningWithMockMime get(UBIDLdapPoolConfig poolConfig) throws LdapException {
        final UBIDLdapClient client = UBIDLdapClient.createNew(poolConfig);
        return new LdapProvisioningWithMockMime(client);
    }
    public LdapProvisioningWithMockMime(UBIDLdapClient client) {
        // disable cache for testing, it makes the provisioning use mocked mime types
        super(CacheMode.OFF, client);
    }


    private void initializeMimeHandlers() {
        addMimeType(MimeConstants.CT_TEXT_PLAIN, createMimeTypeInfo(MimeConstants.CT_TEXT_PLAIN, TextPlainHandler.class.getName()));
        addMimeType(MimeConstants.CT_TEXT_HTML, createMimeTypeInfo(MimeConstants.CT_TEXT_HTML, TextHtmlHandler.class.getName(), "html", "htm"));
        addMimeType(MimeConstants.CT_TEXT_CALENDAR, createMimeTypeInfo(MimeConstants.CT_TEXT_CALENDAR, TextCalendarHandler.class.getName()));
        addMimeType(MimeConstants.CT_MESSAGE_RFC822, createMimeTypeInfo(MimeConstants.CT_MESSAGE_RFC822, MessageRFC822Handler.class.getName()));
    }

    private MockMimeTypeInfo createMimeTypeInfo(String mimeType, String handlerClass, String... fileExtensions) {
        MockMimeTypeInfo info = new MockMimeTypeInfo();
        info.setMimeTypes(mimeType);
        info.setHandlerClass(handlerClass);
        info.setIndexingEnabled(true);
        if (fileExtensions != null && fileExtensions.length > 0) {
            info.setFileExtensions(fileExtensions);
        }
        return info;
    }

    public void addMimeType(String mimeType, MockMimeTypeInfo info) {
        mimeConfig.put(mimeType, Collections.singletonList(info));
    }

    public void clearMimeHandlers() {
        mimeConfig.clear();
    }

    @Override
    public List<MimeTypeInfo> getMimeTypes(String mime) {
        List<MimeTypeInfo> result = mimeConfig.get(mime);
        if (result != null) {
            return result;
        } else {
            MockMimeTypeInfo info = new MockMimeTypeInfo();
            info.setHandlerClass(UnknownTypeHandler.class.getName());
            return Collections.singletonList(info);
        }
    }
    @Override
    public List<MimeTypeInfo> getAllMimeTypes() {
        List<MimeTypeInfo> result = new ArrayList<MimeTypeInfo>();
        for (List<MimeTypeInfo> entry : mimeConfig.values()) {
            result.addAll(entry);
        }
        return result;
    }
}

