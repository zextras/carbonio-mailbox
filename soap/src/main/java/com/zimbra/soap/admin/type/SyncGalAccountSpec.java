// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.type;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.zimbra.common.soap.AdminConstants;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

@XmlAccessorType(XmlAccessType.NONE)
public class SyncGalAccountSpec {

  /**
   * @zm-api-field-tag account-id
   * @zm-api-field-description Account ID
   */
  @XmlAttribute(name = AdminConstants.A_ID /* id */, required = true)
  private String id;

  /**
   * @zm-api-field-description SyncGalAccount data source specifications
   */
  @XmlElement(name = AdminConstants.E_DATASOURCE /* datasource */, required = false)
  private List<SyncGalAccountDataSourceSpec> dataSources = Lists.newArrayList();

  public SyncGalAccountSpec() {}

  private SyncGalAccountSpec(String id) {
    setId(id);
  }

  public static SyncGalAccountSpec createForId(String id) {
    return new SyncGalAccountSpec(id);
  }

  public void setId(String id) {
    this.id = id;
  }

  public void setDataSources(Iterable<SyncGalAccountDataSourceSpec> dataSources) {
    this.dataSources.clear();
    if (dataSources != null) {
      Iterables.addAll(this.dataSources, dataSources);
    }
  }

  public void addDataSource(SyncGalAccountDataSourceSpec dataSource) {
    this.dataSources.add(dataSource);
  }

  public String getId() {
    return id;
  }

  public List<SyncGalAccountDataSourceSpec> getDataSources() {
    return dataSources;
  }

  public MoreObjects.ToStringHelper addToStringInfo(MoreObjects.ToStringHelper helper) {
    return helper.add("id", id).add("dataSources", dataSources);
  }

  @Override
  public String toString() {
    return addToStringInfo(MoreObjects.toStringHelper(this)).toString();
  }
}
