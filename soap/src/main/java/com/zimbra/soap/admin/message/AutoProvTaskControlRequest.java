// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.message;

import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.AdminConstants;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @zm-api-command-auth-required true
 * @zm-api-command-admin-auth-required true
 * @zm-api-command-description Under normal situations, the EAGER auto provisioning task(thread)
 *     should be started/stopped automatically by the server when appropriate. The task should be
 *     running when zimbraAutoProvPollingInterval is not 0 and zimbraAutoProvScheduledDomains is not
 *     empty. The task should be stopped otherwise. This API is to manually force start/stop or
 *     query status of the EAGER auto provisioning task. It is only for diagnosis purpose and should
 *     not be used under normal situations.
 */
@XmlRootElement(name = AdminConstants.E_AUTO_PROV_TASK_CONTROL_REQUEST)
public class AutoProvTaskControlRequest {

  @XmlEnum
  public static enum Action {
    start,
    status,
    stop;

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
  private final Action action;

  /** no-argument constructor wanted by JAXB */
  @SuppressWarnings("unused")
  private AutoProvTaskControlRequest() {
    this((Action) null);
  }

  public AutoProvTaskControlRequest(Action action) {
    this.action = action;
  }

  public Action getAction() {
    return action;
  }
}
