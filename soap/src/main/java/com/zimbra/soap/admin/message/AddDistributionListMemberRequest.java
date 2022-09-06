// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.message;

import com.google.common.collect.Lists;
import com.zimbra.common.soap.AdminConstants;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @zm-api-command-auth-required true
 * @zm-api-command-admin-auth-required true
 * @zm-api-command-description Adding members to a distribution list <br>
 *     Access: domain admin sufficient <br>
 *     <br>
 *     Adding existing members is allowed, even if it may result in this request being a no-op
 *     because all &lt;dlm> addrs are already members.
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = AdminConstants.E_ADD_DISTRIBUTION_LIST_MEMBER_REQUEST)
public class AddDistributionListMemberRequest {

  /**
   * @zm-api-field-tag value-of-id
   * @zm-api-field-description Zimbra ID
   */
  @XmlAttribute(name = AdminConstants.E_ID, required = true)
  private String id;

  /**
   * @zm-api-field-description Members
   */
  @XmlElement(name = AdminConstants.E_DLM, required = true)
  private List<String> members = Lists.newArrayList();

  public AddDistributionListMemberRequest() {
    this((String) null, (Collection<String>) null);
  }

  public AddDistributionListMemberRequest(String id, Collection<String> members) {
    setId(id);
    setMembers(members);
  }

  public AddDistributionListMemberRequest setMembers(Collection<String> members) {
    this.members.clear();
    if (members != null) {
      this.members.addAll(members);
    }
    return this;
  }

  public AddDistributionListMemberRequest addMember(String member) {
    members.add(member);
    return this;
  }

  public List<String> getMembers() {
    return Collections.unmodifiableList(members);
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getId() {
    return id;
  }
}
