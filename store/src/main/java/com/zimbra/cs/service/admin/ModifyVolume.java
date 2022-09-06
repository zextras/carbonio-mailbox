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
import com.zimbra.cs.store.StoreManager;
import com.zimbra.cs.volume.Volume;
import com.zimbra.cs.volume.VolumeManager;
import com.zimbra.soap.ZimbraSoapContext;
import com.zimbra.soap.admin.message.ModifyVolumeRequest;
import com.zimbra.soap.admin.message.ModifyVolumeResponse;
import com.zimbra.soap.admin.type.VolumeInfo;
import java.util.List;
import java.util.Map;

public final class ModifyVolume extends AdminDocumentHandler {

  @Override
  public Element handle(Element req, Map<String, Object> ctx) throws ServiceException {
    ZimbraSoapContext zsc = getZimbraSoapContext(ctx);
    return zsc.jaxbToElement(handle((ModifyVolumeRequest) zsc.elementToJaxb(req), ctx));
  }

  private ModifyVolumeResponse handle(ModifyVolumeRequest req, Map<String, Object> ctx)
      throws ServiceException {
    ZimbraSoapContext zsc = getZimbraSoapContext(ctx);
    checkRight(zsc, ctx, Provisioning.getInstance().getLocalServer(), Admin.R_manageVolume);

    VolumeManager mgr = VolumeManager.getInstance();
    Volume.Builder builder = Volume.builder(mgr.getVolume(req.getId()));
    VolumeInfo vol = req.getVolume();
    if (vol == null) {
      throw ServiceException.INVALID_REQUEST("must specify a volume Element", null);
    }
    StoreManager storeManager = StoreManager.getInstance();
    if (storeManager.supports(
        StoreManager.StoreFeature.CUSTOM_STORE_API, String.valueOf(vol.getId()))) {
      throw ServiceException.INVALID_REQUEST(
          "Operation unsupported, use zxsuite to edit this volume", null);
    }
    if (vol.getType() > 0) {
      builder.setType(vol.getType());
    }
    if (vol.getName() != null) {
      builder.setName(vol.getName());
    }
    if (vol.getRootPath() != null) {
      builder.setPath(vol.getRootPath(), true);
    }
    if (vol.getCompressBlobs() != null) {
      builder.setCompressBlobs(vol.getCompressBlobs());
    }
    if (vol.getCompressionThreshold() > 0) {
      builder.setCompressionThreshold(vol.getCompressionThreshold());
    }
    mgr.update(builder.build());
    return new ModifyVolumeResponse();
  }

  @Override
  public void docRights(List<AdminRight> relatedRights, List<String> notes) {
    relatedRights.add(Admin.R_manageVolume);
  }
}
