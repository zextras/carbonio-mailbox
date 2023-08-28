// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.account.accesscontrol;

import com.zimbra.common.account.Key;
import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.AccountServiceException;
import com.zimbra.cs.account.CalendarResource;
import com.zimbra.cs.account.Config;
import com.zimbra.cs.account.Cos;
import com.zimbra.cs.account.DistributionList;
import com.zimbra.cs.account.Domain;
import com.zimbra.cs.account.DynamicGroup;
import com.zimbra.cs.account.Entry;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.Provisioning.GroupMembership;
import com.zimbra.cs.account.Server;
import com.zimbra.cs.account.XMPPComponent;
import com.zimbra.cs.account.Zimlet;
import java.util.HashMap;
import java.util.Map;

/**
 * @author pshao
 */
public class PseudoTarget {

  private PseudoTarget() {
    throw new IllegalStateException("Utility class");
  }

  static class PseudoZimbraId {

    private PseudoZimbraId() {
      throw new IllegalStateException("Utility class");
    }

    private static final String PSEUDO_ZIMBRA_ID = "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa";

    static String getPseudoZimbraId() {
      return PSEUDO_ZIMBRA_ID;
    }
  }

  public static boolean isPseudoEntry(Entry entry) {
    return entry instanceof PseudoAccount
        || entry instanceof PseudoCalendarResource
        || entry instanceof PseudoDistributionList
        || entry instanceof PseudoCos
        || entry instanceof PseudoDomain
        || entry instanceof PseudoServer
        || entry instanceof PseudoXMPPComponent
        || entry instanceof PseudoZimlet;
  }

  /*
   * PseudoAccount
   * PseudoCalendarResource
   * PseudoCalendarResource
   *
   * can have a real or a pseudo domain
   *   - if having a pseudo domain, the getPseudoDomain method will return the pseudo domain
   *   - if having a real domain, the getPseudoDomain method will return null
   *
   * can have
   */

  static class PseudoAccount extends Account {
    Domain mPseudoDomain;
    GroupMembership mAclGroups;

    PseudoAccount(
        String name,
        String id,
        Map<String, Object> attrs,
        Map<String, Object> defaults,
        Provisioning prov,
        Domain pseudoDomain) {
      super(name, id, attrs, defaults, prov);
      mPseudoDomain = pseudoDomain;
    }

    /*
     * create a pseudo account that is a member of the specified group.
     *
     * The acl groups this account belongs are essentially the acl groups
     * of the specified group, plus the group.
     */
    PseudoAccount(
        String name,
        String id,
        Map<String, Object> attrs,
        Map<String, Object> defaults,
        Provisioning prov,
        DistributionList group)
        throws ServiceException {
      super(name, id, attrs, defaults, prov);
      mAclGroups = prov.getGroupMembership(group, false);
    }

    Domain getPseudoDomain() {
      return mPseudoDomain;
    }

    GroupMembership getAclGroups() {
      return mAclGroups;
    }
  }

  static class PseudoCalendarResource extends CalendarResource {
    Domain mPseudoDomain;

    public PseudoCalendarResource(
        String name,
        String id,
        Map<String, Object> attrs,
        Map<String, Object> defaults,
        Provisioning prov,
        Domain pseudoDomain) {
      super(name, id, attrs, defaults, prov);
      mPseudoDomain = pseudoDomain;
    }

    Domain getPseudoDomain() {
      return mPseudoDomain;
    }
  }

  static class PseudoDistributionList extends DistributionList {
    Domain mPseudoDomain;

    public PseudoDistributionList(
        String name, String id, Map<String, Object> attrs, Provisioning prov, Domain pseudoDomain) {
      super(name, id, attrs, prov);
      mPseudoDomain = pseudoDomain;
    }

    Domain getPseudoDomain() {
      return mPseudoDomain;
    }
  }

  static class PseudoDynamicGroup extends DynamicGroup {
    Domain mPseudoDomain;

    public PseudoDynamicGroup(
        String name, String id, Map<String, Object> attrs, Provisioning prov, Domain pseudoDomain) {
      super(name, id, attrs, prov);
      mPseudoDomain = pseudoDomain;
    }

    Domain getPseudoDomain() {
      return mPseudoDomain;
    }
  }

  static class PseudoCos extends Cos {
    private PseudoCos(String name, String id, Map<String, Object> attrs, Provisioning prov) {
      super(name, id, attrs, prov);
    }
  }

  static class PseudoDomain extends Domain {
    private PseudoDomain(
        String name,
        String id,
        Map<String, Object> attrs,
        Map<String, Object> defaults,
        Provisioning prov) {
      super(name, id, attrs, defaults, prov);
    }
  }

  static class PseudoServer extends Server {
    private PseudoServer(
        String name,
        String id,
        Map<String, Object> attrs,
        Map<String, Object> defaults,
        Provisioning prov) {
      super(name, id, attrs, defaults, prov);
    }
  }

  static class PseudoXMPPComponent extends XMPPComponent {
    private PseudoXMPPComponent(
        String name, String id, Map<String, Object> attrs, Provisioning prov) {
      super(name, id, attrs, prov);
    }
  }

  static class PseudoZimlet extends Zimlet {
    private PseudoZimlet(String name, String id, Map<String, Object> attrs, Provisioning prov) {
      super(name, id, attrs, prov);
    }
  }

  /**
   * short hand for PseudoTarget.createPseudoTarget(prov, TargetType.domain, null, null, false,
   * null, null);
   *
   * @param prov
   * @return
   * @throws ServiceException
   */
  public static Domain createPseudoDomain(Provisioning prov) throws ServiceException {
    return (Domain) createPseudoTarget(prov, TargetType.domain, null, null, false, null, null);
  }

