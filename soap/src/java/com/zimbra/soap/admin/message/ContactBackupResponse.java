// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.message;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import com.google.common.base.MoreObjects;
import com.zimbra.common.soap.AdminConstants;
import com.zimbra.soap.admin.type.ContactBackupServer;
import com.zimbra.soap.json.jackson.annotate.ZimbraJsonArrayForWrapper;

/**
 * @zm-api-command-auth-required true
 * @zm-api-command-admin-auth-required true
 * @zm-api-command-description response for start/stop contact backup
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name=AdminConstants.E_CONTACT_BACKUP_RESPONSE)
public class ContactBackupResponse {
    /**
     * @zm-api-field-tag mailboxIds
     * @zm-api-field-description List of mailbox ids
     */
    @ZimbraJsonArrayForWrapper
    @XmlElementWrapper(name=AdminConstants.E_SERVERS /* servers */, required=false)
    @XmlElement(name=AdminConstants.E_SERVER /* server */, required=true)
    private List<ContactBackupServer> servers;

    /**
     * defualt constructor
     */
    public ContactBackupResponse() {
        this.servers = null;
    }

    /**
     * @param servers
     */
    public ContactBackupResponse(List<ContactBackupServer> servers) {
        this.servers = servers;
    }

    /**
     * @return the servers
     */
    public List<ContactBackupServer> getServers() {
        return servers;
    }

    /**
     * @param servers the servers to set
     */
    public void setServers(List<ContactBackupServer> servers) {
        this.servers = servers;
    }

    /**
     * @param servers the servers to set
     */
    public void addServers(List<ContactBackupServer> servers) {
        if (this.servers == null) {
            this.servers = new ArrayList<ContactBackupServer>();
        }
        this.servers.addAll(servers);
    }

    /**
     * @param servers the servers to set
     */
    public void addServer(ContactBackupServer server) {
        if (this.servers == null) {
            this.servers = new ArrayList<ContactBackupServer>();
        }
        this.servers.add(server);
    }

    public MoreObjects.ToStringHelper addToStringInfo(MoreObjects.ToStringHelper helper) {
        if (this.servers != null && !this.servers.isEmpty()) {
            helper.add("servers", "");
            for (ContactBackupServer server : this.servers) {
                helper.add("server", server);
            }
        }
        return helper;
    }

    @Override
    public String toString() {
        return addToStringInfo(MoreObjects.toStringHelper(this)).toString();
    }
}
