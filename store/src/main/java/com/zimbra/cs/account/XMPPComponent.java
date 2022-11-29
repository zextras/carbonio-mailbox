// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.account;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.common.base.Joiner;
import com.google.common.base.MoreObjects;
import com.zimbra.common.account.Key;
import com.zimbra.common.service.ServiceException;

/**
 * Systemwide configuration entry for XMPP component (e.g. conference.mydomain.com) in the cloud
 */
public class XMPPComponent extends NamedEntry implements Comparable {

    public XMPPComponent(String name, String id, Map<String,Object> attrs, Provisioning prov) {
        super(name, id, attrs, null, prov);
    }

    @Override
    public EntryType getEntryType() {
        return EntryType.XMPPCOMPONENT;
    }

    public String getComponentCategory() {
        return getAttr(Provisioning.A_zimbraXMPPComponentCategory);
    }

    public String getComponentType() {
        return getAttr(Provisioning.A_zimbraXMPPComponentType);
    }

    public String getLongName() {
        return getAttr(Provisioning.A_zimbraXMPPComponentName);
    }

    public String getClassName() {
        return getAttr(Provisioning.A_zimbraXMPPComponentClassName);
    }

    public String getShortName() throws ServiceException {
        String name = getName();
        Domain d = getDomain();
        if (d == null) {
            throw ServiceException.FAILURE("Invalid configuration data: XMPPComponent name, \""+
                                           name+"\" points to nonexistent domain: "+getDomainId(), null);
        }
        String domainName = d.getName();
        if (!name.endsWith(domainName)) {
            throw ServiceException.FAILURE("Invalid configuration data: XMPPComponent name, \""+
                                           name+"\" must be a subdomain of domain \""+domainName+
                                           "\"", null);
        }
        String toRet = name.substring(0, (name.length() - domainName.length())-1);
        return toRet;
    }

    public List<String> getComponentFeatures() {
        List<String> toRet = null;
        String[] features = this.getMultiAttr(Provisioning.A_zimbraXMPPComponentFeatures);
        if (features != null && features.length > 0) {
            toRet = new ArrayList<String>(features.length);
            for (String s : features)
                toRet.add(s);
        } else {
            toRet = new ArrayList<String>();
        }
        return toRet;
    }

    public String getDomainId() {
        return getAttr(Provisioning.A_zimbraDomainId);
    }
    public Domain getDomain() throws ServiceException {
        return Provisioning.getInstance().get(Key.DomainBy.id, getDomainId());
    }

    public String getServerId() {
        return getAttr(Provisioning.A_zimbraServerId);
    }
    public Server getServer() throws ServiceException {
        return Provisioning.getInstance().get(Key.ServerBy.id, getServerId());
    }

    @Override
    public String toString() {
        MoreObjects.ToStringHelper helper = MoreObjects.toStringHelper(this)
            .add("name", getName()).add("category", getComponentCategory()).add("type", getComponentType());
        helper.add("domainId", getDomainId());
        Domain domain = null;
        try {
            domain = getDomain();
        } catch (ServiceException e) {
        }
        if (domain != null) {
            helper.add("domainName", domain.getName());
        }
        helper.add("serverId", getServerId());
        Server server = null;
        try {
            server = getServer();
        } catch (ServiceException e) {
        }
        if (server != null) {
            helper.add("serverName", server.getName());
        }
        helper.add("feature", Joiner.on(',').join(getComponentFeatures()));
        return helper.toString();
    }
}
