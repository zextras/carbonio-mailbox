package com.zextras.mailbox.usecase;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.zextras.mailbox.usecase.factory.ItemIdFactory;
import com.zextras.mailbox.usecase.ldap.GrantType;
import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.*;
import com.zimbra.cs.ldap.ZLdapFilter;
import com.zimbra.cs.ldap.ZLdapFilterFactory;
import com.zimbra.cs.mailbox.*;
import com.zimbra.cs.service.util.ItemId;
import io.vavr.control.Try;
import java.lang.reflect.Field;
import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class RevokeOrphanAccessFolderActionUseCaseTest {

  private MailboxManager mailboxManager;
  private RevokeOrphanAccessFolderActionUseCase revokeOrphanAccessFolderActionUseCase;
  private ItemIdFactory itemIdFactory;
  private Mailbox userMailbox;
  private OperationContext operationContext;
  private ItemId itemId;
  private Mailbox.FolderNode folderNode;
  private Folder folder;
  private ACL acl;
  private ACL.Grant grant;
  private Provisioning provisioning;

  @BeforeEach
  void setUp() throws NoSuchFieldException, IllegalAccessException {
    mailboxManager = mock(MailboxManager.class);
    itemIdFactory = mock(ItemIdFactory.class);
    revokeOrphanAccessFolderActionUseCase =
        new RevokeOrphanAccessFolderActionUseCase(mailboxManager, itemIdFactory);
    Field zLdapInstanceFactory = ZLdapFilterFactory.class.getDeclaredField("SINGLETON");
    zLdapInstanceFactory.setAccessible(true);
    zLdapInstanceFactory.set(null, new DummyLdapFilterFactory());
    userMailbox = mock(Mailbox.class);
    operationContext = mock(OperationContext.class);
    itemId = mock(ItemId.class);
    folderNode = mock(Mailbox.FolderNode.class);
    folder = mock(Folder.class);
    acl = mock(ACL.class);
    grant = mock(ACL.Grant.class);
    provisioning = mock(Provisioning.class);
    Provisioning.setInstance(provisioning);
  }

  @Test
  void shouldBeSuccessAfterRevokeOrphanAccessOnFolder() throws ServiceException {
    final String accountId = "account-id123";
    final String folderId = accountId + ":1";
    final String granteeId = "zimbra-1";

    when(provisioning.searchDirectory(any())).thenReturn(List.of());
    when(folder.getFolderId()).thenReturn(1);
    when(userMailbox.getAccount())
        .thenReturn(new Account("test", accountId, new HashMap<>(), new HashMap<>(), provisioning));
    when(itemId.getId()).thenReturn(1);
    when(mailboxManager.getMailboxByAccountId(accountId, true)).thenReturn(userMailbox);
    when(userMailbox.getFolderTree(operationContext, itemId, true)).thenReturn(folderNode);
    when(itemIdFactory.create(folderId, accountId)).thenReturn(itemId);
    when(folderNode.getFolder()).thenReturn(folder);
    when(folder.getFolderId()).thenReturn(1);
    when(folder.getId()).thenReturn(1);
    when(userMailbox.getEffectivePermissions(
            operationContext, folderNode.getFolder().getId(), MailItem.Type.FOLDER))
        .thenReturn((short) 0x111);
    when(folder.getACL()).thenReturn(acl);
    when(acl.getGrants()).thenReturn(List.of(grant));
    when(grant.getGranteeId()).thenReturn(granteeId);
    when(grant.getGranteeType()).thenReturn(GrantType.GRANTEE_USER.getGranteeNumber());

    Try<Void> result =
        revokeOrphanAccessFolderActionUseCase.revokeOrphanAccess(
            operationContext, accountId, folderId, granteeId, "usr");
    assertTrue(result.isSuccess());
    verify(userMailbox, atLeast(1)).revokeAccess(operationContext, itemId.getId(), granteeId);
  }

  @Test
  void shouldReturnFailureWhenEntriesFound() throws ServiceException {
    final String accountId = "account-id123";
    final String folderId = accountId + ":1";
    final String granteeId = "zimbra-1";

    when(folder.getFolderId()).thenReturn(1);
    when(provisioning.searchDirectory(any())).thenReturn(List.of(mock(NamedEntry.class)));
    when(userMailbox.getAccount())
        .thenReturn(new Account("test", accountId, new HashMap<>(), new HashMap<>(), provisioning));
    when(itemId.getId()).thenReturn(1);
    when(mailboxManager.getMailboxByAccountId(accountId, true)).thenReturn(userMailbox);
    when(userMailbox.getFolderTree(operationContext, itemId, true)).thenReturn(folderNode);
    when(itemIdFactory.create(folderId, accountId)).thenReturn(itemId);
    when(folderNode.getFolder()).thenReturn(folder);
    when(folder.getFolderId()).thenReturn(1);
    when(folder.getId()).thenReturn(1);
    when(userMailbox.getEffectivePermissions(
            operationContext, folderNode.getFolder().getId(), MailItem.Type.FOLDER))
        .thenReturn((short) 0x111);
    when(folder.getACL()).thenReturn(acl);
    when(acl.getGrants()).thenReturn(List.of(grant));
    when(grant.getGranteeId()).thenReturn(granteeId);
    when(grant.getGranteeType()).thenReturn(GrantType.GRANTEE_USER.getGranteeNumber());

    Try<Void> result =
        revokeOrphanAccessFolderActionUseCase.revokeOrphanAccess(
            operationContext, accountId, folderId, granteeId, "usr");
    assertTrue(result.isFailure());
  }
}

