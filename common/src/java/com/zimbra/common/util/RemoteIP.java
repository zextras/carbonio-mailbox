// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.common.util;

import com.zimbra.common.localconfig.LC;
import java.util.HashSet;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;

public class RemoteIP {

  public static final String X_ORIGINATING_IP_HEADER = LC.zimbra_http_originating_ip_header.value();
  public static final String X_ORIGINATING_PORT_HEADER = "X-Forwarded-Port";
  public static final String X_ORIGINATING_PROTOCOL_HEADER = "X-Forwarded-Proto";

  /** IP of the http client, Should be always present. */
  private String mClientIP;

  /** port of the http client, Should be always present. */
  private int mClientPort;

  /** IP of the originating client. It can be null. */
  private String mOrigIP;

  /** Port of the originating client. Can be null. */
  private Integer mOrigPort;

  /**
   * It can be the IP of the http client, or in the presence of a real origin IP address http
   * header(header specified in the LC key zimbra_http_originating_ip_header) the IP of the real
   * origin client if the http client is in a trusted network.
   *
   * <p>Should be always present.
   */
  private String mRequestIP;

  /** Port number of the http client. */
  private Integer mRequestPort;

  /** Original protocol of the request. */
  private String mOrigProto;

  public RemoteIP(HttpServletRequest req, TrustedIPs trustedIPs) {
    mClientIP = req.getRemoteAddr();
    mClientPort = req.getRemotePort();

    if (trustedIPs.isIpTrusted(mClientIP)) {
      mOrigIP = req.getHeader(X_ORIGINATING_IP_HEADER);
      String origPort = req.getHeader(X_ORIGINATING_PORT_HEADER);
      if (origPort != null) {
        try {
          mOrigPort = Integer.parseInt(origPort);
        } catch (NumberFormatException e) {
          // ignore bad header
        }
      }

      mOrigProto = req.getHeader(X_ORIGINATING_PROTOCOL_HEADER);
    }

    if (mOrigPort != null) {
      mRequestPort = mOrigPort;
    } else {
      mRequestPort = mClientPort;
    }

    if (mOrigIP != null) {
      mRequestIP = mOrigIP;
    } else {
      mRequestIP = mClientIP;
    }
  }

  public String getClientIP() {
    return mClientIP;
  }

  public String getOrigIP() {
    return mOrigIP;
  }

  public String getRequestIP() {
    return mRequestIP;
  }

  public Integer getOrigPort() {
    return mOrigPort;
  }

  public Integer getRequestPort() {
    return mRequestPort;
  }

  public Integer getClientPort() {
    return mClientPort;
  }

  public String getOrigProto() {
    return mOrigProto;
  }

  public void addToLoggingContext() {
    if (mOrigIP != null) {
      ZimbraLog.addOrigIpToContext(mOrigIP);
    }

    if (mOrigPort != null) {
      ZimbraLog.addOrigPortToContext(mOrigPort);
    }

    if (mOrigProto != null) {
      ZimbraLog.addOrigProtoToContext(mOrigProto);
    }
    // don't log client's IP or client's port if client's IP is localhost
    if (!TrustedIPs.isLocalhost(mClientIP)) {
      if (mClientIP != null) {
        ZimbraLog.addIpToContext(mClientIP);
      }
      if (mClientPort != 0) {
        ZimbraLog.addPortToContext(mClientPort);
      }
    }
  }

  @Override
  public String toString() {
    return String.format(
        "RemoteIP [mClientIP=%s, mOrigIP=%s, mRequestIP=%s] : RemotePort [mClientPort=%d,"
            + " mOrigPort=%d, mRequestPort=%d] mOrigProto=%s",
        mClientIP, mOrigIP, mRequestIP, mClientPort, mOrigPort, mRequestPort, mOrigProto);
  }

  public static class TrustedIPs {
    private static final String IP_LOCALHOST = "127.0.0.1";

    private Set<String> mTrustedIPs = new HashSet<String>();

    public TrustedIPs(String[] ips) {
      if (ips != null) {
        for (String ip : ips) {
          if (!StringUtil.isNullOrEmpty(ip)) mTrustedIPs.add(ip);
        }
      }
    }

    public boolean isIpTrusted(String ip) {
      return isLocalhost(ip) || mTrustedIPs.contains(ip);
    }

    private static boolean isLocalhost(String ip) {
      return IP_LOCALHOST.equals(ip);
    }

    @Override
    public String toString() {
      return "TrustedIPs [mTrustedIPs=" + mTrustedIPs + "]";
    }
  }
}
