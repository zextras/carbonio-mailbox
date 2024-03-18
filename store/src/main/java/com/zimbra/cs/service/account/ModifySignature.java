// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.service.account;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.zimbra.common.account.Key;
import com.zimbra.common.account.SignatureUtil;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.AccountConstants;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.AccountServiceException;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.Signature;
import com.zimbra.soap.DocumentHandler;
import com.zimbra.common.soap.Element;
import com.zimbra.soap.ZimbraSoapContext;

public class ModifySignature extends DocumentHandler {
    
    public Element handle(Element request, Map<String, Object> context) throws ServiceException {
        ZimbraSoapContext zsc = getZimbraSoapContext(context);
        Account account = getRequestedAccount(zsc);
        
        if (!canModifyOptions(zsc, account))
            throw ServiceException.PERM_DENIED("can not modify options");
        
        Provisioning prov = Provisioning.getInstance();

        Element eSignature = request.getElement(AccountConstants.E_SIGNATURE);
        
        Signature signature = null;
        String id = eSignature.getAttribute(AccountConstants.A_ID);
        signature = prov.get(account, Key.SignatureBy.id, id);
        if (signature == null)
            throw AccountServiceException.NO_SUCH_SIGNATURE(id);

        List<Element> contents = eSignature.listElements(AccountConstants.E_CONTENT);
        Map<String,Object> attrs = new HashMap<>();
        for (Element eContent : contents) {
            String type = eContent.getAttribute(AccountConstants.A_TYPE);
            String attr = SignatureUtil.mimeTypeToAttrName(type);
            if (attr == null)
                throw ServiceException.INVALID_REQUEST("invalid type "+type, null);
            if (attrs.get(attr) != null)
                throw ServiceException.INVALID_REQUEST("only one "+type+" content is allowed", null);
            
            String content = eContent.getText();
            if (content != null)
                attrs.put(attr, content);
        }
        
        String name = eSignature.getAttribute(AccountConstants.A_NAME, null);
        if (name != null)
            attrs.put(Provisioning.A_zimbraSignatureName, name);
        
        Element eContactId = eSignature.getOptionalElement(AccountConstants.E_CONTACT_ID);
        if (eContactId != null)
            attrs.put(Provisioning.A_zimbraPrefMailSignatureContactId, eContactId.getText());
        
        prov.modifySignature(account, signature.getId(), attrs);
        
        Element response = zsc.createElement(AccountConstants.MODIFY_SIGNATURE_RESPONSE);
        return response;
    }
}

