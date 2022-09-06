// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.message;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.zimbra.common.soap.XMbxSearchConstants;
import com.zimbra.soap.admin.type.SearchNode;
import java.util.Collections;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = XMbxSearchConstants.E_GET_XMBX_SEARCHES_RESPONSE)
@XmlType(propOrder = {})
public class GetXMbxSearchesListResponse {

  /**
   * @zm-api-field-description Search task information
   */
  @XmlElement(name = XMbxSearchConstants.E_SrchTask /* searchtask */, required = false)
  private List<SearchNode> searchNodes = Lists.newArrayList();

  public GetXMbxSearchesListResponse() {}

  public void setSearchNodes(Iterable<SearchNode> searchNodes) {
    this.searchNodes.clear();
    if (searchNodes != null) {
      Iterables.addAll(this.searchNodes, searchNodes);
    }
  }

  public void addSearchNode(SearchNode searchNode) {
    this.searchNodes.add(searchNode);
  }

  public List<SearchNode> getSearchNodes() {
    return Collections.unmodifiableList(searchNodes);
  }

  public MoreObjects.ToStringHelper addToStringInfo(MoreObjects.ToStringHelper helper) {
    return helper.add("searchNodes", searchNodes);
  }

  @Override
  public String toString() {
    return addToStringInfo(MoreObjects.toStringHelper(this)).toString();
  }
}
