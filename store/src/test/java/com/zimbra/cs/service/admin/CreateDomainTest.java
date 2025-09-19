package com.zimbra.cs.service.admin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.zextras.mailbox.MailboxTestSuite;
import com.zimbra.cs.account.AccountServiceException;
import com.zimbra.cs.account.Provisioning;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class CreateDomainTest extends MailboxTestSuite {

    private static Provisioning provisioning;

    @BeforeAll
    static void setUp() {
        provisioning = Provisioning.getInstance();
    }

    @Test
    void whenDomainMaxAccountIsNegative_creatingANewDomain_throwsAnExpectedException() {
        final Map<String, Object> extraAttr = new HashMap<>();
        extraAttr.put(Provisioning.A_zimbraDomainMaxAccounts, "-1");

        final AccountServiceException actualException = assertThrows(AccountServiceException.class, () -> provisioning.createDomain("test.domain.com", extraAttr));

        assertEquals("zimbraDomainMaxAccounts value(-1) smaller than minimum allowed: 0", actualException.getMessage());
    }
}