package com.zextras.mailbox.smartlinks;

import com.zextras.carbonio.files.FilesClient;
import com.zextras.mailbox.AuthenticationInfo;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.util.ZimbraCookie;
import com.zimbra.cs.account.AuthToken;
import com.zimbra.cs.account.AuthTokenException;
import com.zimbra.cs.service.mail.FilesCopyHandler;
import com.zimbra.soap.mail.message.CopyToFilesRequest;
import com.zimbra.soap.mail.message.CopyToFilesResponse;
import io.vavr.control.Try;
import java.util.ArrayList;
import java.util.List;

public class FilesSmartLinksGenerator implements SmartLinksGenerator {

  private final FilesClient filesClient;
  private final FilesCopyHandler filesCopyHandler;

  public FilesSmartLinksGenerator(FilesClient filesClient, FilesCopyHandler filesCopyHandler) {
    this.filesClient = filesClient;
    this.filesCopyHandler = filesCopyHandler;
  }

  @Override
  public List<SmartLink> smartLinksFrom(List<Attachment> attachments,
      AuthenticationInfo authenticationInfo) throws ServiceException {
    List<SmartLink> smartLinks = new ArrayList<>();
    try {
      final String cookie = getZmCookie(authenticationInfo.getAuthToken());
      for (var attachment : attachments) {
        String nodeId = uploadToFiles(attachment, authenticationInfo);
        final Try<SmartLink> smartLinkTry = filesClient.createLink(cookie, nodeId).mapTry(
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

  private String getZmCookie(AuthToken authToken) throws AuthTokenException {
      return ZimbraCookie.COOKIE_ZM_AUTH_TOKEN + "=" + authToken.getEncoded();
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
