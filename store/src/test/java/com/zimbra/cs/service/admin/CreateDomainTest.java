package com.zimbra.cs.service.admin;

import com.zextras.mailbox.util.MailboxTestUtil;
import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.AccountServiceException;
import com.zimbra.cs.account.Domain;
import com.zimbra.cs.account.Provisioning;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static com.zimbra.common.account.ZAttrProvisioning.A_carbonioIsInitializedForDelegation;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CreateDomainTest {

    private static Provisioning provisioning;

    @BeforeEach
    void setUp() throws Exception {
        MailboxTestUtil.setUp();
        provisioning = Provisioning.getInstance();
    }

    @AfterEach
    void tearDown() throws ServiceException {
        MailboxTestUtil.tearDown();
    }

    @Test
    void whenDomainMaxAccountIsNegative_creatingANewDomain_throwsAnExpectedException() {
        final Map<String, Object> extraAttr = new HashMap<>();
        extraAttr.put(Provisioning.A_zimbraDomainMaxAccounts, "-1");

        final AccountServiceException actualException = assertThrows(AccountServiceException.class, () -> provisioning.createDomain("test.domain.com", extraAttr));

        assertEquals("zimbraDomainMaxAccounts value(-1) smaller than minimum allowed: 0", actualException.getMessage());
    }

    @Test
    void created_domain_is_not_initialized() throws ServiceException {
        Domain domain = provisioning.createDomain("test.domain.com", new HashMap<>());

        assertTrue(domain.getAttrs(false).containsKey(A_carbonioIsInitializedForDelegation));
        assertFalse(domain.getBooleanAttr(A_carbonioIsInitializedForDelegation, false));
    }
}