// SPDX-FileCopyrightText: 2023 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: AGPL-3.0-only

package com.zextras.mailbox.client;

import java.io.InputStream;

/** Represent an answer from {@link com.zimbra.cs.service.UserServlet} */
public class UserServletResponse {
  final String contentType;
  final String fileName;
  final InputStream content;

  public UserServletResponse(String contentType, String fileName, InputStream content) {
    this.contentType = contentType;
    this.fileName = fileName;
    this.content = content;
  }

  public String getContentType() {
    return contentType;
  }

  public String getFileName() {
    return fileName;
  }

  public InputStream getContent() {
    return content;
  }
}
