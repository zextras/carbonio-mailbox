// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package qa.unittest.prov.ldap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.zimbra.common.account.Key;
import com.zimbra.common.account.ProvisioningConstants;
import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.AccessManager;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.AuthToken;
import com.zimbra.cs.account.CalendarResource;
import com.zimbra.cs.account.Config;
import com.zimbra.cs.account.Cos;
import com.zimbra.cs.account.DistributionList;
import com.zimbra.cs.account.Domain;
import com.zimbra.cs.account.DynamicGroup;
import com.zimbra.cs.account.Entry;
import com.zimbra.cs.account.GlobalGrant;
import com.zimbra.cs.account.Group;
import com.zimbra.cs.account.GuestAccount;
import com.zimbra.cs.account.NamedEntry;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.Server;
import com.zimbra.cs.account.UCService;
import com.zimbra.cs.account.Zimlet;
import com.zimbra.cs.account.accesscontrol.AttrRight;
import com.zimbra.cs.account.accesscontrol.CheckRight;
import com.zimbra.cs.account.accesscontrol.ComboRight;
import com.zimbra.cs.account.accesscontrol.GranteeType;
import com.zimbra.cs.account.accesscontrol.PresetRight;
import com.zimbra.cs.account.accesscontrol.Right;
import com.zimbra.cs.account.accesscontrol.Right.RightType;
import com.zimbra.cs.account.accesscontrol.RightCommand;
import com.zimbra.cs.account.accesscontrol.RightCommand.AllEffectiveRights;
import com.zimbra.cs.account.accesscontrol.RightCommand.DomainedRightsByTargetType;
import com.zimbra.cs.account.accesscontrol.RightCommand.EffectiveRights;
import com.zimbra.cs.account.accesscontrol.RightCommand.Grants;
import com.zimbra.cs.account.accesscontrol.RightCommand.RightAggregation;
import com.zimbra.cs.account.accesscontrol.RightCommand.RightsByTargetType;
import com.zimbra.cs.account.accesscontrol.Rights.Admin;
import com.zimbra.cs.account.accesscontrol.TargetType;
import com.zimbra.cs.account.accesscontrol.UserRight;
import com.zimbra.cs.account.auth.AuthMechanism.AuthMech;
import com.zimbra.cs.account.ldap.LdapProv;
import com.zimbra.cs.account.ldap.entry.LdapDomain;
import com.zimbra.soap.admin.type.GranteeSelector.GranteeBy;
import com.zimbra.soap.type.TargetBy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import qa.unittest.prov.ProvTestUtil;

public class TestACLAll extends LdapTest {

  private static final String ATTR_ALLOWED_IN_THE_RIGHT = Provisioning.A_description;
  private static final String ATTR_NOTALLOWED_IN_THE_RIGHT = Provisioning.A_objectClass;

  private static final String PASSWORD = "test123";
  private static int sequence = 1;

  private static List<Right> rights = Lists.newArrayList();

  private static class TestGranteeType {
    private static final TestGranteeType GRANTEE_DYNAMIC_GROUP = new TestGranteeType("dgp");
    private static final List<TestGranteeType> TEST_GRANTEE_TYPES = Lists.newArrayList();

    static {
      TEST_GRANTEE_TYPES.add(GRANTEE_DYNAMIC_GROUP);
      for (GranteeType granteeType : GranteeType.values()) {
        TEST_GRANTEE_TYPES.add(new TestGranteeType(granteeType));
      }
    }

    private final Object granteeType;

    static TestGranteeType get(GranteeType gt) {
      for (TestGranteeType testGranteeType : TEST_GRANTEE_TYPES) {
        Object granteeType = testGranteeType.getGranteeType();
        if (gt == granteeType) {
          return testGranteeType;
        }
      }
      fail();
      return null;
    }

    private TestGranteeType(Object granteeType) {
      this.granteeType = granteeType;
    }

    private Object getGranteeType() {
      return granteeType;
    }

    private String getCode() {
      if (granteeType instanceof GranteeType) {
        return ((GranteeType) granteeType).getCode();
      } else {
        return granteeType.toString();
      }
    }
  }
  ;

  static final AccessManager accessMgr = AccessManager.getInstance();
  private static LdapProvTestUtil provUtil;
  private static LdapProv prov;
  private static Domain baseDomain;
  private static String BASE_DOMAIN_NAME;
  private static Account globalAdmin;

  @BeforeClass
  public static void init() throws Exception {
    provUtil = new LdapProvTestUtil();
    prov = provUtil.getProv();
    baseDomain = provUtil.createDomain(baseDomainName());
    BASE_DOMAIN_NAME = baseDomain.getName();
    globalAdmin = provUtil.createGlobalAdmin("globaladmin", baseDomain);

    ACLTestUtil.initTestRights();
    initRights();

    // remove all grants on global grant so it will not interfere with later tests
    revokeAllGrantsOnGlobalGrantAndGlobalConfig();
  }

  @AfterClass
  public static void cleanup() throws Exception {
    // remove all grants on global grant so it will not interfere with later tests
    revokeAllGrantsOnGlobalGrantAndGlobalConfig();
    Cleanup.deleteAll(baseDomainName());
  }

  private static void initRights() throws Exception {

    rights.add(ACLTestUtil.USER_LOGIN_AS);
    rights.add(ACLTestUtil.USER_RIGHT);
    rights.add(ACLTestUtil.USER_RIGHT_DISTRIBUTION_LIST);
    rights.add(ACLTestUtil.USER_RIGHT_DOMAIN);
    rights.add(ACLTestUtil.USER_RIGHT_RESTRICTED_GRANT_TARGET_TYPE);

    rights.add(ACLTestUtil.ADMIN_PRESET_LOGIN_AS);
    rights.add(ACLTestUtil.ADMIN_PRESET_ACCOUNT);
    rights.add(ACLTestUtil.ADMIN_PRESET_CALENDAR_RESOURCE);
    rights.add(ACLTestUtil.ADMIN_PRESET_CONFIG);
    rights.add(ACLTestUtil.ADMIN_PRESET_COS);
    rights.add(ACLTestUtil.ADMIN_PRESET_DISTRIBUTION_LIST);
    rights.add(ACLTestUtil.ADMIN_PRESET_DYNAMIC_GROUP);
    rights.add(ACLTestUtil.ADMIN_PRESET_DOMAIN);
    rights.add(ACLTestUtil.ADMIN_PRESET_GLOBALGRANT);
    rights.add(ACLTestUtil.ADMIN_PRESET_SERVER);
    rights.add(ACLTestUtil.ADMIN_PRESET_UC_SERVICE);
    rights.add(ACLTestUtil.ADMIN_PRESET_XMPP_COMPONENT);
    rights.add(ACLTestUtil.ADMIN_PRESET_ZIMLET);

    rights.add(ACLTestUtil.ADMIN_ATTR_GETALL_ACCOUNT);
    rights.add(ACLTestUtil.ADMIN_ATTR_SETALL_ACCOUNT);
    rights.add(ACLTestUtil.ADMIN_ATTR_GETSOME_ACCOUNT);
    rights.add(ACLTestUtil.ADMIN_ATTR_SETSOME_ACCOUNT);
    rights.add(ACLTestUtil.ADMIN_ATTR_GETALL_CALENDAR_RESOURCE);
    rights.add(ACLTestUtil.ADMIN_ATTR_SETALL_CALENDAR_RESOURCE);
    rights.add(ACLTestUtil.ADMIN_ATTR_GETSOME_CALENDAR_RESOURCE);
    rights.add(ACLTestUtil.ADMIN_ATTR_SETSOME_CALENDAR_RESOURCE);
    rights.add(ACLTestUtil.ADMIN_ATTR_GETALL_CONFIG);
    rights.add(ACLTestUtil.ADMIN_ATTR_SETALL_CONFIG);
    rights.add(ACLTestUtil.ADMIN_ATTR_GETSOME_CONFIG);
    rights.add(ACLTestUtil.ADMIN_ATTR_SETSOME_CONFIG);
    rights.add(ACLTestUtil.ADMIN_ATTR_GETALL_COS);
    rights.add(ACLTestUtil.ADMIN_ATTR_SETALL_COS);
    rights.add(ACLTestUtil.ADMIN_ATTR_GETSOME_COS);
    rights.add(ACLTestUtil.ADMIN_ATTR_SETSOME_COS);
    rights.add(ACLTestUtil.ADMIN_ATTR_GETALL_DISTRIBUTION_LIST);
    rights.add(ACLTestUtil.ADMIN_ATTR_SETALL_DISTRIBUTION_LIST);
    rights.add(ACLTestUtil.ADMIN_ATTR_GETSOME_DISTRIBUTION_LIST);
    rights.add(ACLTestUtil.ADMIN_ATTR_SETSOME_DISTRIBUTION_LIST);
    rights.add(ACLTestUtil.ADMIN_ATTR_GETALL_DYNAMIC_GROUP);
    rights.add(ACLTestUtil.ADMIN_ATTR_SETALL_DYNAMIC_GROUP);
    rights.add(ACLTestUtil.ADMIN_ATTR_GETSOME_DYNAMIC_GROUP);
    rights.add(ACLTestUtil.ADMIN_ATTR_SETSOME_DYNAMIC_GROUP);
    rights.add(ACLTestUtil.ADMIN_ATTR_GETALL_DOMAIN);
    rights.add(ACLTestUtil.ADMIN_ATTR_SETALL_DOMAIN);
    rights.add(ACLTestUtil.ADMIN_ATTR_GETSOME_DOMAIN);
    rights.add(ACLTestUtil.ADMIN_ATTR_SETSOME_DOMAIN);
    rights.add(ACLTestUtil.ADMIN_ATTR_GETALL_SERVER);
    rights.add(ACLTestUtil.ADMIN_ATTR_SETALL_SERVER);
    rights.add(ACLTestUtil.ADMIN_ATTR_GETSOME_SERVER);
    rights.add(ACLTestUtil.ADMIN_ATTR_SETSOME_SERVER);
    rights.add(ACLTestUtil.ADMIN_ATTR_GETALL_UC_SERVICE);
    rights.add(ACLTestUtil.ADMIN_ATTR_SETALL_UC_SERVICE);
    rights.add(ACLTestUtil.ADMIN_ATTR_GETSOME_UC_SERVICE);
    rights.add(ACLTestUtil.ADMIN_ATTR_SETSOME_UC_SERVICE);
    rights.add(ACLTestUtil.ADMIN_ATTR_GETALL_ZIMLET);
    rights.add(ACLTestUtil.ADMIN_ATTR_SETALL_ZIMLET);
    rights.add(ACLTestUtil.ADMIN_ATTR_GETSOME_ZIMLET);
    rights.add(ACLTestUtil.ADMIN_ATTR_SETSOME_ZIMLET);

    rights.add(ACLTestUtil.ADMIN_COMBO_ACCOUNT);
    rights.add(ACLTestUtil.ADMIN_COMBO_CALENDAR_RESOURCE);
    rights.add(ACLTestUtil.ADMIN_COMBO_CONFIG);
    rights.add(ACLTestUtil.ADMIN_COMBO_COS);
    rights.add(ACLTestUtil.ADMIN_COMBO_DISTRIBUTION_LIST);
    rights.add(ACLTestUtil.ADMIN_COMBO_DYNAMIC_GROUP);
    rights.add(ACLTestUtil.ADMIN_COMBO_DOMAIN);
    rights.add(ACLTestUtil.ADMIN_COMBO_GLOBALGRANT);
    rights.add(ACLTestUtil.ADMIN_COMBO_SERVER);
    rights.add(ACLTestUtil.ADMIN_COMBO_UC_SERVICE);
    rights.add(ACLTestUtil.ADMIN_COMBO_XMPP_COMPONENT);
    rights.add(ACLTestUtil.ADMIN_COMBO_ZIMLET);
    // sRights.add(ACLTestUtil.ADMIN_COMBO_ALL);
  }

