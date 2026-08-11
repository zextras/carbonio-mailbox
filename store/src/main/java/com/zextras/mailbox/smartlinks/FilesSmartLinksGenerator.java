package com.zextras.mailbox.smartlinks;

import com.zextras.carbonio.files.sdk.FilesInternalClient;
import com.zextras.mailbox.AuthenticationInfo;
import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.service.mail.FilesCopyHandler;
import com.zimbra.soap.mail.message.CopyToFilesRequest;
import com.zimbra.soap.mail.message.CopyToFilesResponse;
import io.vavr.control.Try;
import java.util.ArrayList;
import java.util.List;

public class FilesSmartLinksGenerator implements SmartLinksGenerator {

  private final FilesInternalClient filesInternalClient;
  private final FilesCopyHandler filesCopyHandler;

  public FilesSmartLinksGenerator(FilesInternalClient filesInternalClient, FilesCopyHandler filesCopyHandler) {
    this.filesInternalClient = filesInternalClient;
    this.filesCopyHandler = filesCopyHandler;
  }

  @Override
  public List<SmartLink> smartLinksFrom(List<Attachment> attachments,
      AuthenticationInfo authenticationInfo) throws ServiceException {
    List<SmartLink> smartLinks = new ArrayList<>();
    final String userId = authenticationInfo.getAuthenticatedAccount().getId();
    for (var attachment : attachments) {
      String nodeId = uploadToFiles(attachment, authenticationInfo);
      try {
        String publicUrl = filesInternalClient.createPublicLink(userId, nodeId);
        smartLinks.add(new SmartLink(publicUrl));
      } catch (RuntimeException e) {
        throw ServiceException.FAILURE("Files CreateLink failed", e);
      }
    }
    return smartLinks;
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
