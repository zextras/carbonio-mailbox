// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.service.admin;

import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.AdminConstants;
import com.zimbra.common.soap.Element;
import com.zimbra.common.util.Pair;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.accesscontrol.AdminRight;
import com.zimbra.cs.account.accesscontrol.GranteeType;
import com.zimbra.cs.account.accesscontrol.RightCommand;
import com.zimbra.cs.account.accesscontrol.Rights.Admin;
import com.zimbra.cs.account.accesscontrol.TargetType;
import com.zimbra.soap.ZimbraSoapContext;
import com.zimbra.soap.admin.type.GranteeSelector.GranteeBy;
import com.zimbra.soap.type.TargetBy;
import java.util.List;
import java.util.Map;

public class GetEffectiveRights extends RightDocumentHandler {

  @Override
  public Element handle(Element request, Map<String, Object> context) throws ServiceException {
    ZimbraSoapContext zsc = getZimbraSoapContext(context);

    Pair<Boolean, Boolean> expandAttrs = parseExpandAttrs(request);
    boolean expandSetAttrs = expandAttrs.getFirst();
    boolean expandGetAttrs = expandAttrs.getSecond();

    Element eTarget = request.getElement(AdminConstants.E_TARGET);
    String targetType = eTarget.getAttribute(AdminConstants.A_TYPE);
    TargetBy targetBy = null;
    String target = null;
    if (TargetType.fromCode(targetType).needsTargetIdentity()) {
      targetBy = TargetBy.fromString(eTarget.getAttribute(AdminConstants.A_BY));
      target = eTarget.getText();
    }

    Element eGrantee = request.getOptionalElement(AdminConstants.E_GRANTEE);
    GranteeBy granteeBy;
    String grantee;
    if (eGrantee != null) {
      String granteeType =
          eGrantee.getAttribute(AdminConstants.A_TYPE, GranteeType.GT_USER.getCode());
      if (GranteeType.fromCode(granteeType) != GranteeType.GT_USER)
        throw ServiceException.INVALID_REQUEST("invalid grantee type " + granteeType, null);
      granteeBy = GranteeBy.fromString(eGrantee.getAttribute(AdminConstants.A_BY));
      grantee = eGrantee.getText();
    } else {
      granteeBy = GranteeBy.id;
      grantee = zsc.getRequestedAccountId();
    }

    if (!grantee.equals(zsc.getAuthtokenAccountId())) {
      checkCheckRightRight(zsc, GranteeType.GT_USER, granteeBy, grantee);
    }

    RightCommand.EffectiveRights er =
        RightCommand.getEffectiveRights(
            Provisioning.getInstance(),
            targetType,
            targetBy,
            target,
            granteeBy,
            grantee,
            expandSetAttrs,
            expandGetAttrs);

    Element resp = zsc.createElement(AdminConstants.GET_EFFECTIVE_RIGHTS_RESPONSE);
    er.toXML_getEffectiveRights(resp);
    return resp;
  }

  @Override
  public void docRights(List<AdminRight> relatedRights, List<String> notes) {
    relatedRights.add(Admin.R_checkRightUsr);
  }
}
