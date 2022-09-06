// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.mail.message;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.zimbra.common.soap.MailConstants;
import com.zimbra.soap.mail.type.DispositionAndText;
import java.util.Collections;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = MailConstants.E_DIFF_DOCUMENT_RESPONSE)
public class DiffDocumentResponse {

  /**
   * @zm-api-field-description Difference information in chunks
   */
  @XmlElement(name = MailConstants.E_CHUNK /* chunk */, required = false)
  private List<DispositionAndText> chunks = Lists.newArrayList();

  public DiffDocumentResponse() {}

  public void setChunks(Iterable<DispositionAndText> chunks) {
    this.chunks.clear();
    if (chunks != null) {
      Iterables.addAll(this.chunks, chunks);
    }
  }

  public DiffDocumentResponse addChunk(DispositionAndText chunk) {
    this.chunks.add(chunk);
    return this;
  }

  public List<DispositionAndText> getChunks() {
    return Collections.unmodifiableList(chunks);
  }

  public MoreObjects.ToStringHelper addToStringInfo(MoreObjects.ToStringHelper helper) {
    return helper.add("chunks", chunks);
  }

  @Override
  public String toString() {
    return addToStringInfo(MoreObjects.toStringHelper(this)).toString();
  }
}
