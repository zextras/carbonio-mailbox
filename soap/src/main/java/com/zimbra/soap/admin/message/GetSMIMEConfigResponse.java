// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.message;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.zimbra.common.soap.AdminConstants;
import com.zimbra.soap.admin.type.SMIMEConfigInfo;
import java.util.Collections;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = AdminConstants.E_GET_SMIME_CONFIG_RESPONSE)
@XmlType(propOrder = {})
public class GetSMIMEConfigResponse {

  /**
   * @zm-api-field-description SMIME configuration information
   */
  @XmlElement(name = AdminConstants.E_CONFIG /* config */, required = false)
  private List<SMIMEConfigInfo> configs = Lists.newArrayList();

  public GetSMIMEConfigResponse() {}

  public void setConfigs(Iterable<SMIMEConfigInfo> configs) {
    this.configs.clear();
    if (configs != null) {
      Iterables.addAll(this.configs, configs);
    }
  }

  public void addConfig(SMIMEConfigInfo config) {
    this.configs.add(config);
  }

  public List<SMIMEConfigInfo> getConfigs() {
    return Collections.unmodifiableList(configs);
  }

  public MoreObjects.ToStringHelper addToStringInfo(MoreObjects.ToStringHelper helper) {
    return helper.add("configs", configs);
  }

  @Override
  public String toString() {
    return addToStringInfo(MoreObjects.toStringHelper(this)).toString();
  }
}
