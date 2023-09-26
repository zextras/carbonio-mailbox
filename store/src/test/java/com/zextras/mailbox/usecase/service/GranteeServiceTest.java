package com.zextras.mailbox.usecase.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.common.collect.Maps;
import com.zextras.mailbox.usecase.MailboxTestUtil;
import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.NamedEntry;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.mailbox.ACL;
import com.zimbra.cs.mailbox.OperationContext;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class GranteeServiceTest {
  private GranteeService granteeService;
  private Provisioning provisioning;
  private final String granteeName = "granteeName";
  private final String granteeEmail = granteeName + "@" + MailboxTestUtil.DEFAULT_DOMAIN;
  private String granteeId;

  @BeforeEach
  void setUp() throws Exception {
    MailboxTestUtil.setUp();
    provisioning = Provisioning.getInstance(Provisioning.CacheMode.OFF);
    Map<String, Object> granteeAttrs = Maps.newHashMap();
    granteeAttrs.put(Provisioning.A_zimbraMailHost, MailboxTestUtil.SERVER_NAME);
    granteeId = provisioning.createAccount(granteeEmail, "secret", granteeAttrs).getId();
    granteeService = new GranteeService(provisioning);
  }

  @AfterEach
  void tearDown() {
    MailboxTestUtil.tearDown();
  }

  @Test
  void shouldReturnNamedEntryWhenLookupGranteeByEmailAddress() throws ServiceException {
    NamedEntry namedEntry = granteeService.lookupGranteeByEmailAddress(granteeEmail);
    assertEquals(granteeEmail.toLowerCase(), namedEntry.getName().toLowerCase());
  }

  @Test
  void shouldLookupGranteeByZimbraId() {
    NamedEntry namedEntry = granteeService.lookupGranteeByZimbraId(granteeId, ACL.GRANTEE_USER);
    assertEquals(granteeId, namedEntry.getId());
  }

  @Test
  void shouldLookupGranteeByName() throws ServiceException {
    OperationContext granteeContext = new OperationContext(granteeId);
    granteeContext.setmAuthTokenAccountId(granteeId);

    NamedEntry namedEntry =
        granteeService.lookupGranteeByName(granteeName, ACL.GRANTEE_USER, granteeContext);
    assertEquals(granteeEmail.toLowerCase(), namedEntry.getName().toLowerCase());
  }
}
