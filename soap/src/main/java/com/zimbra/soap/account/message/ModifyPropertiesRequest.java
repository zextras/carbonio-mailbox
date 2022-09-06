// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.account.message;

import com.zimbra.common.soap.AccountConstants;
import com.zimbra.soap.account.type.Prop;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * <ModifyPropertiesRequest> <prop zimlet="{zimlet-name}" name="{name}">{value}</prop> ... <prop
 * zimlet="{zimlet-name}" name="{name}">{value}</prop> </ModifyPropertiesRequest>
 *
 * @zm-api-command-auth-required true
 * @zm-api-command-admin-auth-required false
 * @zm-api-command-description Modify properties related to zimlets
 */
@XmlRootElement(name = AccountConstants.E_MODIFY_PROPERTIES_REQUEST)
public class ModifyPropertiesRequest {
  /**
   * @zm-api-field-description Property to be modified
   */
  @XmlElement(name = AccountConstants.E_PROPERTY, required = true)
  private List<Prop> props = new ArrayList<Prop>();

  public List<Prop> getProps() {
    return props;
  }

  public void setProps(List<Prop> props) {
    this.props = props;
  }
}