  private Config getConfig() throws Exception {
    return prov.getConfig();
  }

  private GlobalGrant getGlobalGrant() throws Exception {
    return prov.getGlobalGrant();
  }

  private boolean asAdmin(Account acct) {
    // for now return true if the account is an admin account
    // TODO: test cases when the account is an admin account but is not using the admin privelege
    return (acct.isIsAdminAccount() || acct.isIsDelegatedAdminAccount());
  }

  private static synchronized String nextSeq() {
    return "" + sequence++;
  }

  private String domainName() {
    return nextSeq() + "." + BASE_DOMAIN_NAME;
  }

  private String accountName() {
    return "acct-" + nextSeq();
  }

  private String calendarResourceName() {
    return "cr-" + nextSeq();
  }

  private String distributionListName() {
    return "dl-" + nextSeq();
  }

  private String dynamicGroupName() {
    return "group-" + nextSeq();
  }

  private String cosName() {
    return "cos-" + nextSeq();
  }

  private String serverName() {
    return "server-" + nextSeq();
  }

  private String ucServiceName() {
    return "ucservice-" + nextSeq();
  }

  private String XMPPComponentName() {
    return "xmpp-" + nextSeq();
  }

  private String zimletName() {
    return "zimlet-" + nextSeq();
  }

  private Domain createDomain() throws Exception {
    return provUtil.createDomain(domainName());
  }

  private Account anonAccount() {
    return GuestAccount.ANONYMOUS_ACCT;
  }

  private Account createUserAccount(String localpart, Domain domain) throws Exception {
    if (domain == null) {
      domain = createDomain();
    }
    return provUtil.createAccount(localpart, domain);
  }

  private Account createUserAccount(Domain domain) throws Exception {
    String localpart = accountName();
    return createUserAccount(localpart, domain);
  }

  private Account createDelegatedAdminAccount(String localpart, Domain domain) throws Exception {
    if (domain == null) {
      domain = createDomain();
    }
    return provUtil.createDelegatedAdmin(localpart, domain);
  }

  private Account createDelegatedAdminAccount(Domain domain) throws Exception {
    String localpart = accountName();
    return createDelegatedAdminAccount(localpart, domain);
  }

  private Account createGuestAccount(String email, String password) {
    return new GuestAccount(email, password);
  }

  private Account createKeyAccount(String name, String accesKey) {
    AuthToken authToken = new ACLTestUtil.KeyAuthToken(name, accesKey);
    return new GuestAccount(authToken);
  }

  private CalendarResource createCalendarResource(String localpart, Domain domain)
      throws Exception {
    if (domain == null) {
      domain = createDomain();
    }

    Map<String, Object> attrs = new HashMap<String, Object>();
    attrs.put(Provisioning.A_displayName, localpart);
    attrs.put(Provisioning.A_zimbraCalResType, "Equipment");

    return provUtil.createCalendarResource(localpart, domain, attrs);
  }

  private CalendarResource createCalendarResource(Domain domain) throws Exception {
    String localpart = calendarResourceName();
    return createCalendarResource(localpart, domain);
  }

  private DistributionList createDistributionList(
      String localpart, Domain domain, Map<String, Object> attrs) throws Exception {
    if (domain == null) {
      domain = createDomain();
    }
    return provUtil.createDistributionList(localpart, domain, attrs);
  }

  private DistributionList createUserDistributionList(String localpart, Domain domain)
      throws Exception {
    return createDistributionList(localpart, domain, new HashMap<String, Object>());
  }

  private DistributionList createUserDistributionList(Domain domain) throws Exception {
    String localpart = distributionListName();
    return createUserDistributionList(localpart, domain);
  }

  private DistributionList createAdminDistributionList(String localpart, Domain domain)
      throws Exception {
    Map<String, Object> attrs = new HashMap<String, Object>();
    attrs.put(Provisioning.A_zimbraIsAdminGroup, ProvisioningConstants.TRUE);
    return createDistributionList(localpart, domain, attrs);
  }

  private DistributionList createAdminDistributionList(Domain domain) throws Exception {
    String localpart = distributionListName();
    return createAdminDistributionList(localpart, domain);
  }

  private DynamicGroup createDynamicGroup(
      String localpart, Domain domain, Map<String, Object> attrs) throws Exception {
    if (domain == null) {
      domain = createDomain();
    }
    return provUtil.createDynamicGroup(localpart, domain, attrs);
  }

  private DynamicGroup createUserDynamicGroup(String localpart, Domain domain) throws Exception {
    return createDynamicGroup(localpart, domain, new HashMap<String, Object>());
  }

  private DynamicGroup createUserDynamicGroup(Domain domain) throws Exception {
    String localpart = dynamicGroupName();
    return createUserDynamicGroup(localpart, domain);
  }

  private DynamicGroup createAdminDynamicGroup(String localpart, Domain domain) throws Exception {
    Map<String, Object> attrs = new HashMap<String, Object>();
    attrs.put(Provisioning.A_zimbraIsAdminGroup, ProvisioningConstants.TRUE);
    return createDynamicGroup(localpart, domain, attrs);
  }

  private DynamicGroup createAdminDynamicGroup(Domain domain) throws Exception {
    String localpart = dynamicGroupName();
    return createAdminDynamicGroup(localpart, domain);
  }

  private Cos createCos() throws Exception {
    return provUtil.createCos(cosName());
  }

  private Server createServer() throws Exception {
    return provUtil.createServer(serverName());
  }

  private UCService createUCService() throws Exception {
    return provUtil.createUCService(ucServiceName());
  }

  private Zimlet createZimlet() throws Exception {
    Map<String, Object> attrs = new HashMap<String, Object>();
    attrs.put(Provisioning.A_zimbraZimletVersion, "1.0");
    return provUtil.createZimlet(zimletName(), attrs);
  }

  private boolean expectedIsUserRightGrantableOnTargetType(
      UserRight userRight, TargetType targetType) throws Exception {
    TargetType rightTarget = userRight.getTargetType();
    TargetType rightGrantTarget = userRight.getGrantTargetType();

    switch (rightTarget) {
      case account:
        if (rightGrantTarget == null) {
          return targetType == TargetType.account
              || targetType == TargetType.calresource
              || targetType == TargetType.dl
              || targetType == TargetType.group
              || targetType == TargetType.domain
              || targetType == TargetType.global;
        } else if (rightGrantTarget == TargetType.account) {
          return targetType == TargetType.account || targetType == TargetType.calresource;
        } else if (rightGrantTarget == TargetType.dl) {
          return targetType == TargetType.dl || targetType == TargetType.group;
        } else if (rightGrantTarget == TargetType.domain) {
          return targetType == TargetType.domain;
        } else if (rightGrantTarget == TargetType.global) {
          return targetType == TargetType.global;
        } else {
          return false;
        }
      case calresource:
        fail();
      case cos:
        fail();
      case dl:
        if (rightGrantTarget == null) {
          return targetType == TargetType.dl
              || targetType == TargetType.group
              || targetType == TargetType.domain
              || targetType == TargetType.global;
        } else if (rightGrantTarget == TargetType.dl) {
          return targetType == TargetType.dl || targetType == TargetType.group;
        } else if (rightGrantTarget == TargetType.domain) {
          return targetType == TargetType.domain;
        } else if (rightGrantTarget == TargetType.global) {
          return targetType == TargetType.global;
        } else {
          return false;
        }
      case domain:
        if (rightGrantTarget == null) {
          return targetType == TargetType.domain || targetType == TargetType.global;
        } else if (rightGrantTarget == TargetType.global) {
          return targetType == TargetType.global;
        } else {
          return false;
        }
      case group:
      case server:
      case ucservice:
      case xmppcomponent:
      case zimlet:
      case config:
      case global:
      default:
        fail();
    }
    return false;
  }

