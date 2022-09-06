// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.message;

import com.google.common.base.MoreObjects;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.AdminConstants;
import com.zimbra.soap.admin.type.ServerSelector;
import com.zimbra.soap.json.jackson.annotate.ZimbraJsonArrayForWrapper;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @zm-api-command-auth-required true
 * @zm-api-command-admin-auth-required true
 * @zm-api-command-description start/stop contact backup
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = AdminConstants.E_CONTACT_BACKUP_REQUEST)
public class ContactBackupRequest {
  @XmlEnum
  public enum Operation {
    // case must match
    start,
    stop;

    public static Operation fromString(String s) throws ServiceException {
      try {
        return Operation.valueOf(s);
      } catch (IllegalArgumentException e) {
        throw ServiceException.INVALID_REQUEST("unknown key: " + s, e);
      }
    }
  }

  /**
   * @zm-api-field-tag op
   * @zm-api-field-description op can be either start or stop
   */
  @XmlAttribute(name = AdminConstants.A_OP /* op */, required = true)
  private Operation op;

  /**
   * @zm-api-field-tag servers
   * @zm-api-field-description List of servers
   */
  @ZimbraJsonArrayForWrapper
  @XmlElementWrapper(name = AdminConstants.E_SERVERS /* servers */, required = false)
  @XmlElement(name = AdminConstants.E_SERVER /* server */, required = true)
  protected List<ServerSelector> servers;

  /** default constructor */
  public ContactBackupRequest() {
    this.op = null;
    this.servers = null;
  }

  /**
   * @param op
   * @param servers
   */
  public ContactBackupRequest(Operation op, List<ServerSelector> servers) {
    this.op = op;
    this.servers = servers;
  }

  /**
   * @return the op
   */
  public Operation getOp() {
    return op;
  }

  /**
   * @param op the op to set
   */
  public void setOp(Operation op) {
    this.op = op;
  }

  /**
   * @return the servers
   */
  public List<ServerSelector> getServers() {
    return servers;
  }

  /**
   * @param servers the servers to set
   */
  public void setServers(List<ServerSelector> servers) {
    this.servers = servers;
  }

  /**
   * @param servers the servers to add
   */
  public void addServers(List<ServerSelector> servers) {
    if (this.servers == null) {
      this.servers = new ArrayList<ServerSelector>();
    }
    this.servers.addAll(servers);
  }

  /**
   * @param servers the servers to set
   */
  public void addServer(ServerSelector server) {
    if (this.servers == null) {
      this.servers = new ArrayList<ServerSelector>();
    }
    this.servers.add(server);
  }

  public MoreObjects.ToStringHelper addToStringInfo(MoreObjects.ToStringHelper helper) {
    if (this.op != null) {
      helper.add("op", this.op);
    }
    if (this.servers != null && !this.servers.isEmpty()) {
      helper.add("servers", "");
      for (ServerSelector server : this.servers) {
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
