// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.type;

import com.zimbra.common.service.ServiceException;
import java.util.Arrays;
import java.util.List;

public interface DataSource {

  enum ConnectionType {
    cleartext,
    ssl,
    tls,
    tls_if_available;

    public static ConnectionType fromString(String s) throws ServiceException {
      try {
        return ConnectionType.valueOf(s);
      } catch (IllegalArgumentException e) {
        throw ServiceException.INVALID_REQUEST(
            "invalid type: " + s + ", valid values: " + Arrays.asList(ConnectionType.values()), e);
      }
    }
  }

  void copy(DataSource from);

  void setId(String id);

  void setName(String name);

  void setFolderId(String folderId);

  void setEnabled(Boolean enabled);

  void setImportOnly(Boolean importOnly);

  void setHost(String host);

  void setPort(Integer port);

  /* Interface interested in ConnectionType, not MdsConnectionType */
  void setConnectionType(ConnectionType connectionType);

  void setUsername(String username);

  void setPassword(String password);

  void setPollingInterval(String pollingInterval);

  void setEmailAddress(String emailAddress);

  void setUseAddressForForwardReply(Boolean useAddressForForwardReply);

  void setDefaultSignature(String defaultSignature);

  void setForwardReplySignature(String forwardReplySignature);

  void setFromDisplay(String fromDisplay);

  void setReplyToAddress(String replyToAddress);

  void setReplyToDisplay(String replyToDisplay);

  void setImportClass(String importClass);

  void setFailingSince(Long failingSince);

  void setLastError(String lastError);

  void setAttributes(Iterable<String> attributes);

  void addAttribute(String attribute);

  String getId();

  String getName();

  String getFolderId();

  Boolean isEnabled();

  Boolean isImportOnly();

  String getHost();

  Integer getPort();

  /* Interface interested in ConnectionType, not MdsConnectionType */
  ConnectionType getConnectionType();

  String getUsername();

  String getPassword();

  String getPollingInterval();

  String getEmailAddress();

  Boolean isUseAddressForForwardReply();

  String getDefaultSignature();

  String getForwardReplySignature();

  String getFromDisplay();

  String getReplyToAddress();

  String getReplyToDisplay();

  String getImportClass();

  Long getFailingSince();

  String getLastError();

  List<String> getAttributes();
}