  private void collectGrantableTargetTypes(TargetType rightTarget, Set<TargetType> validTypes) {
    switch (rightTarget) {
      case account:
        validTypes.add(TargetType.account);
        validTypes.add(TargetType.dl);
        validTypes.add(TargetType.group);
        validTypes.add(TargetType.domain);
        validTypes.add(TargetType.global);
        break;
      case calresource:
        validTypes.add(TargetType.calresource);
        validTypes.add(TargetType.dl);
        validTypes.add(TargetType.group);
        validTypes.add(TargetType.domain);
        validTypes.add(TargetType.global);
        break;
      case cos:
        validTypes.add(TargetType.cos);
        validTypes.add(TargetType.global);
        break;
      case dl:
        validTypes.add(TargetType.dl);
        validTypes.add(TargetType.domain);
        validTypes.add(TargetType.global);
        break;
      case group:
        validTypes.add(TargetType.group);
        validTypes.add(TargetType.domain);
        validTypes.add(TargetType.global);
        break;
      case domain:
        validTypes.add(TargetType.domain);
        validTypes.add(TargetType.global);
        break;
      case server:
        validTypes.add(TargetType.server);
        validTypes.add(TargetType.global);
        break;
      case ucservice:
        validTypes.add(TargetType.ucservice);
        validTypes.add(TargetType.global);
        break;
      case xmppcomponent:
        validTypes.add(TargetType.xmppcomponent);
        validTypes.add(TargetType.global);
        break;
      case zimlet:
        validTypes.add(TargetType.zimlet);
        validTypes.add(TargetType.global);
        break;
      case config:
        validTypes.add(TargetType.config);
        validTypes.add(TargetType.global);
        break;
      case global:
        validTypes.add(TargetType.global);
        break;
      default:
        fail();
    }
  }

  private boolean expectedIsPresetRightGrantableOnTargetType(
      PresetRight presetRight, TargetType targetType) throws Exception {
    Set<TargetType> validTypes = Sets.newHashSet();
    TargetType rightTarget = presetRight.getTargetType();
    collectGrantableTargetTypes(rightTarget, validTypes);
    return validTypes.contains(targetType);
  }

  private boolean expectedIsAttrRightGrantableOnTargetType(
      AttrRight attrRight, TargetType targetType) throws Exception {
    Set<TargetType> rightTargets = attrRight.getTargetTypes();

    // return true if *any* of the applicable target types for the right
    // can inherit from targetType
    Set<TargetType> validTypes = Sets.newHashSet();
    for (TargetType rightTarget : rightTargets) {
      collectGrantableTargetTypes(rightTarget, validTypes);
      return validTypes.contains(targetType);
    }
    return false;
  }

  private boolean expectedIsComboRightGrantableOnTargetType(
      ComboRight comboRight, TargetType targetType) throws Exception {
    Set<Right> allRights = comboRight.getAllRights();

    // each and every right in the combo right must be grantable on the target type
    for (Right right : allRights) {
      if (!expectedIsRightGrantableOnTargetType(right, targetType)) {
        return false;
      }
    }
    return true;
  }

  private boolean expectedIsRightGrantableOnTargetType(Right right, TargetType targetType)
      throws Exception {
    if (targetType.isGroup() && !CheckRight.allowGroupTarget(right)) return false;

    if (right.isUserRight()) {
      return expectedIsUserRightGrantableOnTargetType((UserRight) right, targetType);
    } else if (right.isPresetRight()) {
      return expectedIsPresetRightGrantableOnTargetType((PresetRight) right, targetType);
    } else if (right.isAttrRight()) {
      return expectedIsAttrRightGrantableOnTargetType((AttrRight) right, targetType);
    } else if (right.isComboRight()) {
      return expectedIsComboRightGrantableOnTargetType((ComboRight) right, targetType);
    }
    fail();
    return false; // just to keep the compiler happy
  }

  private void skipTest(
      String note, TargetType grantedOnTargetType, TestGranteeType testGranteeType, Right right)
      throws Exception {

    System.out.println(
        "skipping test ("
            + note
            + "): "
            + "grant target="
            + grantedOnTargetType.getCode()
            + ", grantee type="
            + testGranteeType.getCode()
            + ", right="
            + right.getName());
  }

