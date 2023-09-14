package com.zextras.mailbox.usecase.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.common.collect.Maps;
import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.NamedEntry;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.mailbox.ACL;
import com.zimbra.cs.mailbox.MailboxTestUtil;
import com.zimbra.cs.mailbox.OperationContext;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class GranteeServiceTest {
  private GranteeService granteeService;
  private final String granteeName = "granteeName";
  private final String granteeEmail = granteeName + "@test.com";
  private final String granteeId = UUID.randomUUID().toString();

  @BeforeEach
  void setUp() throws Exception {
    MailboxTestUtil.initServer();
    Provisioning provisioning = Provisioning.getInstance();
    Map<String, Object> granteeAttrs = Maps.newHashMap();
    granteeAttrs.put(Provisioning.A_zimbraId, granteeId);

    provisioning.createAccount(granteeEmail, "secret", granteeAttrs);

    granteeService = new GranteeService(provisioning);
  }

  @Test
  void shouldReturnNamedEntryWhenLookupGranteeByEmailAddress() throws ServiceException {
    NamedEntry namedEntry = granteeService.lookupGranteeByEmailAddress(granteeEmail);
    assertEquals(granteeEmail, namedEntry.getName());
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
    assertEquals(granteeEmail, namedEntry.getName());
  }
}
