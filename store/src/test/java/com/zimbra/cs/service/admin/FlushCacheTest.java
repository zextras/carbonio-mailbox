package com.zimbra.cs.service.admin;

import static com.zimbra.cs.account.Provisioning.SERVICE_MAILCLIENT;

import com.zextras.mailbox.soap.SoapTestSuite;
import com.zimbra.common.account.ZAttrProvisioning;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.Domain;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.Server;
import com.zimbra.cs.account.accesscontrol.ACLUtil;
import com.zimbra.cs.account.accesscontrol.GranteeType;
import com.zimbra.cs.account.accesscontrol.Right;
import com.zimbra.cs.account.accesscontrol.RightManager;
import com.zimbra.cs.account.accesscontrol.RightModifier;
import com.zimbra.cs.account.accesscontrol.ZimbraACE;
import com.zimbra.soap.admin.message.FlushCacheRequest;
import com.zimbra.soap.admin.type.CacheEntrySelector;
import com.zimbra.soap.admin.type.CacheEntryType;
import com.zimbra.soap.admin.type.CacheSelector;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("api")
public class FlushCacheTest extends SoapTestSuite {


    private static Provisioning provisioning;

    @BeforeAll
    public static void setUp() {
        provisioning = Provisioning.getInstance();

    }

    @Test
    public void shouldFlushCacheAccount_IfHasServerFlushRights() throws Exception {
        final Account account = getCreateAccountFactory().create();
        final Account domainAdminAccount = getCreateAccountFactory()
                .withAttribute(ZAttrProvisioning.A_zimbraIsDelegatedAdminAccount, "TRUE").create();
        grantFlushCacheRight(domainAdminAccount);

        final CacheSelector cacheSelector = cacheSelector(CacheEntryType.account, account.getId());

        assertOk(doCall(domainAdminAccount, cacheSelector));
    }

    @Test
    public void shouldFlushCacheDomain_IfHasServerFlushRights() throws Exception {
        final Account domainAdminAccount = getCreateAccountFactory()
                .withAttribute(ZAttrProvisioning.A_zimbraIsDelegatedAdminAccount, "TRUE").create();
        final Domain domain = provisioning.getDomainById(domainAdminAccount.getDomainId());
        grantFlushCacheRight(domainAdminAccount);

        final CacheSelector cacheSelector = cacheSelector(CacheEntryType.domain, domain.getId());

        assertOk(doCall(domainAdminAccount, cacheSelector));
    }

    @Test
    public void shouldFlushCacheOfAnotherDomain_IfHasServerFlushRights() throws Exception {
        final Account domainAdminAccount = getCreateAccountFactory()
                .withAttribute(ZAttrProvisioning.A_zimbraIsDelegatedAdminAccount, "TRUE").create();
        final Domain domain = provisioning.createDomain("otherDomain.com", new HashMap<>());
        grantFlushCacheRight(domainAdminAccount);

        final CacheSelector cacheSelector = cacheSelector(CacheEntryType.domain, domain.getId());

        assertOk(doCall(domainAdminAccount, cacheSelector));
    }

    @Test
    public void shouldFlushCacheServer_IfHasServerFlushRights() throws Exception {
        final Account domainAdminAccount = getCreateAccountFactory()
                .withAttribute(ZAttrProvisioning.A_zimbraIsDelegatedAdminAccount, "TRUE").create();
        final Server server = provisioning.getServer(domainAdminAccount);
        grantFlushCacheRight(domainAdminAccount);

        final CacheSelector cacheSelector = cacheSelector(CacheEntryType.server, server.getId());

        assertOk(doCall(domainAdminAccount, cacheSelector));
    }

    @Test
    public void shouldFlushCacheOfAnotherServer_IfHasServerFlushRights() throws Exception {
        final Account domainAdminAccount = getCreateAccountFactory()
                .withAttribute(ZAttrProvisioning.A_zimbraIsDelegatedAdminAccount, "TRUE").create();
        final Server server = provisioning.createServer(
                "otherServer",
                new HashMap<>(Map.of(ZAttrProvisioning.A_zimbraServiceEnabled, SERVICE_MAILCLIENT)));
        grantFlushCacheRight(domainAdminAccount);

        final CacheSelector cacheSelector = cacheSelector(CacheEntryType.server, server.getId());

        assertOk(doCall(domainAdminAccount, cacheSelector));
    }

    @Test
    public void shouldReject_IfNoServerFlushRights() throws Exception {
        final Account account = getCreateAccountFactory().create();
        final Account domainAdminAccount = getCreateAccountFactory()
                .withAttribute(ZAttrProvisioning.A_zimbraIsDelegatedAdminAccount, "TRUE").create();

        final CacheSelector cacheSelector = cacheSelector(CacheEntryType.account, account.getId());

        assertFailure(doCall(domainAdminAccount, cacheSelector));
    }

    private void grantFlushCacheRight(Account domainAdminAccount) throws Exception {
        final Server server = provisioning.getServer(domainAdminAccount);

        final Set<ZimbraACE> aces = new HashSet<>();
        aces.add(new ZimbraACE(
                domainAdminAccount.getId(),
                GranteeType.GT_USER,
                RightManager.getInstance().getRight(Right.RT_flushCache),
                RightModifier.RM_CAN_DELEGATE,
                null));
        ACLUtil.grantRight(provisioning, server, aces);
    }

    private CacheSelector cacheSelector(CacheEntryType cacheEntryType, String targetId) {
        final CacheSelector cacheSelector = new CacheSelector(true, cacheEntryType.toString());
        final CacheEntrySelector cacheEntrySelector = new CacheEntrySelector(CacheEntrySelector.CacheEntryBy.id,
                targetId);
        cacheSelector.addEntry(cacheEntrySelector);

        return cacheSelector;
    }

    private HttpResponse doCall(Account caller, CacheSelector cacheSelector) throws Exception {
        final FlushCacheRequest request = new FlushCacheRequest(cacheSelector);
        return getSoapClient().newRequest().setCaller(caller).setSoapBody(request).execute();
    }

    private void assertOk(HttpResponse response) {
        assertStatus(response, HttpStatus.SC_OK);
    }

    private void assertFailure(HttpResponse response) {
        assertStatus(response, HttpStatus.SC_UNPROCESSABLE_ENTITY);
    }

    private void assertStatus(HttpResponse response, int status) {
        Assertions.assertEquals(status, response.getStatusLine().getStatusCode());
    }
}