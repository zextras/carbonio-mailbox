// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.service.mail;

import java.io.ByteArrayInputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.common.collect.Maps;
import com.zimbra.common.account.Key;
import com.zimbra.common.mime.MimeConstants;
import com.zimbra.common.soap.Element;
import com.zimbra.common.soap.MailConstants;
import com.zimbra.common.soap.SoapProtocol;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.mailbox.Folder;
import com.zimbra.cs.mailbox.MailItem;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mailbox.MailboxManager;
import com.zimbra.cs.mailbox.MailboxTestUtil;
import com.zimbra.cs.mailbox.OperationContext;
import com.zimbra.cs.service.AuthProvider;
import com.zimbra.cs.service.MockHttpServletRequest;
import com.zimbra.soap.MockSoapEngine;
import com.zimbra.soap.SoapEngine;
import com.zimbra.soap.SoapServlet;
import com.zimbra.soap.ZimbraSoapContext;

import junit.framework.Assert;

public class GetContactBackupListTest {
    @BeforeClass
    public static void init() throws Exception {
        MailboxTestUtil.initServer();
        Provisioning prov = Provisioning.getInstance();
        Map<String, Object> attrs = Maps.newHashMap();
        prov.createAccount("test@zimbra.com", "secret", attrs);
    }

    @Before
    public void setUp() throws Exception {
        MailboxTestUtil.clearData();
    }

    @Test
    public void testGetContactBackupListXML() throws Exception {
        Account acct = Provisioning.getInstance().get(Key.AccountBy.name, "test@zimbra.com");
        Mailbox mbox = MailboxManager.getInstance().getMailboxByAccount(acct);
        Folder folder = mbox.createFolder(null, "Briefcase/ContactsBackup",
            new Folder.FolderOptions().setDefaultView(MailItem.Type.DOCUMENT));
        OperationContext octxt = new OperationContext(acct);
        // Upload the contacts backup file to ContactsBackup folder in briefcase
        mbox.createDocument(octxt, folder.getId(), "backup_dummy_test1.tgz",
            MimeConstants.CT_APPLICATION_ZIMBRA_DOC, "author", "description",
            new ByteArrayInputStream("dummy data".getBytes()));
        mbox.createDocument(octxt, folder.getId(), "backup_dummy_test2.tgz",
            MimeConstants.CT_APPLICATION_ZIMBRA_DOC, "author", "description",
            new ByteArrayInputStream("dummy data".getBytes()));
        Element request = new Element.XMLElement(MailConstants.E_GET_CONTACT_BACKUP_LIST_REQUEST);
        Element response = new GetContactBackupList().handle(request, ServiceTestUtil.getRequestContext(acct));

        String expectedResponse = "<GetContactBackupListResponse xmlns=\"urn:zimbraMail\">\n"
                + "  <backups>\n"
                + "    <backup>backup_dummy_test1.tgz</backup>\n"
                + "    <backup>backup_dummy_test2.tgz</backup>\n"
                + "  </backups>\n"
                + "</GetContactBackupListResponse>";

        Assert.assertEquals("GetContactBackupListResponse is not as expected", expectedResponse, response.prettyPrint());
    }

    @Test
    public void testGetContactBackupListJSON() throws Exception {
        Account acct = Provisioning.getInstance().get(Key.AccountBy.name, "test@zimbra.com");
        Mailbox mbox = MailboxManager.getInstance().getMailboxByAccount(acct);
        Folder folder = mbox.createFolder(null, "Briefcase/ContactsBackup",
            new Folder.FolderOptions().setDefaultView(MailItem.Type.DOCUMENT));
        OperationContext octxt = new OperationContext(acct);
        // Upload the contacts backup file to ContactsBackup folder in briefcase
        mbox.createDocument(octxt, folder.getId(), "backup_dummy_test1.tgz",
            MimeConstants.CT_APPLICATION_ZIMBRA_DOC, "author", "description",
            new ByteArrayInputStream("dummy data".getBytes()));
        mbox.createDocument(octxt, folder.getId(), "backup_dummy_test2.tgz",
            MimeConstants.CT_APPLICATION_ZIMBRA_DOC, "author", "description",
            new ByteArrayInputStream("dummy data".getBytes()));
        Element request = new Element.JSONElement(MailConstants.E_GET_CONTACT_BACKUP_LIST_REQUEST);
        Map<String, Object> context = new HashMap<String, Object>();
        context.put(SoapEngine.ZIMBRA_CONTEXT, new ZimbraSoapContext(AuthProvider.getAuthToken(acct), acct.getId(), SoapProtocol.Soap12, SoapProtocol.SoapJS));
        context.put(SoapServlet.SERVLET_REQUEST, new MockHttpServletRequest("test".getBytes("UTF-8"), new URL("http://localhost:7070/service/FooRequest"), ""));
        context.put(SoapEngine.ZIMBRA_ENGINE, new MockSoapEngine(new MailService()));
        Element response = new GetContactBackupList().handle(request, context);
        String expectedResponse = "{\n"
                + "  \"backups\": [{\n"
                + "      \"backup\": [\n"
                + "        {\n"
                + "          \"_content\": \"backup_dummy_test1.tgz\"\n"
                + "        },\n"
                + "        {\n"
                + "          \"_content\": \"backup_dummy_test2.tgz\"\n"
                + "        }]\n"
                + "    }],\n"
                + "  \"_jsns\": \"urn:zimbraMail\"\n"
                + "}";

        Assert.assertEquals("GetContactBackupListResponse is not as expected", expectedResponse, response.prettyPrint());
    }
}
