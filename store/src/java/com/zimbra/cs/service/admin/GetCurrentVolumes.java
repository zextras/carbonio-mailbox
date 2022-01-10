// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.service.admin;

import java.util.List;
import java.util.Map;

import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.accesscontrol.AdminRight;
import com.zimbra.cs.account.accesscontrol.Rights.Admin;
import com.zimbra.cs.volume.Volume;
import com.zimbra.cs.volume.VolumeManager;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.Element;
import com.zimbra.soap.JaxbUtil;
import com.zimbra.soap.ZimbraSoapContext;
import com.zimbra.soap.admin.message.GetCurrentVolumesRequest;
import com.zimbra.soap.admin.message.GetCurrentVolumesResponse;

public final class GetCurrentVolumes extends AdminDocumentHandler {

    @Override
    public Element handle(Element req, Map<String, Object> ctx) throws ServiceException {
        ZimbraSoapContext zsc = getZimbraSoapContext(ctx);
        return zsc.jaxbToElement(handle((GetCurrentVolumesRequest) zsc.elementToJaxb(req), ctx));
    }

    private GetCurrentVolumesResponse handle(@SuppressWarnings("unused") GetCurrentVolumesRequest req,
            Map<String, Object> ctx) throws ServiceException {
        ZimbraSoapContext zsc = getZimbraSoapContext(ctx);
        checkRight(zsc, ctx, Provisioning.getInstance().getLocalServer(), Admin.R_manageVolume);

        GetCurrentVolumesResponse resp = new GetCurrentVolumesResponse();
        VolumeManager mgr = VolumeManager.getInstance();
        Volume msgVol = mgr.getCurrentMessageVolume();
        if (msgVol != null) {
            resp.addVolume(new GetCurrentVolumesResponse.CurrentVolumeInfo(msgVol.getId(), msgVol.getType()));
        }
        Volume secondaryMsgVol = mgr.getCurrentSecondaryMessageVolume();
        if (secondaryMsgVol != null) {
            resp.addVolume(new GetCurrentVolumesResponse.CurrentVolumeInfo(
                    secondaryMsgVol.getId(), secondaryMsgVol.getType()));
        }
        Volume indexVol = mgr.getCurrentIndexVolume();
        if (indexVol != null) {
            resp.addVolume(new GetCurrentVolumesResponse.CurrentVolumeInfo(indexVol.getId(), indexVol.getType()));
        }
        return resp;
    }

    @Override
    public void docRights(List<AdminRight> relatedRights, List<String> notes) {
        relatedRights.add(Admin.R_manageVolume);
    }

}
