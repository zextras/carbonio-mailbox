// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.type;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.zimbra.common.soap.AdminConstants;
import com.zimbra.common.soap.BackupConstants;
import com.zimbra.common.soap.MailConstants;
import java.util.Collections;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(propOrder = {})
public class MailboxVolumesInfo {

  /**
   * @zm-api-field-tag mailbox-id
   * @zm-api-field-description Mailbox ID
   */
  @XmlAttribute(name = BackupConstants.A_MAILBOXID /* mbxid */, required = true)
  private int mailboxId;

  /**
   * @zm-api-field-tag sync-token
   * @zm-api-field-description Current sync token of the mailbox
   */
  @XmlAttribute(name = MailConstants.A_TOKEN /* token */, required = true)
  private int token;

  /**
   * @zm-api-field-description Volumes
   */
  @XmlElement(name = AdminConstants.E_VOLUME /* volume */, required = false)
  private List<MailboxVolumeInfo> volumes = Lists.newArrayList();

  private MailboxVolumesInfo() {}

  private MailboxVolumesInfo(int mailboxId, int token) {
    setMailboxId(mailboxId);
    setToken(token);
  }

  public static MailboxVolumesInfo createForMailboxIdAndToken(int mailboxId, int token) {
    return new MailboxVolumesInfo(mailboxId, token);
  }

  public void setMailboxId(int mailboxId) {
    this.mailboxId = mailboxId;
  }

  public void setToken(int token) {
    this.token = token;
  }

  public void setVolumes(Iterable<MailboxVolumeInfo> volumes) {
    this.volumes.clear();
    if (volumes != null) {
      Iterables.addAll(this.volumes, volumes);
    }
  }

  public void addVolume(MailboxVolumeInfo volume) {
    this.volumes.add(volume);
  }

  public int getMailboxId() {
    return mailboxId;
  }

  public int getToken() {
    return token;
  }

  public List<MailboxVolumeInfo> getVolumes() {
    return Collections.unmodifiableList(volumes);
  }

  public MoreObjects.ToStringHelper addToStringInfo(MoreObjects.ToStringHelper helper) {
    return helper.add("mailboxId", mailboxId).add("token", token).add("volumes", volumes);
  }

  @Override
  public String toString() {
    return addToStringInfo(MoreObjects.toStringHelper(this)).toString();
  }
}
