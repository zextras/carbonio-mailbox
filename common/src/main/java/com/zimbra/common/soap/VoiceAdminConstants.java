// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.common.soap;

import org.dom4j.QName;
import org.dom4j.Namespace;

public class VoiceAdminConstants {
    public static final String NAMESPACE_STR = AdminConstants.NAMESPACE_STR;
    public static final Namespace NAMESPACE = Namespace.get(NAMESPACE_STR);
    
    public static final String E_GET_ALL_UC_PROVIDERS_REQUEST = "GetAllUCProvidersRequest";
    public static final String E_GET_ALL_UC_PROVIDERS_RESPONSE = "GetAllUCProvidersResponse";
    public static final String E_UPDATE_PRESENCE_SESSION_ID_REQUEST = "UpdatePresenceSessionIdRequest";
    public static final String E_UPDATE_PRESENCE_SESSION_ID_RESPONSE = "UpdatePresenceSessionIdResponse";
    
    public static final QName GET_ALL_UC_PROVIDERS_REQUEST = QName.get(E_GET_ALL_UC_PROVIDERS_REQUEST, NAMESPACE);
    public static final QName GET_ALL_UC_PROVIDERS_RESPONSE = QName.get(E_GET_ALL_UC_PROVIDERS_RESPONSE, NAMESPACE);
    public static final QName UPDATE_PRESENCE_SESSION_ID_REQUEST = QName.get(E_UPDATE_PRESENCE_SESSION_ID_REQUEST, NAMESPACE);
    public static final QName UPDATE_PRESENCE_SESSION_ID_RESPONSE = QName.get(E_UPDATE_PRESENCE_SESSION_ID_RESPONSE, NAMESPACE);
    
    public static final String E_PROVIDER = "provider";
}
