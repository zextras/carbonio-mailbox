// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.service.admin;

import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.Element;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.accesscontrol.AdminRight;
import com.zimbra.cs.account.accesscontrol.Rights.Admin;
import com.zimbra.cs.volume.Volume;
import com.zimbra.cs.volume.VolumeManager;
import com.zimbra.soap.ZimbraSoapContext;
import com.zimbra.soap.admin.message.CreateVolumeRequest;
import com.zimbra.soap.admin.message.CreateVolumeResponse;
import com.zimbra.soap.admin.type.VolumeInfo;
import java.util.List;
import java.util.Map;

public final class CreateVolume extends AdminDocumentHandler {

  @Override
  public Element handle(Element req, Map<String, Object> ctx) throws ServiceException {
    ZimbraSoapContext zsc = getZimbraSoapContext(ctx);
    return zsc.jaxbToElement(handle((CreateVolumeRequest) zsc.elementToJaxb(req), ctx));
  }

  private CreateVolumeResponse handle(CreateVolumeRequest req, Map<String, Object> ctx)
      throws ServiceException {
    ZimbraSoapContext zsc = getZimbraSoapContext(ctx);
    checkRight(zsc, ctx, Provisioning.getInstance().getLocalServer(), Admin.R_manageVolume);

    Volume vol = VolumeManager.getInstance().create(toVolume(req.getVolume()));
    return new CreateVolumeResponse(vol.toJAXB());
  }

  private Volume toVolume(VolumeInfo vol) throws ServiceException {
    return Volume.builder()
        .setType(vol.getType())
        .setName(vol.getName())
        .setPath(vol.getRootPath(), true)
        .setCompressBlobs(vol.isCompressBlobs())
        .setCompressionThreshold(vol.getCompressionThreshold())
        .build();
  }

  @Override
  public void docRights(List<AdminRight> relatedRights, List<String> notes) {
    relatedRights.add(Admin.R_manageVolume);
  }
}