  private void execTest(
      String note, TargetType grantedOnTargetType, TestGranteeType testGranteeType, Right right)
      throws Exception {

    System.out.println(
        "testing ("
            + note
            + "): "
            + "grant target="
            + grantedOnTargetType.getCode()
            + ", grantee type="
            + testGranteeType.getCode()
            + ", right="
            + right.getName());

    //
    // 1. some basic preparation
    //    create a domain
    //
    Domain domain = createDomain();
    boolean isUserRight = right.isUserRight();

    //
    // 2. setup grantee
    //
    List<Account> allowedAccts = new ArrayList<Account>();
    List<Account> deniedAccts = new ArrayList<Account>();
    NamedEntry grantee = null;
    String granteeName = null;
    String secret = null;

    Object gt = testGranteeType.getGranteeType();

    GranteeType granteeType = null;
    if (gt instanceof GranteeType) {
      granteeType = (GranteeType) gt;

      switch (granteeType) {
        case GT_USER:
          if (isUserRight) {
            grantee = createUserAccount(domain);
            allowedAccts.add((Account) grantee);
            deniedAccts.add(createUserAccount(domain));
          } else {
            grantee = createDelegatedAdminAccount(domain);
            allowedAccts.add((Account) grantee);
            deniedAccts.add(createDelegatedAdminAccount(domain));
          }
          granteeName = grantee.getName();

          break;
        case GT_GROUP:
          if (isUserRight) {
            grantee = createUserDistributionList(domain);
            Account allowedAcct = createUserAccount(domain);
            allowedAccts.add(allowedAcct);
            prov.addMembers((DistributionList) grantee, new String[] {allowedAcct.getName()});

            // external members are also honored if the right is a user right
            Account guestAcct = createGuestAccount("guest@guest.com", "test123");
            allowedAccts.add(guestAcct);
            prov.addMembers((DistributionList) grantee, new String[] {guestAcct.getName()});

            deniedAccts.add(createUserAccount(domain));
          } else {
            grantee = createAdminDistributionList(domain);
            Account allowedAcct = createDelegatedAdminAccount(domain);
            allowedAccts.add(allowedAcct);
            prov.addMembers((DistributionList) grantee, new String[] {allowedAcct.getName()});
            deniedAccts.add(createDelegatedAdminAccount(domain));
          }
          granteeName = grantee.getName();
          break;
        case GT_EXT_GROUP:
          // create a domain and use it for the external group
          Domain extDomain = createDomain();
          String extDomainDN = ((LdapDomain) extDomain).getDN();
          String acctLocalpart = "acct-ext";

          //
          // Configure the domain for external AD auth
          //
          Map<String, Object> domainAttrs = Maps.newHashMap();

          if (isUserRight) {
            domain.setAuthMech(AuthMech.ad.name(), domainAttrs);
          } else {
            domain.setAuthMechAdmin(AuthMech.ad.name(), domainAttrs);
          }

          /*  ==== mock test ====
          // setup auth
          domain.addAuthLdapURL("ldap://localhost:389", domainAttrs);
          domain.setAuthLdapBindDn("uid=%u,ou=people," + extDomainDN, domainAttrs);
          // setup external group search parameters
          domain.setAuthLdapSearchBindDn(LC.zimbra_ldap_userdn.value(), domainAttrs);
          domain.setAuthLdapSearchBindPassword(LC.zimbra_ldap_password.value(), domainAttrs);
          domain.setExternalGroupLdapSearchBase(extDomainDN, domainAttrs);
          domain.setExternalGroupLdapSearchFilter("(&(objectClass=zimbraGroup)(cn=%u))", domainAttrs);
          domain.setExternalGroupHandlerClass("qa.unittest.UnittestGroupHandler", domainAttrs);
          mProv.modifyAttrs(domain, domainAttrs);

          // create a group in the external directory and add a member
          Group extGroup = createUserDynamicGroup(extDomain);  // doesn't matter if the group is user or admin
          String extGroupName = extGroup.getName();
          Account extAcct = createUserAccount(acctLocalpart, extDomain);
          mProv.addGroupMembers(extGroup, new String[]{extAcct.getName()});

          // create the admin account in Zimbra directory and map it to the external account
          Account zimbraAcct = createDelegatedAdminAccount(acctLocalpart, domain);
          allowedAccts.add(zimbraAcct);
          */

          domain.addAuthLdapURL("***", domainAttrs);
          domain.setAuthLdapSearchBindDn("***", domainAttrs);
          domain.setAuthLdapSearchBindPassword("***", domainAttrs);
          domain.setExternalGroupLdapSearchBase("OU=Engineering,DC=vmware,DC=com", domainAttrs);
          domain.setExternalGroupLdapSearchFilter("(&(objectClass=group)(mail=%n))", domainAttrs);
          domain.setExternalGroupHandlerClass(
              "com.zimbra.cs.account.grouphandler.ADGroupHandler", domainAttrs);
          prov.modifyAttrs(domain, domainAttrs);

          String extGroupName =
              "ENG_pao_users_home4@vmware.com"; // "ESPPEnrollment-USA@vmware.com";

          // create the admin account in Zimbra directory and map it to the external account
          Account zimbraAcct = createDelegatedAdminAccount(acctLocalpart, domain);
          zimbraAcct.setAuthLdapExternalDn(
              "CN=Phoebe"
                  + " Shao,OU=PAO_Users,OU=PaloAlto_California_USA,OU=NALA,OU=SITES,OU=Engineering,DC=vmware,DC=com");
          allowedAccts.add(zimbraAcct);
          // =======================

          granteeName = domain.getName() + ":" + extGroupName;
          break;
        case GT_AUTHUSER:
          if (isUserRight) {
            allowedAccts.add(createUserAccount("allowed-user-acct", domain));
            deniedAccts.add(createGuestAccount("not-my-guest@external.com", "test123"));
          } else {
            deniedAccts.add(createDelegatedAdminAccount("denied-da-acct", domain));
          }
          break;
        case GT_DOMAIN:
          grantee = createDomain();
          if (isUserRight) {
            allowedAccts.add(createUserAccount("allowed-user-acct", (Domain) grantee));
            Domain notGrantee = createDomain();
            deniedAccts.add(createUserAccount("denied-user-acct", notGrantee));
          } else {
            deniedAccts.add(createDelegatedAdminAccount("denied-da-acct", (Domain) grantee));
            // TODO: TEST R_crossDomainAdmin
          }
          granteeName = grantee.getName();
          break;
        case GT_GUEST:
          granteeName = "be-my-guest@guest.com"; // an email address
          secret = "test123"; // password
          if (isUserRight) {
            allowedAccts.add(createGuestAccount(granteeName, secret));
            deniedAccts.add(createGuestAccount("not-my-guest@external.com", "bad"));
          } else {
            deniedAccts.add(createDelegatedAdminAccount("denied-da-acct", domain));
            deniedAccts.add(createGuestAccount(granteeName, secret));
          }
          break;
        case GT_KEY:
          granteeName = "be-my-guest"; // a display name
          secret = "test123"; // access key
          if (isUserRight) {
            allowedAccts.add(createKeyAccount(granteeName, secret));
            deniedAccts.add(createKeyAccount("not-my-guest", "bad"));
          } else {
            deniedAccts.add(createDelegatedAdminAccount("denied-da-acct", domain));
            deniedAccts.add(createKeyAccount(granteeName, secret));
          }
          break;
        case GT_PUBLIC:
          if (isUserRight) {
            allowedAccts.add(anonAccount());
          } else {
            deniedAccts.add(anonAccount());
          }
          break;
        default:
          fail();
      }
    } else {
      // dynamic group
      assertEquals(TestGranteeType.GRANTEE_DYNAMIC_GROUP, testGranteeType);

      granteeType = GranteeType.GT_GROUP;

      if (isUserRight) {
        grantee = createUserDynamicGroup(domain);
        Account allowedAcct = createUserAccount(domain);
        allowedAccts.add(allowedAcct);
        prov.addGroupMembers((DynamicGroup) grantee, new String[] {allowedAcct.getName()});

        // external members are also honored if the right is a user right
        Account guestAcct = createGuestAccount("guest@guest.com", "test123");
        allowedAccts.add(guestAcct);
        prov.addGroupMembers((DynamicGroup) grantee, new String[] {guestAcct.getName()});

        deniedAccts.add(createUserAccount(domain));
      } else {
        grantee = createAdminDynamicGroup(domain);
        Account allowedAcct = createDelegatedAdminAccount(domain);
        allowedAccts.add(allowedAcct);
        prov.addGroupMembers((DynamicGroup) grantee, new String[] {allowedAcct.getName()});
        deniedAccts.add(createDelegatedAdminAccount(domain));
      }
      granteeName = grantee.getName();
    }

    //
    // 3. setup expectations for the granting action
    //
    boolean expectInvalidRequest = false;
    if (isUserRight) {
      expectInvalidRequest = !expectedIsRightGrantableOnTargetType(right, grantedOnTargetType);

    } else {
      // is admin right
      if (!granteeType.allowedForAdminRights()) {
        expectInvalidRequest = true;
      }

      if (!expectInvalidRequest) {
        if (granteeType == GranteeType.GT_DOMAIN && right != Admin.R_crossDomainAdmin) {
          expectInvalidRequest = true;
        }
      }

      if (!expectInvalidRequest) {
        expectInvalidRequest = !expectedIsRightGrantableOnTargetType(right, grantedOnTargetType);
      }
    }

    //
    // 4. setup target on which the right is to be granted
    //
    Entry grantedOnTarget = null;
    String targetName = null;

    switch (grantedOnTargetType) {
      case account:
        grantedOnTarget = createUserAccount("target-acct", domain);
        targetName = ((Account) grantedOnTarget).getName();
        break;
      case calresource:
        grantedOnTarget = createCalendarResource("target-cr", domain);
        targetName = ((CalendarResource) grantedOnTarget).getName();
        break;
      case cos:
        grantedOnTarget = createCos();
        targetName = ((Cos) grantedOnTarget).getName();
        break;
      case dl:
        grantedOnTarget = createUserDistributionList("target-distributionlist", domain);
        targetName = ((DistributionList) grantedOnTarget).getName();
        break;
      case group:
        grantedOnTarget = createUserDynamicGroup("target-dynamicgroup", domain);
        targetName = ((DynamicGroup) grantedOnTarget).getName();
        break;
      case domain:
        grantedOnTarget = domain;
        targetName = domain.getName();
        break;
      case server:
        grantedOnTarget = createServer();
        targetName = ((Server) grantedOnTarget).getName();
        break;
      case ucservice:
        grantedOnTarget = createUCService();
        targetName = ((UCService) grantedOnTarget).getName();
        break;
      case xmppcomponent:
        // skip for now
        return;
      case zimlet:
        grantedOnTarget = createZimlet();
        targetName = ((Zimlet) grantedOnTarget).getName();
        break;
      case config:
        grantedOnTarget = getConfig();
        break;
      case global:
        grantedOnTarget = getGlobalGrant();
        break;
      default:
        fail();
    }

    //
    // grant right on the target
    //
    boolean gotInvalidRequestException = false;
    try {
      // TODO: in a different test, test granting by a different authed account:
      //       global admin, delegated admin, user
      //
      Account grantingAccount = globalAdmin;

      RightCommand.grantRight(
          prov,
          grantingAccount,
          grantedOnTargetType.getCode(),
          TargetBy.name,
          targetName,
          granteeType.getCode(),
          GranteeBy.name,
          granteeName,
          secret,
          right.getName(),
          null);

    } catch (ServiceException e) {
      if (ServiceException.INVALID_REQUEST.equals(e.getCode())) {
        gotInvalidRequestException = true;
      } else {
        e.printStackTrace();
        fail();
      }
    }

    //
    // 5. verify the grant
    //
    assertEquals(expectInvalidRequest, gotInvalidRequestException);

    // reload the entry after the grant.  DistributionList and DynamicGroups are
    // not cached after creation.  The object on which the mod for ZimbraACE is
    // done will be a different newly created object because it is fetched from
    // cache in TargetType.lookupTarget.  Fetch it from the same cache.
    // This only needs to be done in this unittest.  In production code, everywhere
    // get the target object from cache(getGroupBasic) or LDAP(get(DistributionListBy));
    // in both cases the ACL should be on the entry.  We never do permission check right
    // after group creation using the target object returned from the create call.
    if (grantedOnTarget instanceof Group) {
      grantedOnTarget =
          prov.getGroupBasic(Key.DistributionListBy.id, ((Group) grantedOnTarget).getId());
    }

    //
    // setup test target and verify result
    //
    if (right.isComboRight()) {
      for (Right rt : ((ComboRight) right).getAllRights()) {
        setupTargetAndVerify(
            domain,
            grantedOnTarget,
            grantedOnTargetType,
            rt,
            true,
            allowedAccts,
            deniedAccts,
            !gotInvalidRequestException);
      }
    } else {
      setupTargetAndVerify(
          domain,
          grantedOnTarget,
          grantedOnTargetType,
          right,
          false,
          allowedAccts,
          deniedAccts,
          !gotInvalidRequestException);
    }
  }

  /*
   * returns if the grant is inherited,
   * if the grant is granted on the target entry itself, it is not considered inherited.
   */
  private boolean canGrantBeInheritedForCreate(Entry grantedOnTarget, Entry target)
      throws Exception {
    TargetType targetType = TargetType.getTargetType(target);
    TargetType grantedOnTargetType = TargetType.getTargetType(grantedOnTarget);

    Set<TargetType> inheritableTypes = Sets.newHashSet();
    switch (targetType) {
      case account:
        inheritableTypes.add(TargetType.domain);
        inheritableTypes.add(TargetType.global);
        break;
      case calresource:
        inheritableTypes.add(TargetType.domain);
        inheritableTypes.add(TargetType.global);
        break;
      case cos:
        inheritableTypes.add(TargetType.global);
        break;
      case dl:
        inheritableTypes.add(TargetType.domain);
        inheritableTypes.add(TargetType.global);
        break;
      case group:
        inheritableTypes.add(TargetType.domain);
        inheritableTypes.add(TargetType.global);
        break;
      case domain:
        inheritableTypes.add(TargetType.global);
        break;
      case server:
        inheritableTypes.add(TargetType.global);
        break;
      case ucservice:
        inheritableTypes.add(TargetType.global);
        break;
      case xmppcomponent:
        inheritableTypes.add(TargetType.global);
        break;
      case zimlet:
        inheritableTypes.add(TargetType.global);
        break;
      case config:
        inheritableTypes.add(TargetType.global);
        break;
      case global:
        break;
      default:
        fail();
    }
    return inheritableTypes.contains(grantedOnTargetType);
  }

