// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.service.account;

import java.util.Map;

import com.zimbra.common.account.Key;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.AccountConstants;
import com.zimbra.common.soap.Element;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.AccountServiceException;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.Signature;
import com.zimbra.soap.DocumentHandler;
import com.zimbra.common.soap.SoapFaultException;
import com.zimbra.soap.ZimbraSoapContext;

public class DeleteSignature extends DocumentHandler {
    
    public Element handle(Element request, Map<String, Object> context) throws ServiceException, SoapFaultException {
        ZimbraSoapContext zsc = getZimbraSoapContext(context);
        Account account = getRequestedAccount(zsc);
        
        if (!canModifyOptions(zsc, account))
            throw ServiceException.PERM_DENIED("can not modify options");
        
        Provisioning prov = Provisioning.getInstance();

        Element eSignature = request.getElement(AccountConstants.E_SIGNATURE);

        // signature can be specified by name or by ID
        Signature signature = null;
        String sigStr = eSignature.getAttribute(AccountConstants.A_ID, null);
        if (sigStr != null) {
            signature = prov.get(account, Key.SignatureBy.id, sigStr);
        } else {
            sigStr = eSignature.getAttribute(AccountConstants.A_NAME);
            signature = prov.get(account, Key.SignatureBy.name, sigStr);
        }
        
        if (signature != null)
            Provisioning.getInstance().deleteSignature(account, signature.getId());
        else
            throw AccountServiceException.NO_SUCH_SIGNATURE(sigStr);

        Element response = zsc.createElement(AccountConstants.DELETE_SIGNATURE_RESPONSE);
        return response;
    }
}