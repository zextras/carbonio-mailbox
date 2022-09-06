// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.service.admin;

import com.zimbra.common.account.Key;
import com.zimbra.common.account.Key.AccountBy;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.AdminConstants;
import com.zimbra.common.soap.Element;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.DistributionList;
import com.zimbra.cs.account.NamedEntry;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.Provisioning.GroupMembership;
import com.zimbra.cs.account.accesscontrol.AdminRight;
import com.zimbra.cs.account.accesscontrol.Rights.Admin;
import com.zimbra.soap.ZimbraSoapContext;
import com.zimbra.soap.admin.message.GetAdminConsoleUICompRequest;
import com.zimbra.soap.admin.type.DistributionListSelector;
import com.zimbra.soap.type.AccountSelector;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class GetAdminConsoleUIComp extends AdminDocumentHandler {

  @Override
  public boolean domainAuthSufficient(Map context) {
    return true;
  }

  /**
   * @return true - which means accept responsibility for measures to prevent account harvesting by
   *     delegate admins
   */
  @Override
  public boolean defendsAgainstDelegateAdminAccountHarvesting() {
    return true;
  }

  @Override
  public Element handle(Element request, Map<String, Object> context) throws ServiceException {
    ZimbraSoapContext zsc = getZimbraSoapContext(context);
    Provisioning prov = Provisioning.getInstance();
    GetAdminConsoleUICompRequest req = zsc.elementToJaxb(request);
    AccountSelector accountSel = req.getAccount();
    DistributionListSelector dlSel = req.getDl();

    Element resp = zsc.createElement(AdminConstants.GET_ADMIN_CONSOLE_UI_COMP_RESPONSE);

    if ((null != accountSel) && (null != dlSel)) {
      throw ServiceException.INVALID_REQUEST("can only specify eith account or dl", null);
    }

    Account authedAcct = getAuthenticatedAccount(zsc);

    Set<String> added = new HashSet<String>();
    GroupMembership aclGroups = null;

    if (accountSel != null) {
      AccountBy by = accountSel.getBy().toKeyAccountBy();
      String key = accountSel.getKey();
      Account acct = prov.get(by, key);
      AccountHarvestingCheckerUsingCheckRight checker =
          new AccountHarvestingCheckerUsingCheckRight(zsc, context, Admin.R_viewAccountAdminUI);
      if (acct == null) {
        defendAgainstAccountHarvestingWhenAbsent(by, key, zsc, checker);
      } else {
        if (!authedAcct.getId().equals(acct.getId())) {
          defendAgainstAccountHarvesting(acct, by, key, zsc, checker);
        }
        addValues(acct, resp, added, false);
        aclGroups = prov.getGroupMembership(acct, true);
      }
    } else if (dlSel != null) {
      Key.DistributionListBy by = dlSel.getBy().toKeyDistributionListBy();
      String key = dlSel.getKey();
      DistributionList dl = prov.getDLBasic(by, key);
      GroupHarvestingCheckerUsingCheckRight checker =
          new GroupHarvestingCheckerUsingCheckRight(
              zsc, context, Admin.R_viewDistributionListAdminUI);

      if (dl == null) {
        defendAgainstGroupHarvestingWhenAbsent(by, key, zsc, checker);
      } else {
        defendAgainstGroupHarvesting(dl, by, key, zsc, checker);
        addValues(dl, resp, added, false);
        aclGroups = prov.getGroupMembership(dl, true);
      }
    } else {
      // use the authed account
      addValues(authedAcct, resp, added, false);
      aclGroups = prov.getGroupMembership(authedAcct, true);
    }

    if (aclGroups != null) {
      for (String groupId : aclGroups.groupIds()) {
        DistributionList dl = prov.get(Key.DistributionListBy.id, groupId);
        addValues(dl, resp, added, true);
      }
    }

    return resp;
  }

  private void addValues(NamedEntry entry, Element resp, Set<String> added, boolean inherited) {
    Set<String> values = entry.getMultiAttrSet(Provisioning.A_zimbraAdminConsoleUIComponents);
    for (String value : values) {
      if (!added.contains(value)) {
        resp.addNonUniqueElement(AdminConstants.E_A)
            .setText(value)
            .addAttribute(AdminConstants.A_INHERITED, inherited);
        added.add(value);
      }
    }
  }

  @Override
  public void docRights(List<AdminRight> relatedRights, List<String> notes) {
    relatedRights.add(Admin.R_viewAccountAdminUI);
    relatedRights.add(Admin.R_viewDistributionListAdminUI);

    notes.add("If account/dl is not specified, " + AdminRightCheckPoint.Notes.ALLOW_ALL_ADMINS);
    notes.add(
        "If an account is specified, need the " + Admin.R_viewAccountAdminUI.getName() + " right.");
    notes.add(
        "If a dl is specified, need the "
            + Admin.R_viewDistributionListAdminUI.getName()
            + " right.");
    notes.add(
        "Note, this call does not check for the get attr right for "
            + Provisioning.A_zimbraAdminConsoleUIComponents
            + " attribute on the account/dl, nor "
            + "on the admin groups they belong.  It simply checks the "
            + Admin.R_viewAccountAdminUI.getName()
            + " or "
            + Admin.R_viewDistributionListAdminUI.getName()
            + " right.");
  }
}
