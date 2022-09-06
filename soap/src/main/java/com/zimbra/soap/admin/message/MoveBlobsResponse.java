// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.message;

import com.google.common.base.MoreObjects;
import com.zimbra.common.soap.HsmConstants;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = HsmConstants.E_MOVE_BLOBS_RESPONSE)
public class MoveBlobsResponse {

  /**
   * @zm-api-field-tag num-blobs-moved
   * @zm-api-field-description Number of blobs moved
   */
  @XmlAttribute(name = HsmConstants.A_NUM_BLOBS_MOVED /* numBlobsMoved */, required = false)
  private Integer numBlobsMoved;

  /**
   * @zm-api-field-tag num-bytes-moved
   * @zm-api-field-description Number of bytes moved
   */
  @XmlAttribute(name = HsmConstants.A_NUM_BYTES_MOVED /* numBytesMoved */, required = false)
  private Long numBytesMoved;

  /**
   * @zm-api-field-tag total-mailboxes
   * @zm-api-field-description Total number of mailboxes
   */
  @XmlAttribute(name = HsmConstants.A_TOTAL_MAILBOXES /* totalMailboxes */, required = false)
  private Integer totalMailboxes;

  public MoveBlobsResponse() {}

  public void setNumBlobsMoved(Integer numBlobsMoved) {
    this.numBlobsMoved = numBlobsMoved;
  }

  public void setNumBytesMoved(Long numBytesMoved) {
    this.numBytesMoved = numBytesMoved;
  }

  public void setTotalMailboxes(Integer totalMailboxes) {
    this.totalMailboxes = totalMailboxes;
  }

  public Integer getNumBlobsMoved() {
    return numBlobsMoved;
  }

  public Long getNumBytesMoved() {
    return numBytesMoved;
  }

  public Integer getTotalMailboxes() {
    return totalMailboxes;
  }

  public MoreObjects.ToStringHelper addToStringInfo(MoreObjects.ToStringHelper helper) {
    return helper
        .add("numBlobsMoved", numBlobsMoved)
        .add("numBytesMoved", numBytesMoved)
        .add("totalMailboxes", totalMailboxes);
  }

  @Override
  public String toString() {
    return addToStringInfo(MoreObjects.toStringHelper(this)).toString();
  }
}
