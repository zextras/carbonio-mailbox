package com.zextras.mailbox.smartlinks;

import com.zextras.files.client.GraphQLFilesClient;
import com.zextras.mailbox.AuthenticationInfo;
import com.zimbra.common.auth.ZAuthToken;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.Element;
import com.zimbra.common.soap.SoapHttpTransport;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.AuthToken;
import com.zimbra.cs.httpclient.URLUtil;
import com.zimbra.soap.JaxbUtil;
import com.zimbra.soap.mail.message.CopyToFilesRequest;
import com.zimbra.soap.mail.message.CopyToFilesResponse;

import java.io.IOException;
import java.util.List;

import static java.util.Collections.emptyList;

public class FilesSmartLinksGenerator implements SmartLinksGenerator {
  private final GraphQLFilesClient filesClient;

  public FilesSmartLinksGenerator(GraphQLFilesClient filesClient) {
    this.filesClient = filesClient;
  }

  @Override
  public List<SmartLink> smartLinksFrom(List<Attachment> attachments, AuthenticationInfo authenticationInfo) throws ServiceException {
    for (var attachment : attachments) {
      String nodeId = uploadToFiles(attachment, authenticationInfo);
      // set public link and get it
    }
    return emptyList();
  }

  private String uploadToFiles(Attachment attachment, AuthenticationInfo authenticationInfo) throws ServiceException {
    Account authenticatedAccount = authenticationInfo.getAuthenticatedAccount();
    Account requestedAccount = authenticationInfo.getRequestedAccount();
    ZAuthToken zAuthToken = authenticationInfo.getAuthToken().toZAuthToken();
    String soapUrl = URLUtil.getSoapURL(authenticatedAccount.getServer(), true);
    CopyToFilesRequest request = new CopyToFilesRequest();
    request.setDestinationFolderId("LOCAL_ROOT");
    request.setMessageId(attachment.getDraftId());
    request.setPart(attachment.getPartName());
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
