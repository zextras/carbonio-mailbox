package com.zextras.mailbox.smartlinks;

import com.zextras.files.client.GraphQLFilesClient;
import com.zextras.files.client.Token;
import com.zextras.mailbox.AuthenticationInfo;
import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.AuthTokenException;
import com.zimbra.cs.service.mail.FilesCopyHandler;
import com.zimbra.soap.mail.message.CopyToFilesRequest;
import com.zimbra.soap.mail.message.CopyToFilesResponse;
import io.vavr.control.Try;
import java.util.ArrayList;
import java.util.List;

public class FilesSmartLinksGenerator implements SmartLinksGenerator {

  private final GraphQLFilesClient filesClient;
  private final FilesCopyHandler filesCopyHandler;

  public FilesSmartLinksGenerator(GraphQLFilesClient filesClient, FilesCopyHandler filesCopyHandler) {
    this.filesClient = filesClient;
    this.filesCopyHandler = filesCopyHandler;
  }

  @Override
  public List<SmartLink> smartLinksFrom(List<Attachment> attachments,
      AuthenticationInfo authenticationInfo) throws ServiceException {
    List<SmartLink> smartLinks = new ArrayList<>();
    try {
      final Token token = new Token(authenticationInfo.getAuthToken().getEncoded());
      for (var attachment : attachments) {
        String nodeId = uploadToFiles(attachment, authenticationInfo);
        final Try<SmartLink> smartLinkTry = filesClient.createLink(token, nodeId).mapTry(
            createLink -> new SmartLink(createLink.getUrl())
        );
        if (smartLinkTry.isSuccess()) {
          smartLinks.add(smartLinkTry.get());
        } else {
          throw ServiceException.FAILURE("Files CreateLink failed", smartLinkTry.getCause());
        }
      }
      return smartLinks;
    } catch (AuthTokenException e) {
      throw ServiceException.FAILURE(e.getMessage());
    }

  }

  private String uploadToFiles(Attachment attachment, AuthenticationInfo authenticationInfo)
      throws ServiceException {
    CopyToFilesRequest request = new CopyToFilesRequest();
    request.setDestinationFolderId("LOCAL_ROOT");
    request.setMessageId(attachment.getDraftId());
    request.setPart(attachment.getPartName());
    Try<CopyToFilesResponse> resp = filesCopyHandler.copy(request, authenticationInfo.getAuthenticatedAccount().getId(), authenticationInfo.getAuthToken());
    return resp.map(CopyToFilesResponse::getNodeId)
        .getOrElseThrow( (Throwable e) -> {
          if (e instanceof ServiceException) return (ServiceException)e;
          else return ServiceException.FAILURE(e.getMessage());
        });
  }
}
