// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.service.admin;

import static org.junit.jupiter.api.Assertions.*;

import com.google.common.collect.Maps;
import com.zextras.mailbox.MailboxTestSuite;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.Element;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.mailbox.MailboxManager;
import com.zimbra.cs.service.mail.ServiceTestUtil;
import com.zimbra.soap.JaxbUtil;
import com.zimbra.soap.ZimbraSoapContext;
import com.zimbra.soap.admin.message.ContactBackupRequest;
import com.zimbra.soap.admin.message.ContactBackupRequest.Operation;
import com.zimbra.soap.admin.message.ContactBackupResponse;
import com.zimbra.soap.admin.type.ContactBackupServer;
import com.zimbra.soap.admin.type.ContactBackupServer.ContactBackupStatus;
import com.zimbra.soap.admin.type.ServerSelector;
import com.zimbra.soap.admin.type.ServerSelector.ServerBy;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class ContactBackupApiTest extends MailboxTestSuite {
    private static final String DOMAIN_NAME = "zimbra.com";
    private static final String BUG_NUMBER = "zcs3594";
    private static final String ADMIN = "admin_" + BUG_NUMBER + "@" + DOMAIN_NAME;
    private static Provisioning prov = null;
    private static Account admin = null;

    @BeforeAll
    public static void init() throws Exception {
        prov = Provisioning.getInstance();
        prov.createDomain(DOMAIN_NAME, Maps.newHashMap());
        Map<String, Object> attrs = Maps.newHashMap();
        attrs.put(Provisioning.A_zimbraId, UUID.randomUUID().toString());
        attrs.put(Provisioning.A_zimbraIsAdminAccount, true); // set admin account
        prov.createAccount(ADMIN, "secret", attrs);
        admin = prov.getAccountByName(ADMIN);
        MailboxManager.getInstance().getMailboxByAccount(admin);
    }


 @Test
 void testContactBackupApiWithStart() throws Exception {
  ContactBackupRequest cbReq = new ContactBackupRequest();
  cbReq.setOp(Operation.start);
  cbReq.addServer(new ServerSelector(ServerBy.name, "test1.com"));
  cbReq.addServer(new ServerSelector(ServerBy.name, "test2.com"));
  cbReq.addServer(new ServerSelector(ServerBy.name, "test3.com"));
  Element request = JaxbUtil.jaxbToElement(cbReq);
  Element response  = null;

  try {
   response = new MockContactBackup().handle(request, ServiceTestUtil.getRequestContext(admin));
  } catch (ServiceException se) {
   fail("ServiceException must not be thrown.");
  }
  if (response == null) {
   fail("Response must be received.");
  }
  ContactBackupResponse cbResp = JaxbUtil.elementToJaxb(response);
  assertNotNull(cbResp.getServers());
  List<ContactBackupServer> servers = cbResp.getServers();
  for (ContactBackupServer server : servers) {
   assertEquals(server.getStatus(), ContactBackupStatus.started);
  }
 }

 @Test
 void testContactBackupApiWithStop() throws Exception {
  ContactBackupRequest cbReq = new ContactBackupRequest();
  cbReq.setOp(Operation.stop);
  cbReq.addServer(new ServerSelector(ServerBy.name, "test1.com"));
  cbReq.addServer(new ServerSelector(ServerBy.name, "test2.com"));
  cbReq.addServer(new ServerSelector(ServerBy.name, "test3.com"));
  Element request = JaxbUtil.jaxbToElement(cbReq);
  Element response  = null;

  try {
   response = new MockContactBackup().handle(request, ServiceTestUtil.getRequestContext(admin));
  } catch (ServiceException se) {
   fail("ServiceException must not be thrown.");
  }
  if (response == null) {
   fail("Response must be received.");
  }
  ContactBackupResponse cbResp = JaxbUtil.elementToJaxb(response);
  assertNotNull(cbResp.getServers());
  List<ContactBackupServer> servers = cbResp.getServers();
  for (ContactBackupServer server : servers) {
   assertEquals(server.getStatus(), ContactBackupStatus.stopped);
  }
 }

    public class MockContactBackup extends ContactBackup {
        @Override
        protected List<ContactBackupServer> startContactBackup(List<ServerSelector> selectors, Map<String, Object> context, ZimbraSoapContext zsc) throws ServiceException {
            List<ContactBackupServer> servers = new ArrayList<ContactBackupServer>();
            for (ServerSelector serverSelector : selectors) {
                servers.add(new ContactBackupServer(serverSelector.getKey(), ContactBackupStatus.started));
            }
            return servers;
        }

        @Override
        protected List<ContactBackupServer> stopContactBackup(List<ServerSelector> selectors, Map<String, Object> context, ZimbraSoapContext zsc) throws ServiceException {
            List<ContactBackupServer> servers = new ArrayList<ContactBackupServer>();
            for (ServerSelector serverSelector : selectors) {
                servers.add(new ContactBackupServer(serverSelector.getKey(), ContactBackupStatus.stopped));
            }
            return servers;
        }
    }
}
