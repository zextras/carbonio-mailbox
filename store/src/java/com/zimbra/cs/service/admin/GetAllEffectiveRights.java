// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.service.admin;

import java.util.List;
import java.util.Map;

import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.AdminConstants;
import com.zimbra.common.soap.Element;
import com.zimbra.common.util.Pair;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.accesscontrol.AdminRight;
import com.zimbra.cs.account.accesscontrol.GranteeType;
import com.zimbra.cs.account.accesscontrol.RightCommand;
import com.zimbra.cs.account.accesscontrol.Rights.Admin;
import com.zimbra.soap.ZimbraSoapContext;
import com.zimbra.soap.admin.type.GranteeSelector.GranteeBy;

public class GetAllEffectiveRights extends RightDocumentHandler {

    @Override
    public Element handle(Element request, Map<String, Object> context) throws ServiceException {
        ZimbraSoapContext zsc = getZimbraSoapContext(context);

        Pair<Boolean, Boolean> expandAttrs = parseExpandAttrs(request);
        boolean expandSetAttrs = expandAttrs.getFirst();
        boolean expandGetAttrs = expandAttrs.getSecond();

        Element eGrantee = request.getOptionalElement(AdminConstants.E_GRANTEE);
        String granteeType;
        GranteeBy granteeBy;
        String grantee;
        if (eGrantee != null) {
            granteeType = eGrantee.getAttribute(AdminConstants.A_TYPE, GranteeType.GT_USER.getCode());
            granteeBy = GranteeBy.fromString(eGrantee.getAttribute(AdminConstants.A_BY));
            grantee = eGrantee.getText();
        } else {
            granteeType = GranteeType.GT_USER.getCode();
            granteeBy = GranteeBy.id;
            grantee = zsc.getRequestedAccountId();
        }

        GranteeType gt = GranteeType.fromCode(granteeType);
        if (!grantee.equals(zsc.getAuthtokenAccountId())) {
            checkCheckRightRight(zsc, gt, granteeBy, grantee);
        }

        RightCommand.AllEffectiveRights aer = RightCommand.getAllEffectiveRights(
                Provisioning.getInstance(),
                granteeType, granteeBy, grantee,
                expandSetAttrs, expandGetAttrs);

        Element resp = zsc.createElement(AdminConstants.GET_ALL_EFFECTIVE_RIGHTS_RESPONSE);
        aer.toXML(resp);
        return resp;
    }

    @Override
    public void docRights(List<AdminRight> relatedRights, List<String> notes) {
        relatedRights.add(Admin.R_checkRightUsr);
        relatedRights.add(Admin.R_checkRightGrp);

        notes.add("If grantee to check for is an account, needs the " + Admin.R_checkRightUsr.getName() + " right");
        notes.add("If grantee to check for is a group, needs the " + Admin.R_checkRightGrp.getName() + " right");
    }
}
