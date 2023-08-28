// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.type;

import com.zimbra.common.service.ServiceException;
import java.util.Arrays;
import java.util.List;

public interface DataSource {

  public enum ConnectionType {
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
  };

  public void copy(DataSource from);

  public void setId(String id);

  public void setName(String name);

  public void setFolderId(String folderId);

  public void setEnabled(Boolean enabled);

  public void setImportOnly(Boolean importOnly);

  public void setHost(String host);

  public void setPort(Integer port);

  /* Interface interested in ConnectionType, not MdsConnectionType */
  public void setConnectionType(ConnectionType connectionType);

  public void setUsername(String username);

  public void setPassword(String password);

  public void setPollingInterval(String pollingInterval);

  public void setEmailAddress(String emailAddress);

  public void setUseAddressForForwardReply(Boolean useAddressForForwardReply);

  public void setDefaultSignature(String defaultSignature);

  public void setForwardReplySignature(String forwardReplySignature);

  public void setFromDisplay(String fromDisplay);

  public void setReplyToAddress(String replyToAddress);

  public void setReplyToDisplay(String replyToDisplay);

  public void setImportClass(String importClass);

  public void setFailingSince(Long failingSince);

  public void setLastError(String lastError);

  public void setAttributes(Iterable<String> attributes);

  public void addAttribute(String attribute);

  public String getId();

  public String getName();

  public String getFolderId();

  public Boolean isEnabled();

  public Boolean isImportOnly();

  public String getHost();

  public Integer getPort();

  /* Interface interested in ConnectionType, not MdsConnectionType */
  public ConnectionType getConnectionType();

  public String getUsername();

  public String getPassword();

  public String getPollingInterval();

  public String getEmailAddress();

  public Boolean isUseAddressForForwardReply();

  public String getDefaultSignature();

  public String getForwardReplySignature();

  public String getFromDisplay();

  public String getReplyToAddress();

  public String getReplyToDisplay();

  public String getImportClass();

  public Long getFailingSince();

  public String getLastError();

  public List<String> getAttributes();
}