  private void setupTargetAndVerify(
      Domain domain,
      Entry grantedOnTarget,
      TargetType grantedOnTargetType,
      Right right,
      boolean fromComboRight,
      List<Account> allowedAccts,
      List<Account> deniedAccts,
      boolean grantWasValid)
      throws Exception {
    // System.out.println("Right: " + right.getName());

    List<Entry> goodTargets = Lists.newArrayList();
    List<Entry> badTargets = Lists.newArrayList();

    if (right.isPresetRight()) { // including user right
      TargetType targetTypeOfRight = right.getTargetType();
      setupTarget(
          goodTargets,
          badTargets,
          domain,
          grantedOnTarget,
          grantedOnTargetType,
          targetTypeOfRight,
          right);
    } else if (right.isAttrRight()) {
      for (TargetType targetTypeOfRight : ((AttrRight) right).getTargetTypes()) {
        setupTarget(
            goodTargets,
            badTargets,
            domain,
            grantedOnTarget,
            grantedOnTargetType,
            targetTypeOfRight,
            right);
      }
    } else {
      fail();
    }

    //
    // 7. check permission
    //
    for (Entry goodTarget : goodTargets) {
      boolean canGrantBeInheritedForCreate =
          canGrantBeInheritedForCreate(grantedOnTarget, goodTarget);
      verify(
          goodTarget,
          canGrantBeInheritedForCreate,
          allowedAccts,
          deniedAccts,
          right,
          fromComboRight,
          grantWasValid);
    }
    for (Entry badTarget : badTargets) {
      boolean canGrantBeInheritedForCreate =
          canGrantBeInheritedForCreate(grantedOnTarget, badTarget);
      verify(
          badTarget,
          canGrantBeInheritedForCreate,
          allowedAccts,
          deniedAccts,
          right,
          fromComboRight,
          false);
    }
  }

  private void setupTarget(
      List<Entry> goodTargets,
      List<Entry> badTargets,
      Domain domain,
      Entry grantedOnTarget,
      TargetType grantedOnTargetType,
      TargetType targetTypeOfRight,
      Right right)
      throws Exception {

    Entry good = null;
    Entry bad = null;

    switch (targetTypeOfRight) {
      case account:
        if (grantedOnTargetType == TargetType.account) {
          goodTargets.add(grantedOnTarget);
          badTargets.add(createUserAccount(domain));

        } else if (grantedOnTargetType == TargetType.calresource) {
          if (right.isUserRight()) {
            goodTargets.add(grantedOnTarget);
            badTargets.add(createCalendarResource(domain));
          } else {
            badTargets.add(grantedOnTarget);
          }

        } else if (grantedOnTargetType == TargetType.dl) {
          if (CheckRight.allowGroupTarget(right)) {
            good = createUserAccount(domain);
            goodTargets.add(good);

            // create a subgroup of the group on which the right is granted (testing multi levels of
            // dl)
            DistributionList subGroup = createUserDistributionList(domain);
            prov.addMembers((DistributionList) grantedOnTarget, new String[] {subGroup.getName()});
            prov.addMembers(subGroup, new String[] {((Account) good).getName()});
          } else {
            bad = createUserAccount(domain);
            prov.addMembers(
                (DistributionList) grantedOnTarget, new String[] {((Account) bad).getName()});
            badTargets.add(bad);
          }

        } else if (grantedOnTargetType == TargetType.group) {
          if (CheckRight.allowGroupTarget(right)) {
            good = createUserAccount(domain);
            prov.addGroupMembers(
                (DynamicGroup) grantedOnTarget, new String[] {((Account) good).getName()});
            goodTargets.add(good);
          } else {
            bad = createUserAccount(domain);
            prov.addGroupMembers(
                (DynamicGroup) grantedOnTarget, new String[] {((Account) bad).getName()});
            badTargets.add(bad);
          }

        } else if (grantedOnTargetType == TargetType.domain) {
          goodTargets.add(createUserAccount(domain));

          Domain anyDomain = createDomain();
          badTargets.add(createUserAccount(anyDomain));

        } else if (grantedOnTargetType == TargetType.global) {
          Domain anyDomain = createDomain();
          goodTargets.add(createUserAccount(anyDomain));

        } else {
          badTargets.add(grantedOnTarget);
        }

        break;
      case calresource:
        if (grantedOnTargetType == TargetType.calresource) {
          goodTargets.add(grantedOnTarget);
          badTargets.add(createCalendarResource(domain));

        } else if (grantedOnTargetType == TargetType.dl) {
          if (CheckRight.allowGroupTarget(right)) {
            good = createCalendarResource(domain);
            prov.addMembers(
                (DistributionList) grantedOnTarget, new String[] {((Account) good).getName()});
            goodTargets.add(good);
          } else {
            bad = createCalendarResource(domain);
            prov.addMembers(
                (DistributionList) grantedOnTarget, new String[] {((Account) bad).getName()});
            badTargets.add(bad);
          }

        } else if (grantedOnTargetType == TargetType.group) {
          if (CheckRight.allowGroupTarget(right)) {
            good = createCalendarResource(domain);
            prov.addGroupMembers(
                (DynamicGroup) grantedOnTarget, new String[] {((Account) good).getName()});
            goodTargets.add(good);
          } else {
            bad = createCalendarResource(domain);
            prov.addGroupMembers(
                (DynamicGroup) grantedOnTarget, new String[] {((Account) bad).getName()});
            badTargets.add(bad);
          }

        } else if (grantedOnTargetType == TargetType.domain) {
          good = createCalendarResource(domain);
          goodTargets.add(good);

          Domain anyDomain = createDomain();
          bad = createUserAccount(anyDomain);
          badTargets.add(bad);

        } else if (grantedOnTargetType == TargetType.global) {
          Domain anyDomain = createDomain();
          goodTargets.add(createCalendarResource(anyDomain));
        } else {
          badTargets.add(grantedOnTarget);
        }
        break;
      case cos:
        if (grantedOnTargetType == TargetType.cos) {
          good = grantedOnTarget;
        } else if (grantedOnTargetType == TargetType.global) {
          good = createCos();
        }

        if (good == null) {
          bad = grantedOnTarget;
          badTargets.add(bad);
        } else {
          goodTargets.add(good);
        }
        break;
      case dl:
        if (grantedOnTargetType == TargetType.dl) {
          // create a subgroup of the group on which the right is granted (testing multi levels of
          // dl)
          DistributionList subGroup = createUserDistributionList(domain);
          prov.addMembers((DistributionList) grantedOnTarget, new String[] {subGroup.getName()});

          goodTargets.add(subGroup);
          goodTargets.add(grantedOnTarget);
          badTargets.add(createUserDistributionList(domain));

        } else if (grantedOnTargetType == TargetType.group) {
          // dl rights apply to dynamic groups only for user rights
          if (right.isUserRight()) {
            goodTargets.add(grantedOnTarget);
          } else {
            badTargets.add(grantedOnTarget);
          }

        } else if (grantedOnTargetType == TargetType.domain) {
          goodTargets.add(createUserDistributionList(domain));

          if (right.isUserRight()) {
            goodTargets.add(createUserDynamicGroup(domain));
          } else {
            badTargets.add(createUserDynamicGroup(domain));
          }

          Domain anyDomain = createDomain();
          badTargets.add(createUserDistributionList(anyDomain));
          badTargets.add(createUserDynamicGroup(anyDomain));

        } else if (grantedOnTargetType == TargetType.global) {
          Domain anyDomain = createDomain();
          goodTargets.add(createUserDistributionList(anyDomain));

          if (right.isUserRight()) {
            goodTargets.add(createUserDynamicGroup(anyDomain));
          } else {
            badTargets.add(createUserDynamicGroup(anyDomain));
          }

        } else {
          badTargets.add(grantedOnTarget);
        }
        break;
      case group:
        if (grantedOnTargetType == TargetType.dl) {
          badTargets.add(grantedOnTarget);

        } else if (grantedOnTargetType == TargetType.group) {
          goodTargets.add(grantedOnTarget);

        } else if (grantedOnTargetType == TargetType.domain) {
          goodTargets.add(createUserDynamicGroup(domain));
          badTargets.add(createUserDistributionList(domain));

          Domain anyDomain = createDomain();
          badTargets.add(createUserDistributionList(anyDomain));
          badTargets.add(createUserDynamicGroup(anyDomain));

        } else if (grantedOnTargetType == TargetType.global) {
          Domain anyDomain = createDomain();
          goodTargets.add(createUserDynamicGroup(anyDomain));
          badTargets.add(createUserDistributionList(anyDomain));

        } else {
          badTargets.add(grantedOnTarget);
        }
        break;
      case domain:
        if (grantedOnTargetType == TargetType.domain) {
          goodTargets.add(grantedOnTarget);
          badTargets.add(createDomain());
        } else if (grantedOnTargetType == TargetType.global) {
          goodTargets.add(createDomain());
        } else {
          badTargets.add(grantedOnTarget);
        }
        break;
      case server:
        if (grantedOnTargetType == TargetType.server) {
          goodTargets.add(grantedOnTarget);
          badTargets.add(createServer());
        } else if (grantedOnTargetType == TargetType.global) {
          goodTargets.add(createServer());
        } else {
          badTargets.add(grantedOnTarget);
        }
        break;
      case ucservice:
        if (grantedOnTargetType == TargetType.ucservice) {
          goodTargets.add(grantedOnTarget);
          badTargets.add(createUCService());
        } else if (grantedOnTargetType == TargetType.global) {
          goodTargets.add(createUCService());
        } else {
          badTargets.add(grantedOnTarget);
        }
        break;
      case xmppcomponent:
        // skip for now
        return;
      case zimlet:
        // zimlet is trouble, need to reload it or else the grant is not on the object
        // ldapProvisioning.getZimlet does not return a cached entry so our grantedOnTarget
        // object does not have the grant
        prov.reload(grantedOnTarget);

        if (grantedOnTargetType == TargetType.zimlet) {
          goodTargets.add(grantedOnTarget);
          badTargets.add(createZimlet());
        } else if (grantedOnTargetType == TargetType.global) {
          goodTargets.add(createZimlet());
        } else {
          badTargets.add(grantedOnTarget);
        }
        break;
      case config:
        if (grantedOnTargetType == TargetType.config) goodTargets.add(grantedOnTarget);
        else if (grantedOnTargetType == TargetType.global) goodTargets.add(getConfig());
        else badTargets.add(grantedOnTarget);
        break;
      case global:
        if (grantedOnTargetType == TargetType.global) goodTargets.add(getGlobalGrant());
        else badTargets.add(grantedOnTarget);
        break;
      default:
        fail();
    }
  }

