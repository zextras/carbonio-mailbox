// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.service.mail;

import com.zimbra.common.mailbox.Color;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.Element;
import com.zimbra.common.soap.MailConstants;
import com.zimbra.cs.mailbox.Flag;
import com.zimbra.cs.mailbox.MailItem;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mailbox.OperationContext;
import com.zimbra.cs.mailbox.SearchFolder;
import com.zimbra.cs.service.util.ItemId;
import com.zimbra.cs.service.util.ItemIdFormatter;
import com.zimbra.soap.ZimbraSoapContext;
import com.zimbra.soap.mail.message.CreateSearchFolderRequest;
import com.zimbra.soap.mail.type.NewSearchFolderSpec;
import java.util.Map;

/**
 * @since May 26, 2004
 */
public class CreateSearchFolder extends MailDocumentHandler {

  private static final String[] TARGET_FOLDER_PATH =
      new String[] {MailConstants.E_SEARCH, MailConstants.A_FOLDER};
  private static final String[] RESPONSE_ITEM_PATH = new String[] {};

  @Override
  protected String[] getProxiedIdPath(Element request) {
    return TARGET_FOLDER_PATH;
  }

  @Override
  protected boolean checkMountpointProxy(Element request) {
    return true;
  }

  @Override
  protected String[] getResponseItemPath() {
    return RESPONSE_ITEM_PATH;
  }

  @Override
  public Element handle(Element request, Map<String, Object> context) throws ServiceException {
    ZimbraSoapContext zsc = getZimbraSoapContext(context);
    Mailbox mbox = getRequestedMailbox(zsc);
    OperationContext octxt = getOperationContext(zsc, context);
    ItemIdFormatter ifmt = new ItemIdFormatter(zsc);

    CreateSearchFolderRequest req = zsc.elementToJaxb(request);
    NewSearchFolderSpec spec = req.getSearchFolder();
    Byte color = spec.getColor() != null ? spec.getColor() : MailItem.DEFAULT_COLOR;
    Color itemColor = spec.getRgb() != null ? new Color(spec.getRgb()) : new Color(color);
    ItemId iidParent = new ItemId(spec.getParentFolderId(), zsc);

    SearchFolder search =
        mbox.createSearchFolder(
            octxt,
            iidParent.getId(),
            spec.getName(),
            spec.getQuery(),
            spec.getSearchTypes(),
            spec.getSortBy(),
            Flag.toBitmask(spec.getFlags()),
            itemColor);

    Element response = zsc.createElement(MailConstants.CREATE_SEARCH_FOLDER_RESPONSE);
    if (search != null) ToXML.encodeSearchFolder(response, ifmt, search);
    return response;
  }
}
