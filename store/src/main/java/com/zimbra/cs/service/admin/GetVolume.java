// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.service.admin;

import java.util.List;
import java.util.Map;

import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.Element;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.accesscontrol.AdminRight;
import com.zimbra.cs.account.accesscontrol.Rights.Admin;
import com.zimbra.cs.volume.Volume;
import com.zimbra.cs.volume.VolumeManager;
import com.zimbra.soap.JaxbUtil;
import com.zimbra.soap.ZimbraSoapContext;
import com.zimbra.soap.admin.message.GetVolumeRequest;
import com.zimbra.soap.admin.message.GetVolumeResponse;

public final class GetVolume extends AdminDocumentHandler {

    @Override
    public Element handle(Element req, Map<String, Object> ctx) throws ServiceException {
        ZimbraSoapContext zsc = getZimbraSoapContext(ctx);
        return zsc.jaxbToElement(handle((GetVolumeRequest) zsc.elementToJaxb(req), ctx));
    }

    private GetVolumeResponse handle(GetVolumeRequest req, Map<String, Object> ctx) throws ServiceException {
        ZimbraSoapContext zsc = getZimbraSoapContext(ctx);
        checkRight(zsc, ctx, Provisioning.getInstance().getLocalServer(), Admin.R_manageVolume);

        Volume vol = VolumeManager.getInstance().getVolume(req.getId());
        GetVolumeResponse resp = new GetVolumeResponse(vol.toJAXB());
        return resp;
    }

    @Override
    public void docRights(List<AdminRight> relatedRights, List<String> notes) {
        relatedRights.add(Admin.R_manageVolume);
    }

}