class DummyLdapFilterFactory extends ZLdapFilterFactory {

  @Override
  public String encodeValue(String value) {
    return value;
  }

  @Override
  public ZLdapFilter hasSubordinates() {
    return new DummyFilter(FilterId.TODO);
  }

  @Override
  public ZLdapFilter createdLaterOrEqual(String generalizedTime) {
    return new DummyFilter(FilterId.TODO);
  }

  @Override
  public ZLdapFilter anyEntry() {
    return new DummyFilter(FilterId.TODO);
  }

  @Override
  public ZLdapFilter fromFilterString(FilterId filterId, String filterString) {
    return new DummyFilter(FilterId.TODO);
  }

  @Override
  public ZLdapFilter andWith(ZLdapFilter filter, ZLdapFilter otherFilter) {
    return new DummyFilter(FilterId.TODO);
  }

  @Override
  public ZLdapFilter negate(ZLdapFilter filter) {
    return new DummyFilter(FilterId.TODO);
  }

  @Override
  public ZLdapFilter addrsExist(String[] addrs) {
    return new DummyFilter(FilterId.TODO);
  }

  @Override
  public ZLdapFilter allAccounts() {
    return new DummyFilter(FilterId.TODO);
  }

  @Override
  public ZLdapFilter allAccountsOnly() {
    return new DummyFilter(FilterId.TODO);
  }

  @Override
  public ZLdapFilter allAccountsOnlyByCos(String cosId) {
    return new DummyFilter(FilterId.TODO);
  }

  @Override
  public ZLdapFilter allAdminAccounts() {
    return new DummyFilter(FilterId.TODO);
  }

  @Override
  public ZLdapFilter allNonSystemAccounts() {
    return new DummyFilter(FilterId.TODO);
  }

  @Override
  public ZLdapFilter allNonSystemArchivingAccounts() {
    return new DummyFilter(FilterId.TODO);
  }

  @Override
  public ZLdapFilter allNonSystemInternalAccounts() {
    return new DummyFilter(FilterId.TODO);
  }

  @Override
  public ZLdapFilter accountByForeignPrincipal(String foreignPrincipal) {
    return new DummyFilter(FilterId.TODO);
  }

  @Override
  public ZLdapFilter accountById(String id) {
    return new DummyFilter(FilterId.TODO);
  }

  @Override
  public ZLdapFilter accountByMemberOf(String dynGroupId) {
    return new DummyFilter(FilterId.TODO);
  }

  @Override
  public ZLdapFilter accountByName(String name) {
    return new DummyFilter(FilterId.TODO);
  }

  @Override
  public ZLdapFilter adminAccountByRDN(String namingRdnAttr, String name) {
    return new DummyFilter(FilterId.TODO);
  }

  @Override
  public ZLdapFilter accountsHomedOnServer(String serverServiceHostname) {
    return new DummyFilter(FilterId.TODO);
  }

  @Override
  public ZLdapFilter accountsHomedOnServerAccountsOnly(String serverServiceHostname) {
    return new DummyFilter(FilterId.TODO);
  }

  @Override
  public ZLdapFilter homedOnServer(String serverServiceHostname) {
    return new DummyFilter(FilterId.TODO);
  }

  @Override
  public ZLdapFilter accountsOnServerAndCosHasSubordinates(
      String serverServiceHostname, String cosId) {
    return new DummyFilter(FilterId.TODO);
  }

  @Override
  public ZLdapFilter externalAccountsHomedOnServer(String serverServiceHostname) {
    return new DummyFilter(FilterId.TODO);
  }

  @Override
  public ZLdapFilter accountsByGrants(
      List<String> granteeIds, boolean includePublicShares, boolean includeAllAuthedShares) {
    return new DummyFilter(FilterId.TODO);
  }

