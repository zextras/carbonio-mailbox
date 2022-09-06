// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.mail.type;

import com.google.common.base.Function;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.soap.type.DataSource.ConnectionType;
import java.util.Arrays;
import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;

@XmlEnum
public enum MdsConnectionType {
  @XmlEnumValue("cleartext")
  cleartext,
  @XmlEnumValue("ssl")
  ssl,
  @XmlEnumValue("tls")
  tls,
  @XmlEnumValue("tls_if_available")
  tls_if_available;

  public static MdsConnectionType fromString(String s) throws ServiceException {
    try {
      return MdsConnectionType.valueOf(s);
    } catch (IllegalArgumentException e) {
      throw ServiceException.INVALID_REQUEST(
          "invalid type: " + s + ", valid values: " + Arrays.asList(MdsConnectionType.values()), e);
    }
  }

  public static Function<ConnectionType, MdsConnectionType> CT_TO_MCT =
      new Function<ConnectionType, MdsConnectionType>() {
        @Override
        public MdsConnectionType apply(ConnectionType from) {
          switch (from) {
            case cleartext:
              return cleartext;
            case ssl:
              return ssl;
            case tls:
              return tls;
            case tls_if_available:
              return tls_if_available;
          }
          ZimbraLog.soap.warn("Unexpected connection type %s.  Returning %s.", from, cleartext);
          return cleartext;
        }
      };

  public static Function<MdsConnectionType, ConnectionType> MCT_TO_CT =
      new Function<MdsConnectionType, ConnectionType>() {
        @Override
        public ConnectionType apply(MdsConnectionType from) {
          switch (from) {
            case cleartext:
              return ConnectionType.cleartext;
            case ssl:
              return ConnectionType.ssl;
            case tls:
              return ConnectionType.tls;
            case tls_if_available:
              return ConnectionType.tls_if_available;
          }
          ZimbraLog.soap.warn(
              "Unexpected connection type %s.  Returning %s.", from, ConnectionType.cleartext);
          return ConnectionType.cleartext;
        }
      };
}
