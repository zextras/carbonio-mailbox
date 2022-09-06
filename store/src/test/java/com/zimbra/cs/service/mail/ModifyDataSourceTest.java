// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.service.mail;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.google.common.collect.Maps;
import com.zimbra.common.account.Key;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.Element;
import com.zimbra.common.soap.MailConstants;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.MockProvisioning;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.ldap.LdapConstants;
import com.zimbra.cs.mailbox.MailboxTestUtil;
import com.zimbra.soap.type.DataSource.ConnectionType;
import java.util.Map;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

public class ModifyDataSourceTest {

  @Rule public static TestName testInfo = new TestName();

  private static String NAME_PREFIX;

  @BeforeClass
  public static void init() throws Exception {
    MailboxTestUtil.initServer();
    MockProvisioning prov = new MockProvisioning();
    Provisioning.setInstance(prov);

    Map<String, Object> attrs = Maps.newHashMap();
    prov.createDomain("zimbra.com", attrs);

    attrs = Maps.newHashMap();
    prov.createAccount("test@zimbra.com", "secret", attrs);
  }

  @Before
  public void setUp() throws Exception {
    NAME_PREFIX =
        String.format("%s-%s", ModifyDataSourceTest.class.getSimpleName(), testInfo.getMethodName())
            .toLowerCase();
    MailboxTestUtil.clearData();
  }

  @Test
  public void testValidateDataSourceEmail() {
    try {
      Account acct = null;
      acct = Provisioning.getInstance().get(Key.AccountBy.name, "test@zimbra.com");

      Element request = new Element.XMLElement(MailConstants.E_CREATE_DATA_SOURCE_REQUEST);

      Element ds1 = request.addUniqueElement(MailConstants.E_DS_POP3);

      ds1.addAttribute(MailConstants.A_NAME, NAME_PREFIX + " testDataSourceEmailAddress1");
      ds1.addAttribute(MailConstants.A_DS_IS_ENABLED, LdapConstants.LDAP_FALSE);
      ds1.addAttribute(MailConstants.A_DS_HOST, "testhost.com");
      ds1.addAttribute(MailConstants.A_DS_PORT, "0");
      ds1.addAttribute(MailConstants.A_DS_USERNAME, "testuser1");
      ds1.addAttribute(MailConstants.A_DS_EMAIL_ADDRESS, "testuser2@testhost.com");
      ds1.addAttribute(MailConstants.A_DS_PASSWORD, "testpass");
      ds1.addAttribute(MailConstants.A_FOLDER, "1");
      ds1.addAttribute(MailConstants.A_DS_CONNECTION_TYPE, ConnectionType.cleartext.toString());

      new CreateDataSource().handle(request, ServiceTestUtil.getRequestContext(acct));

      Element request2 = new Element.XMLElement(MailConstants.E_CREATE_DATA_SOURCE_REQUEST);

      Element ds2 = request2.addUniqueElement(MailConstants.E_DS_POP3);

      ds2.addAttribute(MailConstants.A_NAME, NAME_PREFIX + " testDataSourceEmailAddress2");
      ds2.addAttribute(MailConstants.A_DS_IS_ENABLED, LdapConstants.LDAP_FALSE);
      ds2.addAttribute(MailConstants.A_DS_HOST, "testhost.com");
      ds2.addAttribute(MailConstants.A_DS_PORT, "0");
      ds2.addAttribute(MailConstants.A_DS_USERNAME, "testuser2");
      ds2.addAttribute(MailConstants.A_DS_EMAIL_ADDRESS, "testuser3@testhost.com");
      ds2.addAttribute(MailConstants.A_DS_PASSWORD, "testpass");
      ds2.addAttribute(MailConstants.A_FOLDER, "1");
      ds2.addAttribute(MailConstants.A_DS_CONNECTION_TYPE, ConnectionType.cleartext.toString());

      new CreateDataSource().handle(request2, ServiceTestUtil.getRequestContext(acct));

      // try to modify data source 1 to have an existing data source email address.
      ds1.addAttribute(MailConstants.A_DS_EMAIL_ADDRESS, "testuser3@testhost.com");

      try {
        ModifyDataSource.validateDataSourceEmail(acct, ds1);
        fail("datasource email validation failed");
      } catch (ServiceException e) {
        String expected = "data source already exists: testuser3@testhost.com";
        assertTrue(e.getMessage().indexOf(expected) != -1);
        assertNotNull(e);
      }
    } catch (Exception e) {
      fail("No exception should be thrown" + e);
    }
  }
}