  private EffectiveRights getEffectiveRights(Account grantee, Entry target) {
    EffectiveRights effRights = null;
    boolean expectFailure = !grantee.isIsDelegatedAdminAccount();
    try {
      effRights =
          RightCommand.getEffectiveRights(
              prov,
              TargetType.getTargetType(target).getCode(),
              TargetBy.name,
              target.getLabel(),
              GranteeBy.name,
              grantee.getName(),
              false,
              false);
    } catch (ServiceException e) {
      // getEffectiveRights should not throw in the normal case.
      // The only expected exception is when the grantee is not a delegated admin
      if (!expectFailure) {
        e.printStackTrace();
        fail();
      }
    }

    if (expectFailure) {
      assertNull(effRights);
    } else {
      assertNotNull(effRights);
    }
    return effRights;
  }

  private AllEffectiveRights getAllEffectiveRights(Account grantee) {
    AllEffectiveRights allEffRights = null;
    boolean expectFailure = !grantee.isIsDelegatedAdminAccount();
    try {
      allEffRights =
          RightCommand.getAllEffectiveRights(
              prov, GranteeType.GT_USER.getCode(), GranteeBy.name, grantee.getName(), false, false);
    } catch (ServiceException e) {
      // getEffectiveRights should not throw in the normal case.
      // The only expected exception is when the grantee is not a delegated admin
      if (!expectFailure) {
        e.printStackTrace();
        fail();
      }
    }

    if (expectFailure) {
      assertNull(allEffRights);
    } else {
      assertNotNull(allEffRights);
    }
    return allEffRights;
  }

  private EffectiveRights getCreateObjectAttrs(Account grantee, Entry target) {
    EffectiveRights effRights = null;
    boolean expectFailure = false;

    try {
      String domainName = TargetType.getTargetDomainName(prov, target);
      TargetType targetType = TargetType.getTargetType(target);

      expectFailure =
          !grantee.isIsDelegatedAdminAccount()
              || targetType == TargetType.config
              || targetType == TargetType.global;

      effRights =
          RightCommand.getCreateObjectAttrs(
              prov,
              TargetType.getTargetType(target).getCode(),
              Key.DomainBy.name,
              domainName,
              null,
              null,
              GranteeBy.name,
              grantee.getName());

    } catch (ServiceException e) {
      if (!expectFailure) {
        e.printStackTrace();
        fail();
      }
    }

    if (expectFailure) {
      assertNull(effRights);
    } else {
      assertNotNull(effRights);
    }
    return effRights;
  }

  private boolean isPresetRightInEffectiveRights(EffectiveRights effRights, Right right) {
    return effRights.presetRights().contains(right.getName());
  }

  private boolean isAttrRightInEffectiveRights(
      EffectiveRights effRights, RightType rightType, boolean allAttrs, Set<String> attrs) {

    boolean found = false;

    if (rightType == RightType.getAttrs) {
      if (allAttrs) {
        found = effRights.canGetAllAttrs();
      } else {
        Set<String> allowedAttrs = effRights.canGetAttrs().keySet();
        found = allowedAttrs.containsAll(attrs);
      }
    } else if (rightType == RightType.setAttrs) {
      if (allAttrs) {
        found = effRights.canSetAllAttrs();
      } else {
        Set<String> allowedAttrs = effRights.canSetAttrs().keySet();
        found = allowedAttrs.containsAll(attrs);
      }
    } else {
      fail();
    }
    return found;
  }

  private boolean isRightInEffectiveRights(
      EffectiveRights effRights,
      Right right,
      RightType rightType,
      boolean allAttrs,
      Set<String> attrs) {
    boolean found = false;
    if (right.isPresetRight()) {
      found = isPresetRightInEffectiveRights(effRights, right);
    } else if (right.isAttrRight()) {
      AttrRight attrRight = (AttrRight) right;
      found = isAttrRightInEffectiveRights(effRights, rightType, allAttrs, attrs);
    } else {
      fail();
    }
    return found;
  }

  /*
   * RightType rightType, boolean allAttrs, Set<String> attrs
   * are params for attr rights, they are the criteria of rights
   * we are looking for.  Would be wrong to just pass the AttrRight
   * object from callsites, since that's the granted right.
   *
   * If we always pass the granted right, can't test negative case like:
   * if the granted right is a getAttrs right and we want to verify that
   * set attrs rights are no found)
   *
   * For preset right, currently the right passed is always the granted
   * right.  Can change callsites to pass other rights if needed.  We
   * currently don't have such test cases.
   */
  private boolean isRightInRightAggregation(
      RightAggregation rightAggr,
      boolean domainScope,
      Entry target,
      Right right,
      RightType rightType,
      boolean allAttrs,
      Set<String> attrs) {

    EffectiveRights effRights = rightAggr.effectiveRights();

    for (String entry : rightAggr.entries()) {
      boolean matchTarget = false;
      if (domainScope) {
        Domain domain = null;
        try {
          domain = TargetType.getTargetDomain(prov, target);
        } catch (ServiceException e) {
          e.printStackTrace();
          fail();
        }
        assertNotNull(domain);
        matchTarget = entry.equals(domain.getName());
      } else {
        matchTarget = entry.equals(target.getLabel());
      }

      if (!matchTarget) {
        continue;
      }

      boolean found = isRightInEffectiveRights(effRights, right, rightType, allAttrs, attrs);

      if (found) {
        return true;
      }
    }
    return false;
  }

  private boolean isRightInGetAllEffectiveRights(
      AllEffectiveRights allEffRights,
      Account grantee,
      Entry target,
      Right right,
      RightType rightType,
      boolean allAttrs,
      Set<String> attrs)
      throws ServiceException {

    TargetType targetType = TargetType.getTargetType(target);

    Map<TargetType, RightsByTargetType> rbttMap = allEffRights.rightsByTargetType();
    RightsByTargetType rbtt = rbttMap.get(targetType);

    if (rbtt != null) {
      boolean found = false;

      // all entries
      EffectiveRights effRights = rbtt.all();
      if (effRights != null) {
        found = isRightInEffectiveRights(effRights, right, rightType, allAttrs, attrs);
        if (found) {
          return true;
        }
      }

      // check domained entries
      if (rbtt instanceof DomainedRightsByTargetType) {
        DomainedRightsByTargetType domainedRights = (DomainedRightsByTargetType) rbtt;

        for (RightAggregation rightsByDomains : domainedRights.domains()) {
          found =
              isRightInRightAggregation(
                  rightsByDomains, true, target, right, rightType, allAttrs, attrs);
          if (found) {
            return true;
          }
        }
      }

      // check individual entry
      for (RightCommand.RightAggregation rightsByEntries : rbtt.entries()) {
        found =
            isRightInRightAggregation(
                rightsByEntries, false, target, right, rightType, allAttrs, attrs);
        if (found) {
          return true;
        }
      }
    }
    return false;
  }

  private void verifyPresetRight(Account grantee, Entry target, Right right, boolean expectedResult)
      throws ServiceException {

    //
    // verify canDo
    //
    boolean allow = false;
    try {
      allow = accessMgr.canDo(grantee, target, right, asAdmin(grantee), null);
    } catch (ServiceException e) {
      // the only reasonable exception is PERM_DENIED
      if (!ServiceException.PERM_DENIED.equals(e.getCode())) {
        fail();
      }
    }
    assertEquals(expectedResult, allow);

    //
    // verify getEffectiveRights
    //
    EffectiveRights effRights = getEffectiveRights(grantee, target);
    if (effRights != null) {
      allow = isPresetRightInEffectiveRights(effRights, right);
      assertEquals(expectedResult && !right.isUserRight(), allow);
    }

    //
    // verify getAllEffectiveRights
    //
    AllEffectiveRights allEffRights = getAllEffectiveRights(grantee);
    if (allEffRights != null) {
      allow =
          isRightInGetAllEffectiveRights(allEffRights, grantee, target, right, null, false, null);
      assertEquals(expectedResult && !right.isUserRight(), allow);
    }
  }