  @Override
  public ZLdapFilter CMBSearchAccountsOnly() {
    return new DummyFilter(FilterId.TODO);
  }

  @Override
  public ZLdapFilter CMBSearchAccountsOnlyWithArchive() {
    return new DummyFilter(FilterId.TODO);
  }

  @Override
  public ZLdapFilter CMBSearchNonSystemResourceAccountsOnly() {
    return new DummyFilter(FilterId.TODO);
  }

  @Override
  public ZLdapFilter allAliases() {
    return new DummyFilter(FilterId.TODO);
  }

  @Override
  public ZLdapFilter allCalendarResources() {
    return new DummyFilter(FilterId.TODO);
  }

  @Override
  public ZLdapFilter calendarResourceByForeignPrincipal(String foreignPrincipal) {
    return new DummyFilter(FilterId.TODO);
  }

  @Override
  public ZLdapFilter calendarResourceById(String id) {
    return new DummyFilter(FilterId.TODO);
  }

  @Override
  public ZLdapFilter calendarResourceByName(String name) {
    return new DummyFilter(FilterId.TODO);
  }

  @Override
  public ZLdapFilter calendarResourcesHomedOnServer(String serverServiceHostname) {
    return new DummyFilter(FilterId.TODO);
  }

  @Override
  public ZLdapFilter allCoses() {
    return new DummyFilter(FilterId.TODO);
  }

  @Override
  public ZLdapFilter cosById(String id) {
    return new DummyFilter(FilterId.TODO);
  }

  @Override
  public ZLdapFilter cosesByMailHostPool(String serverId) {
    return new DummyFilter(FilterId.TODO);
  }

  @Override
  public ZLdapFilter allDataSources() {
    return new DummyFilter(FilterId.TODO);
  }

  @Override
  public ZLdapFilter dataSourceById(String id) {
    return new DummyFilter(FilterId.TODO);
  }

  @Override
  public ZLdapFilter dataSourceByName(String name) {
    return new DummyFilter(FilterId.TODO);
  }

  @Override
  public ZLdapFilter allDistributionLists() {
    return new DummyFilter(FilterId.TODO);
  }

  @Override
  public ZLdapFilter distributionListById(String id) {
    return new DummyFilter(FilterId.TODO);
  }

  @Override
  public ZLdapFilter distributionListByName(String name) {
    return new DummyFilter(FilterId.TODO);
  }

  @Override
  public ZLdapFilter distributionListsByMemberAddrs(String[] memberAddrs) {
    return new DummyFilter(FilterId.TODO);
  }

  @Override
  public ZLdapFilter allDynamicGroups() {
    return new DummyFilter(FilterId.TODO);
  }

  @Override
  public ZLdapFilter dynamicGroupById(String id) {
    return new DummyFilter(FilterId.TODO);
  }

  @Override
  public ZLdapFilter dynamicGroupByIds(String[] strings) {
    return new DummyFilter(FilterId.TODO);
  }

  @Override
  public ZLdapFilter dynamicGroupByName(String name) {
    return new DummyFilter(FilterId.TODO);
  }

  @Override
  public ZLdapFilter dynamicGroupDynamicUnitByMailAddr(String mailAddr) {
    return new DummyFilter(FilterId.TODO);
  }

  @Override
  public ZLdapFilter dynamicGroupsStaticUnitByMemberAddr(String memberAddr) {
    return new DummyFilter(FilterId.TODO);
  }

  @Override
  public ZLdapFilter allGroups() {
    return new DummyFilter(FilterId.TODO);
  }

  @Override
  public ZLdapFilter groupById(String id) {
    return new DummyFilter(FilterId.TODO);
  }

  @Override
  public ZLdapFilter groupByName(String name) {
    return new DummyFilter(FilterId.TODO);
  }

  @Override
  public ZLdapFilter allHabGroups() {
    return new DummyFilter(FilterId.TODO);
  }

  @Override
  public ZLdapFilter allDomains() {
    return new DummyFilter(FilterId.TODO);
  }

  @Override
  public ZLdapFilter domainAliases(String id) {
    return new DummyFilter(FilterId.TODO);
  }

  @Override
  public ZLdapFilter domainById(String id) {
    return new DummyFilter(FilterId.TODO);
  }

  @Override
  public ZLdapFilter domainsByIds(Collection<String> ids) {
    return new DummyFilter(FilterId.TODO);
  }

  @Override
  public ZLdapFilter domainByName(String name) {
    return new DummyFilter(FilterId.TODO);
  }

  @Override
  public ZLdapFilter domainByKrb5Realm(String krb5Realm) {
    return new DummyFilter(FilterId.TODO);
  }

