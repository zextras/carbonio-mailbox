// SPDX-FileCopyrightText: 2024 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: AGPL-3.0-only

package com.zimbra.cs.service.mail;

import com.zextras.carbonio.files.FilesClient;
import com.zimbra.common.auth.ZAuthToken;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.Element;
import com.zimbra.common.soap.SoapHttpTransport;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.httpclient.URLUtil;
import com.zimbra.soap.DocumentHandler;
import com.zimbra.soap.JaxbUtil;
import com.zimbra.soap.ZimbraSoapContext;
import com.zimbra.soap.mail.message.CopyToFilesRequest;
import com.zimbra.soap.mail.message.CopyToFilesResponse;
import com.zimbra.soap.mail.message.SendMsgResponse;
import com.zimbra.soap.mail.message.SendMsgWithSmartLinksRequest;
import com.zimbra.soap.mail.type.MsgToSend;
import com.zimbra.soap.mail.type.SmartLink;
import java.io.IOException;
import java.util.Map;

public class SendMsgWithSmartLinks extends DocumentHandler {

  private final FilesClient filesClient;

  public SendMsgWithSmartLinks(FilesClient filesClient) {
    this.filesClient = filesClient;
  }



  @Override
  public Element handle(Element request, Map<String, Object> context)
      throws ServiceException {
    SendMsgWithSmartLinksRequest req = JaxbUtil.elementToJaxb(request, SendMsgWithSmartLinksRequest.class);
    ZimbraSoapContext zsc = getZimbraSoapContext(context);
    final Account authenticatedAccount = getAuthenticatedAccount(zsc);
    final Account requestedAccount = getRequestedAccount(zsc);
    final ZAuthToken zAuthToken = zsc.getAuthToken().toZAuthToken();
    // TODO: add smart links to body and remove attachments
    for (var sl : req.getSmartLinks()) {
      // saveToFiles
      // getPublicLink
      String nodeId = uploadToFiles(authenticatedAccount, requestedAccount, sl, zAuthToken);
    }

    SendMsgResponse sendMsgResponse = this.sendMsg(authenticatedAccount, requestedAccount, req.getMsg(), zAuthToken);
    // TODO: use SendMsgWithLinksResponse
    return JaxbUtil.jaxbToElement(sendMsgResponse);
  }

  private SendMsgResponse sendMsg(Account authenticatedAccount, Account requestedAccount, MsgToSend msgToSend, ZAuthToken zAuthToken) {

    return null;
  }

  private String uploadToFiles(Account authenticatedAccount, Account requestedAccount, SmartLink smartLink, ZAuthToken zAuthToken)
      throws ServiceException {
    String soapUrl = URLUtil.getSoapURL(authenticatedAccount.getServer(), true);
    CopyToFilesRequest request = new CopyToFilesRequest();
    request.setDestinationFolderId("LOCAL_ROOT");
    request.setMessageId(smartLink.getDraftId());
    request.setPart(smartLink.getPartName());
    final Element autocompleteRequestElement = JaxbUtil.jaxbToElement(request);
    try {
      CopyToFilesResponse copyToFilesResponse = JaxbUtil.elementToJaxb(new SoapHttpTransport(zAuthToken, soapUrl)
          .invoke(autocompleteRequestElement, requestedAccount.getId()), CopyToFilesResponse.class);
      return copyToFilesResponse.getNodeId();
    } catch (IOException e) {
      throw ServiceException.FAILURE(e.getMessage());
    }
  }
}