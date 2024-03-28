package com.zimbra.cs.service.mail;

import com.zimbra.cs.account.AuthToken;
import com.zimbra.soap.mail.message.CopyToFilesRequest;
import com.zimbra.soap.mail.message.CopyToFilesResponse;
import io.vavr.control.Try;

public interface FilesCopyHandler {

  Try<CopyToFilesResponse> copy(CopyToFilesRequest copyToFilesRequest, String accountId, AuthToken authToken);
}
