// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.service.admin;

import java.util.List;
import java.util.Map;

import com.zimbra.common.localconfig.LC;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.Element;
import com.zimbra.cs.account.accesscontrol.AdminRight;
import com.zimbra.soap.ZimbraSoapContext;
import com.zimbra.soap.admin.message.GetLicenseInfoResponse;
import com.zimbra.soap.admin.type.LicenseExpirationInfo;

public class GetLicenseInfo extends AdminDocumentHandler {

    static final String TRIAL_EXPIRATION_DATE_KEY = "trial_expiration_date";

    @Override
    public Element handle(Element request, Map<String, Object> context)
    throws ServiceException {
        ZimbraSoapContext lc = getZimbraSoapContext(context);

        String expirationDate = LC.get(TRIAL_EXPIRATION_DATE_KEY);
        LicenseExpirationInfo expirationInfo =
            new LicenseExpirationInfo(expirationDate);
        GetLicenseInfoResponse resp =
            new GetLicenseInfoResponse(expirationInfo);
        return lc.jaxbToElement(resp);
    }

    @Override
    public void docRights(List<AdminRight> relatedRights, List<String> notes) {
        notes.add(AdminRightCheckPoint.Notes.ALLOW_ALL_ADMINS);
    }
}