  private void verifyGetAttrs(
      Account grantee,
      Entry target,
      AttrRight attrRight,
      boolean canGrantBeInheritedForCreate,
      Set<String> attrs,
      boolean expectedResult)
      throws ServiceException {
    boolean allow = false;

    //
    // verify getAttr
    //
    try {
      allow = accessMgr.canGetAttrs(grantee, target, attrs, true);
    } catch (ServiceException e) {
      // the only reasonable exception is PERM_DENIED
      if (!ServiceException.PERM_DENIED.equals(e.getCode())) {
        e.printStackTrace();
        fail();
      }
    }
    assertEquals(expectedResult, allow);

    //
    // verify getEffectiveRights
    //
    EffectiveRights effRights = getEffectiveRights(grantee, target);
    if (effRights != null) {
      allow =
          isAttrRightInEffectiveRights(effRights, RightType.getAttrs, attrRight.allAttrs(), attrs);
      assertEquals(expectedResult, allow);
    }

    //
    // verify getAllEffectiveRights
    //
    AllEffectiveRights allEffRights = getAllEffectiveRights(grantee);
    if (allEffRights != null) {
      allow =
          isRightInGetAllEffectiveRights(
              allEffRights,
              grantee,
              target,
              attrRight,
              RightType.getAttrs,
              attrRight.allAttrs(),
              attrs);
      assertEquals(expectedResult, allow);
    }

    //
    // verify getCreateObjectAttrs
    //
    EffectiveRights effRightsCreate = getCreateObjectAttrs(grantee, target);
    if (effRightsCreate != null) {
      // getAttr rights are not returned by getCreateObjectAttrs via SOAP,
      // but they exist in the java object, just verify it.

      // Note: only inherited attr rights should be expected
      allow =
          isAttrRightInEffectiveRights(
              effRightsCreate, RightType.getAttrs, attrRight.allAttrs(), attrs);
      assertEquals(expectedResult && canGrantBeInheritedForCreate, allow);
    }
  }

  private void verifySetAttrs(
      Account grantee,
      Entry target,
      AttrRight attrRight,
      boolean canGrantBeInheritedForCreate,
      Set<String> attrs,
      boolean expectedResult)
      throws ServiceException {
    boolean allow = false;

    //
    // verify setAttr
    //
    try {
      allow = accessMgr.canSetAttrs(grantee, target, attrs, true);
    } catch (ServiceException e) {
      // the only reasonable exception is PERM_DENIED
      if (!ServiceException.PERM_DENIED.equals(e.getCode())) {
        fail();
      }
    }
    assertEquals(expectedResult, allow);

    //
    // verify getEffectiveRights
    //
    EffectiveRights effRights = getEffectiveRights(grantee, target);
    if (effRights != null) {
      allow =
          isAttrRightInEffectiveRights(effRights, RightType.setAttrs, attrRight.allAttrs(), attrs);
      assertEquals(expectedResult, allow);
    }

    //
    // verify getAllEffectiveRights
    //
    AllEffectiveRights allEffRights = getAllEffectiveRights(grantee);
    if (allEffRights != null) {
      allow =
          isRightInGetAllEffectiveRights(
              allEffRights,
              grantee,
              target,
              attrRight,
              RightType.setAttrs,
              attrRight.allAttrs(),
              attrs);
      assertEquals(expectedResult, allow);
    }

    //
    // verify getCreateObjectAttrs
    //
    EffectiveRights effRightsCreate = getCreateObjectAttrs(grantee, target);
    if (effRightsCreate != null) {
      // Note: only inherited attr rights should be expected
      allow =
          isAttrRightInEffectiveRights(
              effRightsCreate, RightType.setAttrs, attrRight.allAttrs(), attrs);
      assertEquals(expectedResult && canGrantBeInheritedForCreate, allow);
    }
  }

  private void verifyAttrRight(
      Account grantee,
      Entry target,
      AttrRight attrRight,
      boolean canGrantBeInheritedForCreate,
      boolean fromComboRight,
      boolean expectedResult)
      throws ServiceException {
    RightType rightType = attrRight.getRightType();

    Set<String> attrs =
        attrRight.allAttrs()
            ? TargetType.getAttrsInClass(target)
            : Sets.newHashSet(ATTR_ALLOWED_IN_THE_RIGHT);
    attrs = Collections.unmodifiableSet(attrs);

    // setAttr right also covers get
    verifyGetAttrs(grantee, target, attrRight, canGrantBeInheritedForCreate, attrs, expectedResult);

    // test set attr
    if (rightType == RightType.setAttrs) {
      verifySetAttrs(
          grantee, target, attrRight, canGrantBeInheritedForCreate, attrs, expectedResult);
    } else if (rightType == RightType.getAttrs) {
      // skip this test if this right is part of a combo right,
      // because the combo right might also have a setAttr right
      if (!fromComboRight) {
        verifySetAttrs(grantee, target, attrRight, canGrantBeInheritedForCreate, attrs, false);
      }
    } else {
      fail(); // no such thing
    }

    // verify attr not in the right is denied
    if (!attrRight.allAttrs()) {
      Set<String> badAttrs = Sets.newHashSet(ATTR_NOTALLOWED_IN_THE_RIGHT);
      badAttrs = Collections.unmodifiableSet(badAttrs);

      verifyGetAttrs(grantee, target, attrRight, canGrantBeInheritedForCreate, badAttrs, false);
      verifySetAttrs(grantee, target, attrRight, canGrantBeInheritedForCreate, badAttrs, false);
    }
  }

  private void verify(
      Entry target,
      boolean canGrantBeInheritedForCreate,
      List<Account> allowedAccts,
      List<Account> deniedAccts,
      Right right,
      boolean fromComboRight,
      boolean allowedExpected)
      throws Exception {
    if (target == null) {
      return;
    }

    for (Account allowedAcct : allowedAccts) {

      if (right.isPresetRight()) {
        verifyPresetRight(allowedAcct, target, right, allowedExpected);
      } else if (right.isAttrRight()) {
        verifyAttrRight(
            allowedAcct,
            target,
            (AttrRight) right,
            canGrantBeInheritedForCreate,
            fromComboRight,
            allowedExpected);
      } else {
        fail(); // not yet implemented
      }
    }

    // verify those should be denied
    for (Account deniedAcct : deniedAccts) {
      if (right.isPresetRight()) {
        verifyPresetRight(deniedAcct, target, right, false);
      } else if (right.isAttrRight()) {
        verifyAttrRight(
            deniedAcct,
            target,
            (AttrRight) right,
            canGrantBeInheritedForCreate,
            fromComboRight,
            false);
      } else {
        fail(); // not yet implemented
      }
    }
  }

  private static void revokeAllGrantsOnGlobalGrantAndGlobalConfig() throws Exception {

    Grants grants =
        RightCommand.getGrants(
            prov, TargetType.global.getCode(), null, null, null, null, null, false);

    revokeAllGrants(grants);

    grants =
        RightCommand.getGrants(
            prov, TargetType.config.getCode(), null, null, null, null, null, false);

    revokeAllGrants(grants);
  }

  private static void revokeAllGrants(Grants grants) throws Exception {
    for (RightCommand.ACE ace : grants.getACEs()) {
      RightCommand.revokeRight(
          prov,
          globalAdmin,
          ace.targetType(),
          TargetBy.id,
          ace.targetId(),
          ace.granteeType(),
          GranteeBy.id,
          ace.granteeId(),
          ace.right(),
          ace.rightModifier());
    }
  }

  // called from testOne()
  private void doTest(
      String note, TargetType grantedOnTargetType, GranteeType granteeType, Right right)
      throws Exception {
    doTest(note, grantedOnTargetType, granteeType, right, false);
  }

  // called from testOne()
  private void doTest(
      String note, TargetType grantedOnTargetType, TestGranteeType granteeType, Right right)
      throws Exception {
    doTest(note, grantedOnTargetType, granteeType, right, false);
  }

  private void doTest(
      String note,
      TargetType grantedOnTargetType,
      GranteeType granteeType,
      Right right,
      boolean skip)
      throws Exception {
    TestGranteeType testGranteeType = TestGranteeType.get(granteeType);

    doTest(note, grantedOnTargetType, testGranteeType, right, skip);
  }

  private void doTest(
      String note,
      TargetType grantedOnTargetType,
      TestGranteeType granteeType,
      Right right,
      boolean skip)
      throws Exception {
    try {
      if (skip) {
        skipTest(note, grantedOnTargetType, granteeType, right);
      } else {
        execTest(note, grantedOnTargetType, granteeType, right);
      }
    } finally {
      revokeAllGrantsOnGlobalGrantAndGlobalConfig();
      provUtil.deleteAllEntries();
    }
  }

  private static final Set<String> EXCLUDE_GRANTEE_TYPES =
      Sets.newHashSet(GranteeType.GT_EXT_GROUP.getCode(), GranteeType.GT_EMAIL.getCode());

  /*
   * full test
   */
  private void testAll() throws Exception {
    SKIP_FOR_REAL_LDAP_SERVER(SkipTestReason.LONG_TEST);

    int totalTests =
        TargetType.values().length * TestGranteeType.TEST_GRANTEE_TYPES.size() * rights.size();
    int curTest = 1;
    for (TargetType targetType : TargetType.values()) {
      for (TestGranteeType granteeType : TestGranteeType.TEST_GRANTEE_TYPES) {
        boolean skip = EXCLUDE_GRANTEE_TYPES.contains(granteeType.getCode());

        for (Right right : rights) {
          doTest((curTest++) + "/" + totalTests, targetType, granteeType, right, skip);
        }
      }
    }
  }

