// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.service.admin;

import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.AdminConstants;
import com.zimbra.common.soap.Element;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.Server;
import com.zimbra.cs.account.accesscontrol.AdminRight;
import com.zimbra.cs.account.accesscontrol.Rights.Admin;
import com.zimbra.soap.ZimbraSoapContext;
import java.io.File;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class CheckDirectory extends AdminDocumentHandler {

  @Override
  public Element handle(Element request, Map<String, Object> context) throws ServiceException {

    ZimbraSoapContext lc = getZimbraSoapContext(context);

    Server localServer = Provisioning.getInstance().getLocalServer();
    checkRight(lc, context, localServer, Admin.R_checkDirectoryOnFileSystem);

    Element response = lc.createElement(AdminConstants.CHECK_DIRECTORY_RESPONSE);

    for (Iterator<Element> iter = request.elementIterator(AdminConstants.E_DIRECTORY);
        iter.hasNext(); ) {
      Element dirReq = iter.next();
      String path = dirReq.getAttribute(AdminConstants.A_PATH);
      boolean create = dirReq.getAttributeBool(AdminConstants.A_CREATE, false);
      File dir = new File(path);
      boolean exists = dir.exists();
      if (!exists && create) {
        dir.mkdirs();
        exists = dir.exists();
      }
      boolean isDirectory = false;
      boolean readable = false;
      boolean writable = false;
      if (exists) {
        isDirectory = dir.isDirectory();
        readable = dir.canRead();
        writable = dir.canWrite();
      }

      Element dirResp = response.addElement(AdminConstants.E_DIRECTORY);
      dirResp.addAttribute(AdminConstants.A_PATH, path);
      dirResp.addAttribute(AdminConstants.A_EXISTS, exists);
      dirResp.addAttribute(AdminConstants.A_IS_DIRECTORY, isDirectory);
      dirResp.addAttribute(AdminConstants.A_READABLE, readable);
      dirResp.addAttribute(AdminConstants.A_WRITABLE, writable);
    }

    return response;
  }

  @Override
  public void docRights(List<AdminRight> relatedRights, List<String> notes) {
    relatedRights.add(Admin.R_checkDirectoryOnFileSystem);
  }
}