  /**
   * creagte a pseudo domain with the real name.
   *
   * <p>This is only used for computing settable attrs when creating a domain. We need to be able to
   * traverse the domain hierarchy for grants with the subDomain modifier.
   *
   * @param prov
   * @param domainName
   * @return
   * @throws ServiceException
   */
  public static Domain createPseudoDomain(Provisioning prov, String domainName)
      throws ServiceException {
    return (Domain)
        createPseudoTarget(prov, TargetType.domain, null, null, false, null, null, domainName);
  }

  public static Entry createPseudoTarget(
      Provisioning prov,
      TargetType targetType,
      Key.DomainBy domainBy,
      String domainStr,
      boolean createPseudoDomain,
      Key.CosBy cosBy,
      String cosStr)
      throws ServiceException {
    return createPseudoTarget(
        prov, targetType, domainBy, domainStr, createPseudoDomain, cosBy, cosStr, null);
  }

  /**
   * construct a pseudo target
   *
   * <p>if targetType is a domain-ed type: account. cr, dl: then exactly one of the following must
   * be passed in: - domainBy == null domainStr == null createPseudoDomain == true or - domainBy !=
   * null domainStr != null createPseudoDomain == false
   *
   * @param prov
   * @param targetType
   * @param domainBy
   * @param domainStr
   * @param createPseudoDomain
   * @param cosBy
   * @param cosStr
   * @param domainName used only when targetType is domain, ignored otherwise. if not null, the
   *     pseudo domain will be created with the provided domainName, not a pseudo name. This is only
   *     used/needed for computing settable attrs when creating a domain. We need to be able to
   *     traverse the domain hierarchy for grants with the subDomain modifier.
   * @return
   * @throws ServiceException
   */
  public static Entry createPseudoTarget(
      Provisioning prov,
      TargetType targetType,
      Key.DomainBy domainBy,
      String domainStr,
      boolean createPseudoDomain,
      Key.CosBy cosBy,
      String cosStr,
      String domainName)
      throws ServiceException {

    Entry targetEntry = null;
    Config config = prov.getConfig();

    String zimbraId = PseudoZimbraId.getPseudoZimbraId();
    Map<String, Object> attrMap = new HashMap<String, Object>();
    attrMap.put(Provisioning.A_zimbraId, zimbraId);

    Domain pseudoDomain = null;
    Domain domain = null;
    if (targetType == TargetType.account
        || targetType == TargetType.calresource
        || targetType == TargetType.dl
        || targetType == TargetType.group) {

      if (createPseudoDomain) {
        domain =
            pseudoDomain =
                (Domain) createPseudoTarget(prov, TargetType.domain, null, null, false, null, null);
      } else {
        if (domainBy == null || domainStr == null)
          throw ServiceException.INVALID_REQUEST(
              "domainBy and domain identifier is required", null);
        domain = prov.get(domainBy, domainStr);
      }
      if (domain == null) {
        throw AccountServiceException.NO_SUCH_DOMAIN(domainStr);
      }
    }

    switch (targetType) {
      case account:
      case calresource:
        Cos cos = null;
        if (cosBy != null && cosStr != null) {
          cos = prov.get(cosBy, cosStr);
          if (cos == null) {
            throw AccountServiceException.NO_SUCH_COS(cosStr);
          }
          attrMap.put(Provisioning.A_zimbraCOSId, cos.getId());
        } else {
          String domainCosId =
              domain != null ? domain.getAttr(Provisioning.A_zimbraDomainDefaultCOSId, null) : null;
          if (domainCosId != null) {
            cos = prov.get(Key.CosBy.id, domainCosId);
          }
          if (cos == null) {
            cos = prov.get(Key.CosBy.name, Provisioning.DEFAULT_COS_NAME);
          }
        }

        if (targetType == TargetType.account) {
          targetEntry =
              new PseudoAccount(
                  "pseudo@" + domain.getName(),
                  zimbraId,
                  attrMap,
                  cos.getAccountDefaults(),
                  prov,
                  pseudoDomain);
        } else {
          targetEntry =
              new PseudoCalendarResource(
                  "pseudo@" + domain.getName(),
                  zimbraId,
                  attrMap,
                  cos.getAccountDefaults(),
                  prov,
                  pseudoDomain);
        }
        break;

      case cos:
        targetEntry = new PseudoCos("pseudocos", zimbraId, attrMap, prov);
        break;
      case dl:
        targetEntry =
            new PseudoDistributionList(
                "pseudo@" + domain.getName(), zimbraId, attrMap, prov, pseudoDomain);
        break;
      case group:
        targetEntry =
            new PseudoDynamicGroup(
                "pseudo@" + domain.getName(), zimbraId, attrMap, prov, pseudoDomain);
        break;
      case domain:
        String name = domainName == null ? "pseudo.pseudo" : domainName;
        targetEntry = new PseudoDomain(name, zimbraId, attrMap, config.getDomainDefaults(), prov);
        break;
      case server:
        targetEntry =
            new PseudoServer("pseudo.pseudo", zimbraId, attrMap, config.getServerDefaults(), prov);
        break;
      case xmppcomponent:
        targetEntry = new PseudoXMPPComponent("pseudo", zimbraId, attrMap, prov);
        break;
      case zimlet:
        targetEntry = new PseudoZimlet("pseudo", zimbraId, attrMap, prov);
        break;
      default:
        throw ServiceException.INVALID_REQUEST(
            "unsupported target for createPseudoTarget: " + targetType.getCode(), null);
    }

    return targetEntry;
  }
}
