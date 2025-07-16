package com.zimbra.cs.service.util;

import com.zextras.mailbox.MailboxTestSuite;
import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.Provisioning;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

class DomainUtilsTest extends MailboxTestSuite {
    private static Provisioning provisioning;

    @BeforeAll
    static void setUp() {
        provisioning = Provisioning.getInstance();
    }

    @Test
    void getDomainsWithConflictingVHosts() throws ServiceException {
        Map<String, Object> currentDomainAttrs = new HashMap<>();
        currentDomainAttrs.put(Provisioning.A_zimbraVirtualHostname, "virtual.com");
        var currentDomain = provisioning.createDomain("current.domain.com", currentDomainAttrs);

        Map<String, Object> conflictingDomainAttrs = new HashMap<>();
        conflictingDomainAttrs.put(Provisioning.A_zimbraVirtualHostname, "virtual.com");
        var conflictingDomain = provisioning.createDomain("conflicting.domain.com", conflictingDomainAttrs);

        var domainsWithConflictingVHosts = DomainUtils.getDomainsWithConflictingVHosts(currentDomain, new String [] {"virtual.com"}, provisioning);

        Assertions.assertEquals(1, domainsWithConflictingVHosts.size());
        Assertions.assertEquals(conflictingDomain.getId(), domainsWithConflictingVHosts.get(conflictingDomain.getDomainName()).getId());
    }
}