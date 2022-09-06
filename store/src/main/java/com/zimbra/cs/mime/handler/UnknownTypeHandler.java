// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.mime.handler;

import com.zimbra.cs.convert.AttachmentInfo;
import com.zimbra.cs.mime.MimeHandler;
import org.apache.lucene.document.Document;

/**
 * {@link MimeHandler} for no conversion.
 *
 * @since Apr 1, 2004
 * @author schemers
 */
public class UnknownTypeHandler extends MimeHandler {

  private String contentType;

  @Override
  protected boolean runsExternally() {
    return false;
  }

  @Override
  public void addFields(Document doc) {
    // do nothing
  }

  @Override
  protected String getContentImpl() {
    return "";
  }

  @Override
  public boolean isIndexingEnabled() {
    return true;
  }

  @Override
  public String getContentType() {
    return contentType;
  }

  @Override
  public String convert(AttachmentInfo doc, String baseURL) {
    throw new IllegalStateException("conversion not allowed for content of unknown type");
  }

  @Override
  public boolean doConversion() {
    return false;
  }

  @Override
  public void setContentType(String value) {
    contentType = value;
  }
}
