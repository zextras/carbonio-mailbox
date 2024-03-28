// SPDX-FileCopyrightText: 2024 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: AGPL-3.0-only

package com.zimbra.cs.service.mail.message.parser;

import com.zimbra.cs.service.FileUploadServlet.Upload;
import java.util.ArrayList;
import java.util.List;

/**
 * Wrapper class for data parsed out of the mime message
 */
public class MimeMessageData {

  public List<Upload> fetches; // NULL unless we fetched messages from another server
  public List<Upload> uploads; // NULL unless there are uploaded attachments
  public String iCalUUID; // NULL unless there is an iCal part

  void addUpload(Upload up) {
    if (uploads == null) {
      uploads = new ArrayList<Upload>(4);
    }
    uploads.add(up);
  }

  void addFetch(Upload up) {
    if (fetches == null) {
      fetches = new ArrayList<Upload>(4);
    }
    fetches.add(up);
  }
}