  @Override
  public ZLdapFilter domainByVirtualHostame(String virtualHostname) {
    return new DummyFilter(FilterId.TODO);
  }

  @Override
  public ZLdapFilter domainByForeignName(String foreignName) {
    return new DummyFilter(FilterId.TODO);
  }

  @Override
  public ZLdapFilter domainLabel() {
    return new DummyFilter(FilterId.TODO);
  }

  @Override
  public ZLdapFilter domainLockedForEagerAutoProvision() {
    return new DummyFilter(FilterId.TODO);
  }

  @Override
  public ZLdapFilter globalConfig() {
    return new DummyFilter(FilterId.TODO);
  }

  @Override
  public ZLdapFilter allIdentities() {
    return new DummyFilter(FilterId.TODO);
  }

  @Override
  public ZLdapFilter identityByName(String name) {
    return new DummyFilter(FilterId.TODO);
  }

  @Override
  public ZLdapFilter allMimeEntries() {
    return new DummyFilter(FilterId.TODO);
  }

  @Override
  public ZLdapFilter mimeEntryByMimeType(String mimeType) {
    return new DummyFilter(FilterId.TODO);
  }

  @Override
  public ZLdapFilter allServers() {
    return new DummyFilter(FilterId.TODO);
  }

  @Override
  public ZLdapFilter serverById(String id) {
    return new DummyFilter(FilterId.TODO);
  }

  @Override
  public ZLdapFilter serverByService(String service) {
    return new DummyFilter(FilterId.TODO);
  }

  @Override
  public ZLdapFilter shareLocatorById(String id) {
    return new DummyFilter(FilterId.TODO);
  }

  @Override
  public ZLdapFilter allSignatures() {
    return new DummyFilter(FilterId.TODO);
  }

  @Override
  public ZLdapFilter signatureById(String id) {
    return new DummyFilter(FilterId.TODO);
  }

  @Override
  public ZLdapFilter allXMPPComponents() {
    return new DummyFilter(FilterId.TODO);
  }

  @Override
  public ZLdapFilter imComponentById(String id) {
    return new DummyFilter(FilterId.TODO);
  }

  @Override
  public ZLdapFilter xmppComponentById(String id) {
    return new DummyFilter(FilterId.TODO);
  }

  @Override
  public ZLdapFilter memberOf(String dnOfGroup) {
    return new DummyFilter(FilterId.TODO);
  }

  @Override
  public ZLdapFilter velodromeAllAccountsByDomain(String domainName) {
    return new DummyFilter(FilterId.TODO);
  }

  @Override
  public ZLdapFilter velodromeAllAccountsOnlyByDomain(String domainName) {
    return new DummyFilter(FilterId.TODO);
  }

  @Override
  public ZLdapFilter velodromeAllCalendarResourcesByDomain(String domainName) {
    return new DummyFilter(FilterId.TODO);
  }

  @Override
  public ZLdapFilter velodromeAllAccountsByDomainAndServer(
      String domainName, String serverServiceHostname) {
    return new DummyFilter(FilterId.TODO);
  }

  @Override
  public ZLdapFilter velodromeAllAccountsOnlyByDomainAndServer(
      String domainName, String serverServiceHostname) {
    return new DummyFilter(FilterId.TODO);
  }

  @Override
  public ZLdapFilter velodromeAllCalendarResourcesByDomainAndServer(
      String domainName, String serverServiceHostname) {
    return new DummyFilter(FilterId.TODO);
  }

  @Override
  public ZLdapFilter velodromeAllDistributionListsByDomain(String domainName) {
    return new DummyFilter(FilterId.TODO);
  }

  @Override
  public ZLdapFilter velodromeAllGroupsByDomain(String domainName) {
    return new DummyFilter(FilterId.TODO);
  }

  @Override
  public ZLdapFilter allAddressLists() {
    return new DummyFilter(FilterId.TODO);
  }

  @Override
  public ZLdapFilter addressListById(String id) {
    return new DummyFilter(FilterId.TODO);
  }

  @Override
  public ZLdapFilter addressListByName(String name) {
    return new DummyFilter(FilterId.TODO);
  }

  @Override
  public ZLdapFilter dnSubtreeMatch(String... dns) {
    return new DummyFilter(FilterId.TODO);
  }

  @Override
  public ZLdapFilter habOrgUnitByName(String name) {
    return new DummyFilter(FilterId.TODO);
  }
}

class DummyFilter extends ZLdapFilter {

  DummyFilter(ZLdapFilterFactory.FilterId filterId) {
    super(filterId);
  }

  @Override
  public String toFilterString() {
    return "filter";
  }
}
