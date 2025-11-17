package com.zimbra.cs.account.accesscontrol;

import com.zimbra.common.account.Key;
import com.zimbra.common.account.Key.AccountBy;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.util.SetUtil;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.AccountServiceException;
import com.zimbra.cs.account.AttributeClass;
import com.zimbra.cs.account.AttributeEntry;
import com.zimbra.cs.account.AttributeManager;
import com.zimbra.cs.account.CalendarResource;
import com.zimbra.cs.account.Config;
import com.zimbra.cs.account.Cos;
import com.zimbra.cs.account.DistributionList;
import com.zimbra.cs.account.Domain;
import com.zimbra.cs.account.DynamicGroup;
import com.zimbra.cs.account.Entry;
import com.zimbra.cs.account.GlobalGrant;
import com.zimbra.cs.account.NamedEntry;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.Server;
import com.zimbra.cs.account.StoreAttributeManager;
import com.zimbra.cs.account.Zimlet;
import com.zimbra.cs.account.ldap.LdapDIT;
import com.zimbra.cs.account.ldap.LdapProv;
import com.zimbra.soap.admin.type.EffectiveRightsTargetSelector;
import com.zimbra.soap.type.TargetBy;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public enum TargetTypeLookup {
	;

	public static Entry lookupTarget(Provisioning prov, EffectiveRightsTargetSelector targSel)
			throws ServiceException {
		return lookupTarget(prov, targSel, true);
	}

	public static Entry lookupTarget(
			Provisioning prov, EffectiveRightsTargetSelector targSel, boolean mustFind)
			throws ServiceException {
		return lookupTarget(
				prov, TargetType.fromJaxb(targSel.getType()), targSel.getBy(), targSel.getValue(), mustFind);
	}

	public static Entry lookupTarget(
			Provisioning prov, TargetType targetType, TargetBy targetBy, String target)
			throws ServiceException {
		return lookupTarget(prov, targetType, targetBy, target, true);
	}

	public static Entry lookupTarget(
			Provisioning prov, TargetType targetType, TargetBy targetBy, String target, boolean mustFind)
			throws ServiceException {
		return lookupTarget(prov, targetType, targetBy, target, false, mustFind);
	}

	/**
	 * central place where a target should be loaded
	 *
	 * @param prov
	 * @param targetType
	 * @param targetBy
	 * @param target
	 * @return
	 * @throws ServiceException
	 */
	public static Entry lookupTarget(
			Provisioning prov,
			TargetType targetType,
			TargetBy targetBy,
			String target,
			boolean needFullDL,
			boolean mustFind)
			throws ServiceException {
		Entry targetEntry = null;

		switch (targetType) {
			case account:
				targetEntry = prov.get(AccountBy.fromString(targetBy.name()), target);
				if (targetEntry == null && mustFind) {
					throw AccountServiceException.NO_SUCH_ACCOUNT(target);
				}
				break;
			case calresource:
				targetEntry = prov.get(Key.CalendarResourceBy.fromString(targetBy.name()), target);
				if (targetEntry == null && mustFind) {
					throw AccountServiceException.NO_SUCH_CALENDAR_RESOURCE(target);
				}
				break;
			case dl:
				if (needFullDL) {
					targetEntry = prov.getGroup(Key.DistributionListBy.fromString(targetBy.name()), target);
				} else {
					targetEntry =
							prov.getGroupBasic(Key.DistributionListBy.fromString(targetBy.name()), target);
				}
				if (targetEntry == null && mustFind) {
					throw AccountServiceException.NO_SUCH_DISTRIBUTION_LIST(target);
				}
				break;
			case group:
				targetEntry =
						prov.getGroupBasic(Key.DistributionListBy.fromString(targetBy.name()), target);
				if (targetEntry == null && mustFind) {
					throw AccountServiceException.NO_SUCH_DISTRIBUTION_LIST(target);
				}
				break;
			case domain:
				targetEntry = prov.get(Key.DomainBy.fromString(targetBy.name()), target);
				if (targetEntry == null && mustFind) {
					throw AccountServiceException.NO_SUCH_DOMAIN(target);
				}
				break;
			case cos:
				targetEntry = prov.get(Key.CosBy.fromString(targetBy.name()), target);
				if (targetEntry == null && mustFind) {
					throw AccountServiceException.NO_SUCH_COS(target);
				}
				break;
			case server:
				targetEntry = prov.get(Key.ServerBy.fromString(targetBy.name()), target);
				if (targetEntry == null && mustFind) {
					throw AccountServiceException.NO_SUCH_SERVER(target);
				}
				break;
			case zimlet:
				Key.ZimletBy zimletBy = Key.ZimletBy.fromString(targetBy.name());
				if (zimletBy != Key.ZimletBy.name) {
					throw ServiceException.INVALID_REQUEST("zimlet must be by name", null);
				}
				targetEntry = prov.getZimlet(target);
				if (targetEntry == null && mustFind) {
					throw AccountServiceException.NO_SUCH_ZIMLET(target);
				}
				break;
			case config:
				targetEntry = prov.getConfig();
				break;
			case global:
				targetEntry = prov.getGlobalGrant();
				break;
			default:
				ServiceException.INVALID_REQUEST(
						"invallid target type for lookupTarget:" + targetType, null);
		}

		return targetEntry;
	}

	public static String getTargetDomainName(Provisioning prov, AttributeEntry target)
			throws ServiceException {

		if (target instanceof CalendarResource) {
			CalendarResource cr = (CalendarResource) target;
			return cr.getDomainName();
		} else if (target instanceof Account) {
			Account acct = (Account) target;
			return acct.getDomainName();
		} else if (target instanceof DistributionList) {
			DistributionList dl = (DistributionList) target;
			return dl.getDomainName();
		} else if (target instanceof DynamicGroup) {
			DynamicGroup group = (DynamicGroup) target;
			return group.getDomainName();
		} else {
			return null;
		}
	}

	/*
	 * This method is called for searching for negative grants granted on a
	 * "sub-target" of a target on which we are granting a right.  If the
	 * granting account has any negative grants for a right that is a
	 * "sub-right" of the right he is trying to grant to someone else,
	 * and this negative grant is on a "sub-target" of the target he is
	 * trying to grant on, then sorry, it is not allowed.  Because otherwise
	 * the person receiving the grant can end up getting "more" rights
	 * than the granting person.
	 *
	 * e.g. on domain D.com, adminA +domainAdminRights
	 *      on dl dl@D.com,  adminA -setPassword
	 *
	 *      When adminA tries to grant domainAdminRights to adminB on
	 *      domain D.com, it should not be allowed; otherwise adminB
	 *      can setPassword for accounts in dl@D.com, but adminA cannot.
	 *
	 * The targetTypes parameter contains a set of target types that are
	 * "sub-target" of the target type on which we are trying to grant.
	 *
	 * SO, for the search base:
	 *   - for domain-ed targets, dls must be under the domain, but accounts
	 *     in dls can be in any domain, so the search base is the mail branch base.
	 *   - for non domain-ed targets, the search base is the base DN for the type
	 *
	 *   we go through all wanted target types, find the least common base
	 */
	static Map<String, Set<String>> getSearchBasesAndOCs(
			Provisioning prov, Set<TargetType> targetTypes) throws ServiceException {

		// sanity check, is really an internal error if targetTypes is empty
		if (targetTypes.isEmpty()) return null;

		Map<String, Set<String>> tempResult = new HashMap<>();

		for (TargetType tt : targetTypes) {
			String base = getSearchBase(prov, tt);

			String oc = tt.getAttributeClass().getOCName();
			Set<String> ocs = tempResult.get(base);
			if (ocs == null) {
				ocs = new HashSet<>();
				tempResult.put(base, ocs);
			}
			ocs.add(oc);
		}

		// optimize
		LdapDIT dit = ((LdapProv) prov).getDIT();
		String configBranchBase = dit.configBranchBaseDN();
		Set<String> mailBranchOCs = new HashSet<>();
		Set<String> configBranchOCs = new HashSet<>();

		String leastCommonBaseInMailBranch = null;
		String leastCommonBaseInConfigBranch = null;

		for (Map.Entry<String, Set<String>> entry : tempResult.entrySet()) {
			String base = entry.getKey();
			Set<String> ocs = entry.getValue();

			boolean inConfigBranch = base.endsWith(configBranchBase);
			if (inConfigBranch) {
				configBranchOCs.addAll(ocs);

				if (leastCommonBaseInConfigBranch == null) {
					leastCommonBaseInConfigBranch = base;
				} else {
					leastCommonBaseInConfigBranch = TargetType.getCommonBase(base, leastCommonBaseInConfigBranch);
				}

			} else {
				mailBranchOCs.addAll(ocs);

				if (leastCommonBaseInMailBranch == null) {
					leastCommonBaseInMailBranch = base;
				} else {
					leastCommonBaseInMailBranch = TargetType.getCommonBase(base, leastCommonBaseInMailBranch);
				}
			}
		}

		Map<String, Set<String>> result = new HashMap<>();

		// if zimbra default DIT and both mail branch and config branch are needed, merge the two
		if (LdapDIT.isZimbraDefault(dit)) {
			if (leastCommonBaseInMailBranch != null && leastCommonBaseInConfigBranch != null) {
				// merge the two
				String commonBase =
						TargetType.getCommonBase(leastCommonBaseInMailBranch, leastCommonBaseInConfigBranch);
				Set<String> allOCs = SetUtil.union(mailBranchOCs, configBranchOCs);
				result.put(commonBase, allOCs);
				return result;
			}
		}

		// bug 48272, do two searches, one based at the mail branch, one based on the config branch.
		if (leastCommonBaseInMailBranch != null) {
			result.put(leastCommonBaseInMailBranch, mailBranchOCs);
		}
		if (leastCommonBaseInConfigBranch != null) {
			result.put(leastCommonBaseInConfigBranch, configBranchOCs);
		}

		return result;
	}

	static String getSearchBase(Provisioning prov, TargetType tt) throws ServiceException {
		LdapDIT dit = ((LdapProv) prov).getDIT();

		String base;

		switch (tt) {
			case account:
			case calresource:
			case dl:
			case group:
				base = dit.mailBranchBaseDN();
				break;
			case domain:
				base = dit.domainBaseDN();
				break;
			case cos:
				base = dit.cosBaseDN();
				break;
			case server:
				base = dit.serverBaseDN();
				break;
			case zimlet:
				base = dit.zimletBaseDN();
				break;
			case config:
				base = dit.configDN();
				break;
			case global:
				// is really an internal error, globalgrant should never appear in the
				// targetTypes if we get here, because it is not a sub-target of any
				// other target types.
				base = dit.globalGrantDN();
				break;
			default:
				throw ServiceException.FAILURE("internal error", null);
		}

		return base;
	}

	public static Domain getTargetDomain(Provisioning prov, AttributeEntry target) throws ServiceException {

		if (target instanceof CalendarResource) {
			CalendarResource cr = (CalendarResource) target;
			return prov.getDomain(cr);
		} else if (target instanceof Account) {
			Account acct = (Account) target;
			return prov.getDomain(acct);
		} else if (target instanceof DistributionList) {
			DistributionList dl = (DistributionList) target;
			return prov.getDomain(dl);
		} else if (target instanceof DynamicGroup) {
			DynamicGroup group = (DynamicGroup) target;
			return prov.getDomain(group);
		} else return null;
	}

	static AttributeClass getAttributeClass(AttributeEntry target) throws ServiceException {
		return getTargetType(target).getAttributeClass();
	}

	public static TargetType getTargetType(AttributeEntry target) throws ServiceException {

		if (target instanceof CalendarResource) return TargetType.calresource;
		else if (target instanceof Account) return TargetType.account;
		else if (target instanceof Domain) return TargetType.domain;
		else if (target instanceof Cos) return TargetType.cos;
		else if (target instanceof DistributionList) return TargetType.dl;
		else if (target instanceof DynamicGroup) return TargetType.group;
		else if (target instanceof Server) return TargetType.server;
		else if (target instanceof Config) return TargetType.config;
		else if (target instanceof GlobalGrant) return TargetType.global;
		else if (target instanceof Zimlet) return TargetType.zimlet;
		else
			throw ServiceException.FAILURE(
					"internal error, target is : "
							+ (target == null ? "null" : target.getClass().getCanonicalName()),
					null);
	}

	public static String getId(AttributeEntry target) {
		return (target instanceof NamedEntry) ? ((NamedEntry) target).getId() : null;
	}

	public static boolean canBeInheritedFrom(Entry target) throws ServiceException {
		TargetType targetType = getTargetType(target);
		return !targetType.subTargetTypes().isEmpty();
	}

	public static Set<String> getAttrsInClass(AttributeEntry target) throws ServiceException {
		AttributeClass klass = getAttributeClass(target);
		return StoreAttributeManager.getInstance().getAllAttrsInClass(klass);
	}

	static class SearchBaseAndOC {
		String mBase;
		List<String> mOCs;
	}
}
