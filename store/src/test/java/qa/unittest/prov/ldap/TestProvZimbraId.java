// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package qa.unittest.prov.ldap;

import static org.junit.Assert.*;

import com.google.common.collect.Maps;
import com.zimbra.common.account.Key;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.util.ByteUtil;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.AuthToken;
import com.zimbra.cs.account.Cos;
import com.zimbra.cs.account.Domain;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.service.AuthProvider;
import com.zimbra.cs.service.FileUploadServlet;
import com.zimbra.cs.service.FileUploadServlet.Upload;
import java.io.ByteArrayInputStream;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Map;
import org.junit.*;

public class TestProvZimbraId extends LdapTest {

  private static final String ZIMBRA_ID = "1234567890@" + genTestId();

  private static LdapProvTestUtil provUtil;
  private static Provisioning prov;
  private static Domain domain;

  @BeforeClass
  public static void init() throws Exception {
    provUtil = new LdapProvTestUtil();
    prov = provUtil.getProv();
    domain = provUtil.createDomain(baseDomainName());
  }

  @AfterClass
  public static void cleanup() throws Exception {
    Cleanup.deleteAll(baseDomainName());
  }

  @Test
  public void createAccountWithZimbraId() throws Exception {
    String zimbraId = ZIMBRA_ID;

    Map<String, Object> attrs = Maps.newHashMap();
    attrs.put(Provisioning.A_zimbraId, zimbraId);
    Account acct = provUtil.createAccount(genAcctNameLocalPart(), domain, attrs);
    assertEquals(zimbraId, acct.getId());

    // get account by id
    Account acctById = prov.get(Key.AccountBy.id, zimbraId);
    assertNotNull(acctById);
  }

  @Test
  public void createAccountWithInvalidZimbraId() throws Exception {
    String zimbraId = "containing:colon";

    Map<String, Object> attrs = Maps.newHashMap();
    attrs.put(Provisioning.A_zimbraId, zimbraId);

    boolean caughtException = false;
    try {
      Account acct = provUtil.createAccount(genAcctNameLocalPart(), domain, attrs);
    } catch (ServiceException e) {
      assertEquals(ServiceException.INVALID_REQUEST, e.getCode());
      caughtException = true;
      ;
    }
    assertTrue(caughtException);
  }

  @Test
  @Ignore // no longer supported/allowed
  public void createAccountWithCosName() throws Exception {
    // create a COS
    String cosName = genCosName();
    Cos cos = provUtil.createCos(cosName);

    Map<String, Object> attrs = Maps.newHashMap();
    attrs.put(Provisioning.A_zimbraCOSId, cosName); // use cos name instead of id
    Account acct = provUtil.createAccount(genAcctNameLocalPart(), domain, attrs);

    Cos acctCos = prov.getCOS(acct);
    assertEquals(cos.getName(), acctCos.getName());
    assertEquals(cos.getId(), acctCos.getId());
  }

  @Test
  public void createAccountWithCosId() throws Exception {
    // create a COS
    String cosName = genCosName();
    Cos cos = provUtil.createCos(cosName);

    Map<String, Object> attrs = Maps.newHashMap();
    attrs.put(Provisioning.A_zimbraCOSId, cos.getId());
    Account acct = provUtil.createAccount(genAcctNameLocalPart(), domain, attrs);

    Cos acctCos = prov.getCOS(acct);
    assertEquals(cos.getName(), acctCos.getName());
    assertEquals(cos.getId(), acctCos.getId());
  }

  @Test
  public void testFileUpload() throws Exception {
    Account acct = provUtil.createAccount(genAcctNameLocalPart(), domain);

    int bodyLen = 128;
    byte[] body = new byte[bodyLen];
    SecureRandom sr = new SecureRandom();
    sr.nextBytes(body);

    Upload ulSaved =
        FileUploadServlet.saveUpload(
            new ByteArrayInputStream(body), "zimbraId-test", "text/plain", acct.getId());
    // System.out.println("Upload id is: " + ulSaved.getId());

    AuthToken authToken = AuthProvider.getAuthToken(acct);
    Upload ulFetched = FileUploadServlet.fetchUpload(acct.getId(), ulSaved.getId(), authToken);

    assertEquals(ulSaved.getId(), ulFetched.getId());
    assertEquals(ulSaved.getName(), ulFetched.getName());
    assertEquals(ulSaved.getSize(), ulFetched.getSize());
    assertEquals(ulSaved.getContentType(), ulFetched.getContentType());
    assertEquals(ulSaved.toString(), ulFetched.toString());

    byte[] bytesUploaded = ByteUtil.getContent(ulFetched.getInputStream(), -1);
    assertTrue(Arrays.equals(body, bytesUploaded));
  }
}
