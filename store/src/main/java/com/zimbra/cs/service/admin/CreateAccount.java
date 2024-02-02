// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.service.admin;

import static com.zimbra.common.account.ZAttrProvisioning.A_zimbraId;

import java.util.List;
import java.util.Map;

import com.zimbra.common.account.Key;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.AdminConstants;
import com.zimbra.common.soap.Element;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.AccountServiceException;
import com.zimbra.cs.account.Cos;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.accesscontrol.AdminRight;
import com.zimbra.cs.account.accesscontrol.Rights.Admin;
import com.zimbra.cs.account.accesscontrol.TargetType;
import com.zimbra.cs.listeners.AccountListener;
import com.zimbra.soap.ZimbraSoapContext;
import com.zimbra.soap.admin.message.CreateAccountRequest;

public class CreateAccount extends AdminDocumentHandler {

    /**
     * must be careful and only create accounts for the domain admin!
     */
    @Override
    public boolean domainAuthSufficient(Map context) {
        return true;
    }

    /**
     * @return true - which means accept responsibility for measures to prevent account harvesting by delegate admins
     */
    @Override
    public boolean defendsAgainstDelegateAdminAccountHarvesting() {
        return true;
    }

    @Override
    public Element handle(Element request, Map<String, Object> context) throws ServiceException {
        ZimbraSoapContext zsc = getZimbraSoapContext(context);
        Provisioning prov = Provisioning.getInstance();
        Account account = null;
        try {
        CreateAccountRequest req = zsc.elementToJaxb(request);

        String name = req.getName().toLowerCase();
        Map<String, Object> attrs = req.getAttrsAsOldMultimap(true /* ignoreEmptyValues */);

        checkDomainRightByEmail(zsc, name, Admin.R_createAccount);
        checkSetAttrsOnCreate(zsc, TargetType.account, name, attrs);
        checkCos(zsc, attrs);

        account = prov.createAccount(name, req.getPassword(), attrs);

        ZimbraLog.security.info(ZimbraLog.encodeAttrs( new String[] {"cmd", "CreateAccount","name", name}, attrs));
        } catch (ServiceException e) {
            AccountListener.invokeOnException(e);
            throw e;
        }

        Element response = zsc.createElement(AdminConstants.CREATE_ACCOUNT_RESPONSE);
        ToXML.encodeAccount(response, account);
        AccountListener.invokeOnAccountCreation(account);
        return response;
    }

    private void checkCos(ZimbraSoapContext zsc, Map<String, Object> attrs) throws ServiceException {
        String cosId = ModifyAccount.getStringAttrNewValue(Provisioning.A_zimbraCOSId, attrs);
        if (cosId == null) {
            return;  // not setting it
        }

        Provisioning prov = Provisioning.getInstance();

        Cos cos = prov.get(Key.CosBy.id, cosId);
        if (cos == null) {
            throw AccountServiceException.NO_SUCH_COS(cosId);
        }

        // call checkRight instead of checkCosRight, because:
        // 1. no domain based access manager backward compatibility issue
        // 2. we only want to check right if we are using pure ACL based access manager.
        checkRight(zsc, cos, Admin.R_assignCos);
    }

    @Override
    public void docRights(List<AdminRight> relatedRights, List<String> notes) {
        relatedRights.add(Admin.R_createAccount);

        notes.add(String.format(AdminRightCheckPoint.Notes.MODIFY_ENTRY,
                Admin.R_modifyAccount.getName(), "account"));

        notes.add("Notes on " + Provisioning.A_zimbraCOSId + ": " +
                "If setting " + Provisioning.A_zimbraCOSId + ", needs the " + Admin.R_assignCos.getName() +
                " right on the cos.");
    }
}
