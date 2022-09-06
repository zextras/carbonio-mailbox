// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.client;

import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.Element;
import com.zimbra.common.soap.MailConstants;
import com.zimbra.common.util.SystemUtil;
import com.zimbra.soap.admin.type.DataSourceType;
import com.zimbra.soap.mail.type.DataSourceNameOrId;
import com.zimbra.soap.mail.type.ImapDataSourceNameOrId;
import com.zimbra.soap.mail.type.MailImapDataSource;
import com.zimbra.soap.type.DataSource;
import com.zimbra.soap.type.DataSource.ConnectionType;
import com.zimbra.soap.type.DataSources;
import com.zimbra.soap.type.ImapDataSource;
import org.json.JSONException;

public class ZImapDataSource extends ZDataSource implements ToZJSONObject {

  public ZImapDataSource(ImapDataSource data) {
    this.data = DataSources.newImapDataSource(data);
  }

  public ZImapDataSource(
      String name,
      boolean enabled,
      String host,
      int port,
      String username,
      String password,
      String folderid,
      ConnectionType connectionType,
      boolean isImportOnly)
      throws ServiceException {
    data = DataSources.newImapDataSource();
    data.setName(name);
    data.setEnabled(enabled);
    data.setHost(host);
    data.setPort(port);
    data.setUsername(username);
    data.setPassword(password);

    try {
      data.setFolderId(folderid);
    } catch (NumberFormatException e) {
      throw ServiceException.INVALID_REQUEST("Invalid folder id", e);
    }

    data.setConnectionType(connectionType);
    data.setImportOnly(isImportOnly);
  }

  public ZImapDataSource(
      String name,
      boolean enabled,
      String host,
      int port,
      String username,
      String password,
      String folderid,
      ConnectionType connectionType)
      throws ServiceException {
    this(name, enabled, host, port, username, password, folderid, connectionType, false);
  }

  @Deprecated
  public Element toElement(Element parent) {
    Element src = parent.addElement(MailConstants.E_DS_IMAP);
    src.addAttribute(MailConstants.A_ID, data.getId());
    src.addAttribute(MailConstants.A_NAME, data.getName());
    src.addAttribute(MailConstants.A_DS_IS_ENABLED, data.isEnabled());
    src.addAttribute(MailConstants.A_DS_HOST, data.getHost());
    src.addAttribute(MailConstants.A_DS_PORT, data.getPort());
    src.addAttribute(MailConstants.A_DS_USERNAME, data.getUsername());
    src.addAttribute(MailConstants.A_DS_PASSWORD, data.getPassword());
    src.addAttribute(MailConstants.A_FOLDER, data.getFolderId());
    src.addAttribute(MailConstants.A_DS_CONNECTION_TYPE, data.getConnectionType().name());
    src.addAttribute(MailConstants.A_DS_IS_IMPORTONLY, data.isImportOnly());
    return src;
  }

  public Element toIdElement(Element parent) {
    Element src = parent.addElement(MailConstants.E_DS_IMAP);
    src.addAttribute(MailConstants.A_ID, getId());
    return src;
  }

  @Override
  public DataSource toJaxb() {
    MailImapDataSource jaxbObject = new MailImapDataSource();
    jaxbObject.setId(data.getId());
    jaxbObject.setName(data.getName());
    jaxbObject.setHost(data.getHost());
    jaxbObject.setPort(data.getPort());
    jaxbObject.setUsername(data.getUsername());
    jaxbObject.setPassword(data.getPassword());
    jaxbObject.setFolderId(data.getFolderId());
    jaxbObject.setConnectionType(data.getConnectionType());
    jaxbObject.setImportOnly(data.isImportOnly());
    jaxbObject.setEnabled(data.isEnabled());
    return jaxbObject;
  }

  @Override
  public DataSourceNameOrId toJaxbNameOrId() {
    ImapDataSourceNameOrId jaxbObject = ImapDataSourceNameOrId.createForId(data.getId());
    return jaxbObject;
  }

  @Override
  public DataSourceType getType() {
    return DataSourceType.imap;
  }

  public String getHost() {
    return data.getHost();
  }

  public void setHost(String host) {
    data.setHost(host);
  }

  public int getPort() {
    return SystemUtil.coalesce(data.getPort(), -1);
  }

  public void setPort(int port) {
    data.setPort(port);
  }

  public String getUsername() {
    return data.getUsername();
  }

  public void setUsername(String username) {
    data.setUsername(username);
  }

  public String getFolderId() {
    return data.getFolderId();
  }

  public void setFolderId(String folderid) {
    data.setFolderId(folderid);
  }

  public ConnectionType getConnectionType() {
    ConnectionType ct = data.getConnectionType();
    return (ct != null ? ct : ConnectionType.cleartext);
  }

  public void setConnectionType(ConnectionType connectionType) {
    data.setConnectionType(connectionType);
  }

  public ZJSONObject toZJSONObject() throws JSONException {
    ZJSONObject zjo = new ZJSONObject();
    zjo.put("id", data.getId());
    zjo.put("name", data.getName());
    zjo.put("enabled", data.isEnabled());
    zjo.put("host", data.getHost());
    zjo.put("port", data.getPort());
    zjo.put("username", data.getUsername());
    zjo.put("folderId", data.getFolderId());
    zjo.put("connectionType", data.getConnectionType().toString());
    zjo.put("importOnly", data.isImportOnly());
    return zjo;
  }

  public String toString() {
    return String.format("[ZImapDataSource %s]", getName());
  }

  public String dump() {
    return ZJSONObject.toString(this);
  }

  public void setImportOnly(boolean importOnly) {
    data.setImportOnly(importOnly);
  }

  public boolean isImportOnly() {
    return SystemUtil.coalesce(data.isImportOnly(), Boolean.FALSE);
  }
}
