package com.zimbra.cs.service.admin;

import com.zextras.mailbox.soap.SoapTestSuite;
import com.zextras.mailbox.util.MailboxTestUtil;
import com.zimbra.common.account.ZAttrProvisioning;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.Server;
import com.zimbra.cs.account.accesscontrol.*;
import com.zimbra.soap.admin.message.FlushCacheRequest;
import com.zimbra.soap.admin.type.CacheEntrySelector;
import com.zimbra.soap.admin.type.CacheEntryType;
import com.zimbra.soap.admin.type.CacheSelector;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;
@Tag("api")
public class FlushCacheTest extends SoapTestSuite {

    private static MailboxTestUtil.AccountCreator.Factory accountCreatorFactory;
    private static Provisioning provisioning;

    @BeforeAll
    public static void setUp() throws Exception {
        provisioning = Provisioning.getInstance();
        accountCreatorFactory = new MailboxTestUtil.AccountCreator.Factory(provisioning);
    }

    @Test
    public void shouldFlushCacheAccount_IfHasServerFlushRights() throws Exception {
        final Account accountToFlush = accountCreatorFactory.get().create();
        final Account domainAdminAccount = accountCreatorFactory.get()
                .withAttribute(ZAttrProvisioning.A_zimbraIsDelegatedAdminAccount, "TRUE").create();
        final Server target = provisioning.getServer(accountToFlush);

        final Set<ZimbraACE> aces = new HashSet<>();
        aces.add(new ZimbraACE(
                domainAdminAccount.getId(),
                GranteeType.GT_USER,
                RightManager.getInstance().getRight(Right.RT_domainAdminServerRights),
                RightModifier.RM_CAN_DELEGATE,
                null));
        ACLUtil.grantRight(provisioning, target, aces);

        final CacheSelector cacheSelector = new CacheSelector(false, CacheEntryType.account.toString());
        final CacheEntrySelector cacheEntrySelector = new CacheEntrySelector(CacheEntrySelector.CacheEntryBy.id,
                accountToFlush.getId());
        cacheSelector.addEntry(cacheEntrySelector);
        final FlushCacheRequest request = new FlushCacheRequest(cacheSelector);
        final HttpResponse response = getSoapClient().newRequest().setCaller(domainAdminAccount)
                .setSoapBody(request).execute();

        System.out.println(new String(response.getEntity().getContent().readAllBytes()));
        Assertions.assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());
    }

}