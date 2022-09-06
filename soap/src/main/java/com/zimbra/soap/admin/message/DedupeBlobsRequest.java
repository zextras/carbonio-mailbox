// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.message;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.AdminConstants;
import com.zimbra.soap.admin.message.AutoProvTaskControlRequest.Action;
import com.zimbra.soap.admin.type.IntIdAttr;
import java.util.Collections;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @zm-api-command-auth-required true
 * @zm-api-command-admin-auth-required true
 * @zm-api-command-description Dedupe the blobs having the same digest.
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = AdminConstants.E_DEDUPE_BLOBS_REQUEST)
public class DedupeBlobsRequest {

  @XmlEnum
  public static enum DedupAction {
    start,
    status,
    stop,
    reset;

    public static Action fromString(String action) throws ServiceException {
      try {
        return Action.valueOf(action);
      } catch (IllegalArgumentException e) {
        throw ServiceException.INVALID_REQUEST("unknown action: " + action, e);
      }
    }
  }

  /**
   * @zm-api-field-description Action to perform - one of <b>start|status|stop</b>
   */
  @XmlAttribute(name = AdminConstants.E_ACTION, required = true)
  private final DedupAction action;

  // ShortIdAttr would be a more accurate fit
  /**
   * @zm-api-field-description Volumes
   */
  @XmlElement(name = AdminConstants.E_VOLUME /* volume */, required = false)
  private List<IntIdAttr> volumes = Lists.newArrayList();

  public void setVolumes(Iterable<IntIdAttr> volumes) {
    this.volumes.clear();
    if (volumes != null) {
      Iterables.addAll(this.volumes, volumes);
    }
  }

  /** no-argument constructor wanted by JAXB */
  @SuppressWarnings("unused")
  private DedupeBlobsRequest() {
    this((DedupAction) null);
  }

  public DedupeBlobsRequest(DedupAction action) {
    this.action = action;
  }

  public DedupAction getAction() {
    return action;
  }

  public DedupeBlobsRequest addVolume(IntIdAttr volume) {
    this.volumes.add(volume);
    return this;
  }

  public List<IntIdAttr> getVolumes() {
    return Collections.unmodifiableList(volumes);
  }

  public MoreObjects.ToStringHelper addToStringInfo(MoreObjects.ToStringHelper helper) {
    return helper.add("action", action).add("volumes", volumes);
  }

  @Override
  public String toString() {
    return addToStringInfo(MoreObjects.toStringHelper(this)).toString();
  }
}
