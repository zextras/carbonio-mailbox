// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.message;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.zimbra.common.soap.AdminConstants;
import com.zimbra.common.soap.VoiceAdminConstants;
import com.zimbra.soap.admin.type.AdminAttrsImpl;
import com.zimbra.soap.admin.type.UCServiceSelector;

/**
 * @zm-api-command-auth-required true
 * @zm-api-command-admin-auth-required true
 * @zm-api-command-description Generate a new Cisco Presence server session ID and persist 
 * the newly generated session id in zimbraUCCiscoPresenceSessionId attribute for the 
 * specified UC service.
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name=VoiceAdminConstants.E_UPDATE_PRESENCE_SESSION_ID_REQUEST)
public class UpdatePresenceSessionIdRequest extends AdminAttrsImpl {

    /**
     * @zm-api-field-description the UC service
     */
    @XmlElement(name=AdminConstants.E_UC_SERVICE, required=true)
    private UCServiceSelector ucService;
    
    /**
     * @zm-api-field-description app username
     */
    @XmlElement(name=AdminConstants.E_USERNAME, required=true)
    private String username;
    
    /**
     * @zm-api-field-description app password
     */
    @XmlElement(name=AdminConstants.E_PASSWORD, required=true)
    private String password;

    public UpdatePresenceSessionIdRequest() {
    }
    
    public UpdatePresenceSessionIdRequest(UCServiceSelector ucService, String username, String password) {
        setUCService(ucService);
        setUsername(username);
        setPassword(password);
    }

    public void setUCService(UCServiceSelector ucService) {
        this.ucService = ucService;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }

    public UCServiceSelector getId() {
        return ucService;
    }
    
    public String getUsername() {
        return username;
    }
    
    public String getPassword() {
        return password;
    }
}
