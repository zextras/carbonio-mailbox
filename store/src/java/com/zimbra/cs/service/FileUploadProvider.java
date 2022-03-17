package com.zimbra.cs.service;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.AuthToken;
import com.zimbra.cs.service.FileUploadServlet.Upload;

/**
 * Interface to allow definition of upload content provider (get).
 */
@FunctionalInterface
public interface FileUploadProvider {

  /**
   * Returns a {@link FileUploadServlet.Upload}.
   *
   * @param accountId id of the account who is requesting the upload
   * @param uploadId id of the requested upload
   * @param authToken token to check request validity
   * @return an uploaded content
   */
  Upload getUpload(String accountId, String uploadId, AuthToken authToken) throws ServiceException;
}
