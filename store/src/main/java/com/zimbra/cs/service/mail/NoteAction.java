// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.service.mail;

import com.google.common.base.Joiner;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.Element;
import com.zimbra.common.soap.MailConstants;
import com.zimbra.common.soap.SoapFaultException;
import com.zimbra.cs.mailbox.MailItem;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mailbox.Note.Rectangle;
import com.zimbra.cs.mailbox.OperationContext;
import com.zimbra.cs.service.util.ItemId;
import com.zimbra.cs.service.util.ItemIdFormatter;
import com.zimbra.soap.ZimbraSoapContext;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @since Sep 8, 2004
 * @author dkarp
 */
public class NoteAction extends ItemAction {

  @Override
  protected String[] getProxiedIdPath(Element request) {
    String operation = getXPath(request, OPERATION_PATH);
    if (operation != null && NOTE_OPS.contains(operation.toLowerCase())) return TARGET_ITEM_PATH;
    return super.getProxiedIdPath(request);
  }

  public static final String OP_EDIT = "edit";
  public static final String OP_REPOSITION = "pos";

  private static final Set<String> NOTE_OPS =
      new HashSet<String>(Arrays.asList(new String[] {OP_EDIT, OP_REPOSITION}));

  @Override
  public Element handle(Element request, Map<String, Object> context)
      throws ServiceException, SoapFaultException {
    ZimbraSoapContext lc = getZimbraSoapContext(context);

    Element action = request.getElement(MailConstants.E_ACTION);
    String operation = action.getAttribute(MailConstants.A_OPERATION).toLowerCase();

    if (operation.endsWith(OP_READ) || operation.endsWith(OP_SPAM))
      throw ServiceException.INVALID_REQUEST("invalid operation on note: " + operation, null);
    String successes;
    if (NOTE_OPS.contains(operation)) {
      successes = handleNote(context, request, operation);
    } else {
      successes =
          Joiner.on(",").join(handleCommon(context, request, MailItem.Type.NOTE).getSuccessIds());
    }
    Element response = lc.createElement(MailConstants.NOTE_ACTION_RESPONSE);
    Element act = response.addUniqueElement(MailConstants.E_ACTION);
    act.addAttribute(MailConstants.A_ID, successes);
    act.addAttribute(MailConstants.A_OPERATION, operation);
    return response;
  }

  private String handleNote(Map<String, Object> context, Element request, String operation)
      throws ServiceException {
    Element action = request.getElement(MailConstants.E_ACTION);

    ZimbraSoapContext zsc = getZimbraSoapContext(context);
    Mailbox mbox = getRequestedMailbox(zsc);
    OperationContext octxt = getOperationContext(zsc, context);
    ItemId iid = new ItemId(action.getAttribute(MailConstants.A_ID), zsc);

    if (operation.equals(OP_EDIT)) {
      String content = action.getAttribute(MailConstants.E_CONTENT);
      mbox.editNote(octxt, iid.getId(), content);
    } else if (operation.equals(OP_REPOSITION)) {
      String strBounds = action.getAttribute(MailConstants.A_BOUNDS, null);
      mbox.repositionNote(octxt, iid.getId(), new Rectangle(strBounds));
    } else throw ServiceException.INVALID_REQUEST("unknown operation: " + operation, null);

    return new ItemIdFormatter(zsc).formatItemId(iid);
  }
}