  /*
   * test a particular target type and a range of rights for all grantee types
   */
  private void testTarget() throws Exception {
    SKIP_FOR_REAL_LDAP_SERVER(SkipTestReason.LONG_TEST);

    /*
     *  account
     *  calresource
     *  cos
     *  dl
     *  group
     *  domain
     *  server
     *  ucservice
     *  xmppcomponent
     *  zimlet
     *  config
     *  global
     */

    TargetType targetType = TargetType.ucservice;

    int beginRight = 0; // sRights.indexOf(ADMIN_COMBO_ACCOUNT);  // inclusive
    int endRight = rights.size() - 1; // inclusive

    int totalTests = TestGranteeType.TEST_GRANTEE_TYPES.size() * (endRight - beginRight + 1);
    int curTest = 1;

    for (TestGranteeType granteeType : TestGranteeType.TEST_GRANTEE_TYPES) {
      boolean skip = EXCLUDE_GRANTEE_TYPES.contains(granteeType.getCode());

      // for (Right right : sRights) {
      for (int i = beginRight; i <= endRight; i++) {
        Right right = rights.get(i);
        doTest((curTest++) + "/" + totalTests, targetType, granteeType, right, skip);
      }
    }
  }

  /*
   * test a particular grantee type and a range of rights for all target types
   */
  private void testGrantee() throws Exception {
    SKIP_FOR_REAL_LDAP_SERVER(SkipTestReason.LONG_TEST);

    /*
     * TestGranteeType.GRANTEE_DYNAMIC_GROUP
     * GT_USER
     * GT_GROUP
     * GT_EXT_GROUP
     * GT_AUTHUSER
     * GT_DOMAIN
     * GT_GUEST
     * GT_KEY
     * GT_PUBLIC
     */
    TestGranteeType granteeType = TestGranteeType.GRANTEE_DYNAMIC_GROUP;
    int beginRight = 0; // sRights.indexOf(ADMIN_COMBO_ACCOUNT);  // inclusive
    int endRight = rights.size() - 1; // inclusive

    int totalTests = TargetType.values().length * rights.size();
    int curTest = 1;

    for (TargetType targetType : TargetType.values()) {
      for (Right right : rights) {
        doTest((curTest++) + "/" + totalTests, targetType, granteeType, right, false);
      }
    }
  }

  /*
   * test a particular right for all target types and grantee types
   */
  private void testRight() throws Exception {
    SKIP_FOR_REAL_LDAP_SERVER(SkipTestReason.LONG_TEST);

    Right right = ACLTestUtil.ADMIN_COMBO_ACCOUNT;

    int totalTests =
        TargetType.values().length * TestGranteeType.TEST_GRANTEE_TYPES.size() * rights.size();
    int curTest = 1;
    for (TargetType targetType : TargetType.values()) {
      for (TestGranteeType granteeType : TestGranteeType.TEST_GRANTEE_TYPES) {
        boolean skip = EXCLUDE_GRANTEE_TYPES.contains(granteeType.getCode());

        doTest((curTest++) + "/" + totalTests, targetType, granteeType, right, skip);
      }
    }
  }

  /*
   * do a specific test
   */
  private void testOne() throws Exception {
    // test a particular grant target and grantee type and right
    // doTest("1/1", TargetType.account, GranteeType.GT_USER, ADMIN_ATTR_GETALL_ACCOUNT);
    // doTest("1/1", TargetType.account, GranteeType.GT_USER, ADMIN_PRESET_ACCOUNT);
    // doTest("1/1", TargetType.account, GranteeType.GT_USER, ADMIN_ATTR_GETALL_ACCOUNT);
    // doTest("1/1", TargetType.account, GranteeType.GT_USER, ADMIN_ATTR_SETALL_ACCOUNT);
    // doTest("1/1", TargetType.account, GranteeType.GT_USER, ADMIN_COMBO_ACCOUNT);
    // doTest("1/1", TargetType.account, GranteeType.GT_USER, ADMIN_COMBO_ALL);
    // doTest("1/1", TargetType.account, GranteeType.GT_USER, ADMIN_PRESET_CALENDAR_RESOURCE);
    // doTest("1/1", TargetType.account, GranteeType.GT_USER, USER_LOGIN_AS);
    // doTest("1/1", TargetType.account, GranteeType.GT_GROUP, USER_RIGHT);
    // doTest("1/1", TargetType.account, GranteeType.GT_GUEST, USER_RIGHT);
    // doTest("1/1", TargetType.account, GranteeType.GT_KEY, USER_RIGHT);
    // doTest("1/1", TargetType.account, TestGranteeType.GRANTEE_DYNAMIC_GROUP,
    // ADMIN_ATTR_GETALL_ACCOUNT);
    // doTest("1/1", TargetType.account, GranteeType.GT_EXT_GROUP, ADMIN_PRESET_ACCOUNT);

    // doTest("1/1", TargetType.calresource, GranteeType.GT_USER, USER_RIGHT);
    // doTest("1/1", TargetType.calresource, GranteeType.GT_USER, ADMIN_PRESET_ACCOUNT);
    // doTest("1/1", TargetType.calresource, GranteeType.GT_USER, ADMIN_PRESET_CALENDAR_RESOURCE);

    // doTest("1/1", TargetType.config, TestGranteeType.GRANTEE_DYNAMIC_GROUP,
    // ADMIN_ATTR_GETALL_ACCOUNT);

    // doTest("1/1", TargetType.global, TestGranteeType.GRANTEE_DYNAMIC_GROUP,
    // ADMIN_PRESET_LOGIN_AS);
    // doTest("1/1", TargetType.global, GranteeType.GT_GROUP, ADMIN_PRESET_LOGIN_AS);
    // doTest("1/1", TargetType.global, TestGranteeType.GRANTEE_DYNAMIC_GROUP, ADMIN_COMBO_ALL);
    // doTest("1/1", TargetType.global, GranteeType.GT_USER, ADMIN_COMBO_ALL);

    // doTest("1/1", TargetType.dl, GranteeType.GT_USER, USER_LOGIN_AS);
    // doTest("1/1", TargetType.dl, GranteeType.GT_USER, ADMIN_RIGHT_ACCOUNT);
    // doTest("1/1", TargetType.dl, GranteeType.GT_USER, USER_RIGHT_DISTRIBUTION_LIST);
    // doTest("1/1", TargetType.dl, GranteeType.GT_USER, ADMIN_RIGHT_ZIMLET);
    // doTest("1/1", TargetType.dl, GranteeType.GT_USER, ADMIN_ATTR_GETALL_ACCOUNT);
    // doTest("1/1", TargetType.dl, GranteeType.GT_GUEST, USER_RIGHT_DISTRIBUTION_LIST);
    // doTest("1/1", TargetType.dl, TestGranteeType.GRANTEE_DYNAMIC_GROUP,
    // ACLTestUtil.ADMIN_PRESET_LOGIN_AS);

    // doTest("1/1", TargetType.group, GranteeType.GT_USER, USER_RIGHT_DISTRIBUTION_LIST);
    // doTest("1/1", TargetType.group, GranteeType.GT_USER, ADMIN_PRESET_LOGIN_AS);
    // doTest("1/1", TargetType.group, GranteeType.GT_USER, ADMIN_PRESET_CONFIG);
    // doTest("1/1", TargetType.group, GranteeType.GT_USER, ADMIN_RIGHT_CALENDAR_RESOURCE);
    // doTest("1/1", TargetType.group, GranteeType.GT_USER, ADMIN_ATTR_SETSOME_DISTRIBUTION_LIST);

    // doTest("1/1", TargetType.domain, GranteeType.GT_USER, USER_LOGIN_AS);
    // doTest("1/1", TargetType.domain, GranteeType.GT_USER, ADMIN_PRESET_LOGIN_AS);
    // doTest("1/1", TargetType.domain, GranteeType.GT_USER, ADMIN_PRESET_DISTRIBUTION_LIST);
    // doTest("1/1", TargetType.domain, GranteeType.GT_USER, ADMIN_PRESET_DOMAIN);
    // doTest("1/1", TargetType.domain, GranteeType.GT_EXT_GROUP, ADMIN_PRESET_DOMAIN);
    // doTest("1/1", TargetType.domain, GranteeType.GT_EXT_GROUP, USER_RIGHT_DOMAIN);
    // doTest("1/1", TargetType.domain, GranteeType.GT_DOMAIN, ACLTestUtil.USER_RIGHT_DOMAIN);

    // doTest("1/1", TargetType.config, GranteeType.GT_EXT_GROUP, ADMIN_ATTR_GETALL_CONFIG);

    doTest(
        "1/1",
        TargetType.ucservice,
        TestGranteeType.GRANTEE_DYNAMIC_GROUP,
        ACLTestUtil.USER_LOGIN_AS);

    // doTest("1/1", TargetType.zimlet, TestGranteeType.GRANTEE_DYNAMIC_GROUP,
    // ADMIN_COMBO_XMPP_COMPONENT);

  }

  /*
   * test basic target-grantee-right combos
   */
  @Test
  public void test() throws Exception {

    Config config = Provisioning.getInstance().getConfig();
    config.setUCProviderEnabled(ProvTestUtil.DEFAULT_UC_PROVIDER);

    // testAll();
    // testTarget();
    testGrantee();
    // testOne();
  }
}
