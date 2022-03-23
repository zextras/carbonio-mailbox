package com.zimbra.cs.service.mail;

import com.google.common.collect.Maps;
import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.Server;
import com.zimbra.cs.mailbox.MailboxTestUtil;
import com.zimbra.qa.unittest.TestUtil;
import java.util.Map;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class GetPreviewTest {

  private static Server localServer = null;

  @BeforeClass
  public static void init() throws Exception {
    MailboxTestUtil.initServer();
    Provisioning prov = Provisioning.getInstance();

    Map<String, Object> attrs = Maps.newHashMap();
    prov.createAccount("test@zimbra.com", "secret", attrs);

    localServer = prov.getLocalServer();
  }

  @Before
  public void setUp() throws Exception {
    MailboxTestUtil.clearData();
  }

  @Test
  public void shouldGetServerBaseUrlWithoutPort() throws ServiceException {
      String baseUrl = "http://localhost";
      System.out.println("LOCALSERVER URL "+ TestUtil.getBaseUrl(localServer));
      Assert.assertEquals(baseUrl, GetPreview.getServerBaseUrl(localServer, false));
  }

  @Test
  public void shouldGetServerBaseUrlWithPort() throws ServiceException {
    String baseUrl = "http://localhost:0";
    System.out.println("LOCALSERVER URL "+ TestUtil.getBaseUrl(localServer));
    Assert.assertEquals(baseUrl, GetPreview.getServerBaseUrl(localServer, true));
  }

}