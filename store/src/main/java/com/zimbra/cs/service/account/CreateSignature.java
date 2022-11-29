// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.service.account;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.zimbra.common.account.SignatureUtil;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.AccountConstants;
import com.zimbra.common.soap.Element;
import com.zimbra.common.soap.SoapFaultException;
import com.zimbra.common.util.StringUtil;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.Signature;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.soap.DocumentHandler;

import com.zimbra.soap.ZimbraSoapContext;

public class CreateSignature extends DocumentHandler {
    public Element handle(Element request, Map<String, Object> context) throws ServiceException, SoapFaultException {
        ZimbraSoapContext zsc = getZimbraSoapContext(context);
        Account account = getRequestedAccount(zsc);
        
        if (!canModifyOptions(zsc, account))
            throw ServiceException.PERM_DENIED("can not modify options");
        
        Element eReqSignature = request.getElement(AccountConstants.E_SIGNATURE);
        String name = eReqSignature.getAttribute(AccountConstants.A_NAME);
        String id = eReqSignature.getAttribute(AccountConstants.A_ID, null);
        
        List<Element> contents = eReqSignature.listElements(AccountConstants.E_CONTENT);
        Map<String,Object> attrs = new HashMap<String, Object>();
        for (Element eContent : contents) {
            String type = eContent.getAttribute(AccountConstants.A_TYPE);
            String attr = SignatureUtil.mimeTypeToAttrName(type);
            if (attr == null)
                throw ServiceException.INVALID_REQUEST("invalid type "+type, null);
            if (attrs.get(attr) != null)
                throw ServiceException.INVALID_REQUEST("only one "+type+" content is allowed", null);
            
            String content = eContent.getText();
            if (!StringUtil.isNullOrEmpty(content))
                attrs.put(attr, content);
        }
        
        if (id != null)
            attrs.put(Provisioning.A_zimbraSignatureId, id);
        
        Element eContactId = eReqSignature.getOptionalElement(AccountConstants.E_CONTACT_ID);
        if (eContactId != null)
            attrs.put(Provisioning.A_zimbraPrefMailSignatureContactId, eContactId.getText());
        
        Signature signature = Provisioning.getInstance().createSignature(account, name, attrs);
        
        Element response = zsc.createElement(AccountConstants.CREATE_SIGNATURE_RESPONSE);
        Element eRespSignature = response.addElement(AccountConstants.E_SIGNATURE);
        eRespSignature.addAttribute(AccountConstants.A_ID, signature.getId());
        eRespSignature.addAttribute(AccountConstants.A_NAME, signature.getName());
        return response;
    }
}

