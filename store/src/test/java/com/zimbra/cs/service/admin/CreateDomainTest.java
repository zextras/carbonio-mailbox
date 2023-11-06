package com.zimbra.cs.service.admin;

import com.zextras.mailbox.util.MailboxTestUtil;
import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.AccountServiceException;
import com.zimbra.cs.account.Provisioning;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
}