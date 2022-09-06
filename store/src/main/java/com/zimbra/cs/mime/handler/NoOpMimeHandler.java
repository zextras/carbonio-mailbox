// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.mime.handler;

import com.zimbra.cs.convert.AttachmentInfo;
import com.zimbra.cs.convert.ConversionException;
import com.zimbra.cs.mime.MimeHandler;
import com.zimbra.cs.mime.MimeHandlerException;
import java.io.IOException;
import org.apache.lucene.document.Document;

/**
 * A mime handler that does nothing. Unlike the unknown type handler this won't throw any exceptions
 * for calling the wrong method.
 *
 * <p>This handler is returned when indexing is turned off
 *
 * @author jpowers
 */
public class NoOpMimeHandler extends MimeHandler {

  @Override
  protected void addFields(Document doc) throws MimeHandlerException {}

  @Override
  public boolean isIndexingEnabled() {
    return false;
  }

  @Override
  public String convert(AttachmentInfo doc, String urlPart)
      throws IOException, ConversionException {
    return "";
  }

  @Override
  public boolean doConversion() {
    return false;
  }

  @Override
  public String getContentType() {
    return super.getContentType();
  }

  @Override
  public String getDescription() {
    return super.getDescription();
  }

  @Override
  public String getPartName() {
    return super.getPartName();
  }

  @Override
  protected String getContentImpl() throws MimeHandlerException {
    return "";
  }

  @Override
  protected boolean runsExternally() {
    return false;
  }
}
