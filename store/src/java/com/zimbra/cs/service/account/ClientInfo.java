// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.service.account;

import java.util.Map;

import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.AccountConstants;
import com.zimbra.common.soap.Element;
import com.zimbra.cs.account.Domain;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.soap.JaxbUtil;
import com.zimbra.soap.ZimbraSoapContext;
import com.zimbra.soap.account.message.ClientInfoRequest;


/**
 * ClientInfo returns domain attributes that may interest a client.
 */
public class ClientInfo extends AccountDocumentHandler {

    @Override
    public Element handle(Element request, Map<String, Object> context) throws ServiceException {
        ZimbraSoapContext zsc = getZimbraSoapContext(context);
        ClientInfoRequest req = JaxbUtil.elementToJaxb(request);

        Domain domain = Provisioning.getInstance().get(req.getDomain());

        Element parent =  zsc.createElement(AccountConstants.CLIENT_INFO_RESPONSE);
        if (domain != null) {
            ToXML.encodeAttr(parent, Provisioning.A_zimbraFeatureResetPasswordStatus, domain.getFeatureResetPasswordStatusAsString());
            ToXML.encodeAttr(parent, Provisioning.A_zimbraSkinBackgroundColor, domain.getSkinBackgroundColor());
            ToXML.encodeAttr(parent, Provisioning.A_zimbraSkinFavicon, domain.getSkinFavicon());
            ToXML.encodeAttr(parent, Provisioning.A_zimbraSkinForegroundColor, domain.getSkinForegroundColor());
            ToXML.encodeAttr(parent, Provisioning.A_zimbraSkinLogoAppBanner, domain.getSkinLogoAppBanner());
            ToXML.encodeAttr(parent, Provisioning.A_zimbraSkinLogoLoginBanner, domain.getSkinLogoLoginBanner());
            ToXML.encodeAttr(parent, Provisioning.A_zimbraSkinLogoURL, domain.getSkinLogoURL());
            ToXML.encodeAttr(parent, Provisioning.A_zimbraSkinSecondaryColor, domain.getSkinSecondaryColor());
            ToXML.encodeAttr(parent, Provisioning.A_zimbraSkinSelectionColor, domain.getSkinSelectionColor());
            ToXML.encodeAttr(parent, Provisioning.A_zimbraWebClientLoginURL, domain.getWebClientLoginURL());
            ToXML.encodeAttr(parent, Provisioning.A_zimbraWebClientLogoutURL, domain.getWebClientLogoutURL());
            ToXML.encodeAttr(parent, Provisioning.A_zimbraWebClientStaySignedInDisabled, String.valueOf(domain.isWebClientStaySignedInDisabled()));
        }
        return parent;
    }

    @Override
    public boolean needsAuth(Map<String, Object> context) {
        return false;
    }

    @Override
    public boolean needsAdminAuth(Map<String, Object> context) {
        return false;
    }

}
