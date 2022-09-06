// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.service.admin;

import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.AdminConstants;
import com.zimbra.common.soap.Element;
import com.zimbra.common.soap.MailConstants;
import com.zimbra.common.util.ByteUtil;
import com.zimbra.cs.account.accesscontrol.AdminRight;
import com.zimbra.cs.mailbox.MailServiceException;
import com.zimbra.cs.service.FileUploadServlet;
import com.zimbra.cs.service.FileUploadServlet.Upload;
import com.zimbra.cs.zimlet.ZimletException;
import com.zimbra.cs.zimlet.ZimletUtil;
import com.zimbra.soap.ZimbraSoapContext;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class ConfigureZimlet extends AdminDocumentHandler {

  @Override
  public Element handle(Element request, Map<String, Object> context) throws ServiceException {

    checkRightTODO();

    ZimbraSoapContext zsc = getZimbraSoapContext(context);
    Element content = request.getElement(MailConstants.E_CONTENT);
    String attachment = content.getAttribute(MailConstants.A_ATTACHMENT_ID, null);
    Upload up =
        FileUploadServlet.fetchUpload(zsc.getAuthtokenAccountId(), attachment, zsc.getAuthToken());
    if (up == null) {
      throw MailServiceException.NO_SUCH_UPLOAD(attachment);
    }

    Element response = zsc.createElement(AdminConstants.CONFIGURE_ZIMLET_RESPONSE);
    try {
      byte[] blob = ByteUtil.getContent(up.getInputStream(), 0);
      ZimletUtil.installConfig(new String(blob));
    } catch (IOException ioe) {
      throw ServiceException.FAILURE("cannot configure", ioe);
    } catch (ZimletException ze) {
      throw ServiceException.FAILURE("cannot configure", ze);
    } finally {
      FileUploadServlet.deleteUpload(up);
    }
    return response;
  }

  @Override
  public void docRights(List<AdminRight> relatedRights, List<String> notes) {
    notes.add(AdminRightCheckPoint.Notes.TODO);

    notes.add(
        "Currently the soap gets a uploaded blob containing metadata. "
            + "The zimlet name is encoded in in the blob and is decoded in ZimletUtil. "
            + "We need a way to know the zimlet name (and cos name if any, currently it "
            + "seems to always only update the default cos) in the SOAP handler in order to "
            + "check right.");
  }
}
