// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.adminext.message;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Lists;
import com.zimbra.common.soap.AdminExtConstants;
import com.zimbra.soap.adminext.type.BulkIMAPImportTaskInfo;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = AdminExtConstants.E_GET_BULK_IMAP_IMPORT_TASKLIST_RESPONSE)
public class GetBulkIMAPImportTaskListResponse {

  /**
   * @zm-api-field-description Information on inport tasks
   */
  @XmlElement(name = AdminExtConstants.E_Task /* task */, required = false)
  private List<BulkIMAPImportTaskInfo> tasks = Lists.newArrayList();

  public GetBulkIMAPImportTaskListResponse() {}

  public List<BulkIMAPImportTaskInfo> getTasks() {
    return tasks;
  }

  public MoreObjects.ToStringHelper addToStringInfo(MoreObjects.ToStringHelper helper) {
    return helper.add("tasks", tasks);
  }

  @Override
  public String toString() {
    return addToStringInfo(MoreObjects.toStringHelper(this)).toString();
  }
}
