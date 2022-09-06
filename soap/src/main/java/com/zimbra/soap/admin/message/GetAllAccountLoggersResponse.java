// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.message;

import com.google.common.collect.Lists;
import com.zimbra.common.soap.AdminConstants;
import com.zimbra.soap.admin.type.AccountLoggerInfo;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = AdminConstants.E_GET_ALL_ACCOUNT_LOGGERS_RESPONSE)
public class GetAllAccountLoggersResponse {

  /**
   * @zm-api-field-description Account loggers that have been created on the given server since the
   *     last server start
   */
  @XmlElement(name = AdminConstants.E_ACCOUNT_LOGGER, required = false)
  private List<AccountLoggerInfo> loggers = Lists.newArrayList();

  public GetAllAccountLoggersResponse() {
    this((Collection<AccountLoggerInfo>) null);
  }

  public GetAllAccountLoggersResponse(Collection<AccountLoggerInfo> loggers) {
    setLoggers(loggers);
  }

  public GetAllAccountLoggersResponse setLoggers(Collection<AccountLoggerInfo> loggers) {
    this.loggers.clear();
    if (loggers != null) {
      this.loggers.addAll(loggers);
    }
    return this;
  }

  public GetAllAccountLoggersResponse addLogger(AccountLoggerInfo logger) {
    loggers.add(logger);
    return this;
  }

  public List<AccountLoggerInfo> getLoggers() {
    return Collections.unmodifiableList(loggers);
  }
}
