// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.lmtpserver;

import static com.zimbra.common.account.ZAttrProvisioning.A_zimbraLmtpAdvertisedName;
import static com.zimbra.common.account.ZAttrProvisioning.A_zimbraLmtpBindAddress;
import static com.zimbra.common.account.ZAttrProvisioning.A_zimbraLmtpBindPort;
import static com.zimbra.common.account.ZAttrProvisioning.A_zimbraLmtpExposeVersionOnBanner;
import static com.zimbra.common.account.ZAttrProvisioning.A_zimbraLmtpLHLORequired;
import static com.zimbra.common.account.ZAttrProvisioning.A_zimbraLmtpNumThreads;
import static com.zimbra.common.account.ZAttrProvisioning.A_zimbraLmtpPermanentFailureWhenOverQuota;
import static com.zimbra.common.account.ZAttrProvisioning.A_zimbraLmtpShutdownGraceSeconds;
import static com.zimbra.common.account.ZAttrProvisioning.A_zimbraMtaRecipientDelimiter;

import com.zimbra.common.localconfig.LC;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.util.Log;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.cs.server.ServerConfig;
import com.zimbra.cs.util.BuildInfo;
import com.zimbra.cs.util.Config;

public class LmtpConfig extends ServerConfig {
  private final LmtpBackend lmtpBackend;

  private static final String PROTOCOL = "LMTP";
  private static final int MAX_IDLE_TIME = 300; // seconds

  public static final LmtpConfig INSTANCE = new LmtpConfig();

  public static LmtpConfig getInstance() {
    return INSTANCE;
  }

  private LmtpConfig() {
    super(PROTOCOL, false);
    lmtpBackend = new ZimbraLmtpBackend(this);
  }

  @Override
  public String getServerName() {
    return getAttr(A_zimbraLmtpAdvertisedName, LC.zimbra_server_hostname.value());
  }

  @Override
  public String getServerVersion() {
    return getBooleanAttr(A_zimbraLmtpExposeVersionOnBanner, false) ? BuildInfo.VERSION : null;
  }

  @Override
  public int getMaxIdleTime() {
    return MAX_IDLE_TIME;
  }

  @Override
  public int getShutdownTimeout() {
    return getIntAttr(A_zimbraLmtpShutdownGraceSeconds, super.getShutdownTimeout());
  }

  @Override
  public int getMaxThreads() {
    return getIntAttr(A_zimbraLmtpNumThreads, super.getMaxThreads());
  }

  @Override
  public int getBindPort() {
    return getIntAttr(A_zimbraLmtpBindPort, Config.D_LMTP_BIND_PORT);
  }

  @Override
  public String getBindAddress() {
    return getAttr(A_zimbraLmtpBindAddress, null);
  }

  @Override
  public Log getLog() {
    return ZimbraLog.lmtp;
  }

  @Override
  public String getConnectionRejected() {
    return "421 " + getDescription() + " closing connection; service busy";
  }

  public String getMtaRecipientDelimiter() {
    try {
      return getGlobalConfig().getAttr(A_zimbraMtaRecipientDelimiter);
    } catch (ServiceException e) {
      getLog().warn("Unable to get global attribute: " + A_zimbraMtaRecipientDelimiter, e);
      return null;
    }
  }

  public LmtpBackend getLmtpBackend() {
    return lmtpBackend;
  }

  public boolean isPermanentFailureWhenOverQuota() {
    return getBooleanAttr(A_zimbraLmtpPermanentFailureWhenOverQuota, true);
  }

  public boolean isTLSEnforcedByServer() {
    return LC.zimbra_require_interprocess_security.booleanValue();
  }

  public boolean isLHLORequired() {
    return getBooleanAttr(A_zimbraLmtpLHLORequired, true);
  }
}
