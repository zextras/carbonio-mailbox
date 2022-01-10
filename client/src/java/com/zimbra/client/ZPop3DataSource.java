// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.client;

import org.json.JSONException;

import com.zimbra.common.soap.Element;
import com.zimbra.common.soap.MailConstants;
import com.zimbra.common.util.SystemUtil;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.soap.account.type.AccountPop3DataSource;
import com.zimbra.soap.admin.type.DataSourceType;
import com.zimbra.soap.mail.type.DataSourceNameOrId;
import com.zimbra.soap.mail.type.MailPop3DataSource;
import com.zimbra.soap.mail.type.Pop3DataSourceNameOrId;
import com.zimbra.soap.type.DataSource;
import com.zimbra.soap.type.DataSource.ConnectionType;
import com.zimbra.soap.type.DataSources;
import com.zimbra.soap.type.Pop3DataSource;

public class ZPop3DataSource extends ZDataSource implements ToZJSONObject {

    public ZPop3DataSource(Pop3DataSource data) {
        this.data = DataSources.newPop3DataSource(data);
    }

    public ZPop3DataSource(String name, boolean enabled, String host, int port,
                           String username, String password, String folderid,
                           ConnectionType connectionType, boolean leaveOnServer) {
        data = new AccountPop3DataSource();
        data.setName(name);
        data.setEnabled(enabled);
        data.setHost(host);
        data.setPort(port);
        data.setUsername(username);
        data.setPassword(password);
        data.setFolderId(folderid);
        data.setConnectionType(connectionType);
        setLeaveOnServer(leaveOnServer);
    }

    @Deprecated
    public Element toElement(Element parent) {
        Element src = parent.addElement(MailConstants.E_DS_POP3);
        src.addAttribute(MailConstants.A_ID, data.getId());
        src.addAttribute(MailConstants.A_NAME, data.getName());
        src.addAttribute(MailConstants.A_DS_IS_ENABLED, data.isEnabled());
        src.addAttribute(MailConstants.A_DS_HOST, data.getHost());
        src.addAttribute(MailConstants.A_DS_PORT, data.getPort());
        src.addAttribute(MailConstants.A_DS_USERNAME, data.getUsername());
        src.addAttribute(MailConstants.A_DS_PASSWORD, data.getPassword());
        src.addAttribute(MailConstants.A_FOLDER, data.getFolderId());
        src.addAttribute(MailConstants.A_DS_CONNECTION_TYPE, data.getConnectionType().name());
        src.addAttribute(MailConstants.A_DS_LEAVE_ON_SERVER, leaveOnServer());
        ZimbraLog.test.info("XXX bburtin: " + src.prettyPrint());
        return src;
    }

    @Deprecated
    public Element toIdElement(Element parent) {
        Element src = parent.addElement(MailConstants.E_DS_POP3);
        src.addAttribute(MailConstants.A_ID, getId());
        return src;
    }

    @Override
    public DataSource toJaxb() {
        MailPop3DataSource jaxbObject = new MailPop3DataSource();
        jaxbObject.setId(data.getId());
        jaxbObject.setName(data.getName());
        jaxbObject.setHost(data.getHost());
        jaxbObject.setPort(data.getPort());
        jaxbObject.setUsername(data.getUsername());
        jaxbObject.setPassword(data.getPassword());
        jaxbObject.setFolderId(data.getFolderId());
        jaxbObject.setConnectionType(data.getConnectionType());
        jaxbObject.setLeaveOnServer(leaveOnServer());
        jaxbObject.setEnabled(data.isEnabled());
        return jaxbObject;
    }

    @Override
    public DataSourceNameOrId toJaxbNameOrId() {
        Pop3DataSourceNameOrId jaxbObject = Pop3DataSourceNameOrId.createForId(data.getId());
        return jaxbObject;
    }
    private Pop3DataSource getData() {
        return ((Pop3DataSource)data);
    }
    @Override
    public DataSourceType getType() { return DataSourceType.pop3; }

    public String getHost() { return data.getHost(); }
    public void setHost(String host) { data.setHost(host); }

    public int getPort() { return SystemUtil.coalesce(data.getPort(), -1); }
    public void setPort(int port) { data.setPort(port); } 

    public String getUsername() { return data.getUsername(); }
    public void setUsername(String username) { data.setUsername(username); }

    public String getFolderId() { return data.getFolderId(); }
    public void setFolderId(String folderid) { data.setFolderId(folderid); }

    public ConnectionType getConnectionType() {
        ConnectionType ct = data.getConnectionType();
        return (ct == null ? ConnectionType.cleartext : ct);
    }
    
    public void setConnectionType(ConnectionType connectionType) {
        data.setConnectionType(connectionType);
    }
    
    public boolean leaveOnServer() {
        Boolean val = getData().isLeaveOnServer();
        return (val == null ? true : val);
    }
    
    public void setLeaveOnServer(boolean leaveOnServer) { getData().setLeaveOnServer(leaveOnServer); }
    
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
        zjo.put("leaveOnServer", leaveOnServer());
        return zjo;
    }

    public String toString() {
        return String.format("[ZPop3DataSource %s]", getName());
    }

    public String dump() {
        return ZJSONObject.toString(this);
    }
}
