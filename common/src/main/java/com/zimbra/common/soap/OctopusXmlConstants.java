// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.common.soap;

import org.dom4j.Namespace;
import org.dom4j.QName;

public final class OctopusXmlConstants {

    public static final String NAMESPACE_STR = MailConstants.NAMESPACE_STR;
    public static final Namespace NAMESPACE = Namespace.get(NAMESPACE_STR);

    public static final String E_GET_ACTIVITY_STREAM_REQUEST = "GetActivityStreamRequest";
    public static final String E_GET_ACTIVITY_STREAM_RESPONSE = "GetActivityStreamResponse";

    public static final String E_REGISTER_DEVICE_REQUEST = "RegisterDeviceRequest";
    public static final String E_REGISTER_DEVICE_RESPONSE = "RegisterDeviceResponse";

    public static final String E_UPDATE_DEVICE_STATUS_REQUEST = "UpdateDeviceStatusRequest";
    public static final String E_UPDATE_DEVICE_STATUS_RESPONSE = "UpdateDeviceStatusResponse";

    public static final String E_CHECK_DEVICE_STATUS_REQUEST = "CheckDeviceStatusRequest";
    public static final String E_CHECK_DEVICE_STATUS_RESPONSE = "CheckDeviceStatusResponse";

    public static final String E_GET_ALL_DEVICES_REQUEST = "GetAllDevicesRequest";
    public static final String E_GET_ALL_DEVICES_RESPONSE = "GetAllDevicesResponse";

    public static final String E_DELETE_DEVICE_REQUEST = "DeleteDeviceRequest";
    public static final String E_DELETE_DEVICE_RESPONSE = "DeleteDeviceResponse";

    public static final String E_DOCUMENT_ACTION_REQUEST = "DocumentActionRequest";
    public static final String E_DOCUMENT_ACTION_RESPONSE = "DocumentActionResponse";

    public static final String E_GET_WATCHERS_REQUEST = "GetWatchersRequest";
    public static final String E_GET_WATCHERS_RESPONSE = "GetWatchersResponse";

    public static final String E_GET_WATCHING_ITEMS_REQUEST = "GetWatchingItemsRequest";
    public static final String E_GET_WATCHING_ITEMS_RESPONSE = "GetWatchingItemsResponse";

    public static final String E_GET_NOTIFICATIONS_REQUEST = "GetNotificationsRequest";
    public static final String E_GET_NOTIFICATIONS_RESPONSE = "GetNotificationsResponse";

    public static final String E_GET_DOCUMENT_SHARE_URL_REQUEST = "GetDocumentShareURLRequest";
    public static final String E_GET_DOCUMENT_SHARE_URL_RESPONSE = "GetDocumentShareURLResponse";

    public static final String E_GET_SHARE_DETAILS_REQUEST = "GetShareDetailsRequest";
    public static final String E_GET_SHARE_DETAILS_RESPONSE = "GetShareDetailsResponse";

    public static final QName GET_ACTIVITY_STREAM_REQUEST = QName.get(E_GET_ACTIVITY_STREAM_REQUEST, NAMESPACE);
    public static final QName GET_ACTIVITY_STREAM_RESPONSE = QName.get(E_GET_ACTIVITY_STREAM_RESPONSE, NAMESPACE);

    public static final QName REGISTER_DEVICE_REQUEST = QName.get(E_REGISTER_DEVICE_REQUEST, NAMESPACE);
    public static final QName REGISTER_DEVICE_RESPONSE = QName.get(E_REGISTER_DEVICE_RESPONSE, NAMESPACE);

    public static final QName UPDATE_DEVICE_STATUS_REQUEST = QName.get(E_UPDATE_DEVICE_STATUS_REQUEST, NAMESPACE);
    public static final QName UPDATE_DEVICE_STATUS_RESPONSE = QName.get(E_UPDATE_DEVICE_STATUS_RESPONSE, NAMESPACE);

    public static final QName CHECK_DEVICE_STATUS_REQUEST = QName.get(E_CHECK_DEVICE_STATUS_REQUEST, NAMESPACE);
    public static final QName CHECK_DEVICE_STATUS_RESPONSE = QName.get(E_CHECK_DEVICE_STATUS_RESPONSE, NAMESPACE);

    public static final QName GET_ALL_DEVICES_REQUEST = QName.get(E_GET_ALL_DEVICES_REQUEST, NAMESPACE);
    public static final QName GET_ALL_DEVICES_RESPONSE = QName.get(E_GET_ALL_DEVICES_RESPONSE, NAMESPACE);

    public static final QName DELETE_DEVICE_REQUEST = QName.get(E_DELETE_DEVICE_REQUEST, NAMESPACE);
    public static final QName DELETE_DEVICE_RESPONSE = QName.get(E_DELETE_DEVICE_RESPONSE, NAMESPACE);

    public static final QName DOCUMENT_ACTION_REQUEST = QName.get(E_DOCUMENT_ACTION_REQUEST, NAMESPACE);
    public static final QName DOCUMENT_ACTION_RESPONSE = QName.get(E_DOCUMENT_ACTION_RESPONSE, NAMESPACE);

    public static final QName GET_WATCHERS_REQUEST = QName.get(E_GET_WATCHERS_REQUEST, NAMESPACE);
    public static final QName GET_WATCHERS_RESPONSE = QName.get(E_GET_WATCHERS_RESPONSE, NAMESPACE);

    public static final QName GET_WATCHING_ITEMS_REQUEST = QName.get(E_GET_WATCHING_ITEMS_REQUEST, NAMESPACE);
    public static final QName GET_WATCHING_ITEMS_RESPONSE = QName.get(E_GET_WATCHING_ITEMS_RESPONSE, NAMESPACE);

    public static final QName GET_NOTIFICATIONS_REQUEST = QName.get(E_GET_NOTIFICATIONS_REQUEST, NAMESPACE);
    public static final QName GET_NOTIFICATIONS_RESPONSE = QName.get(E_GET_NOTIFICATIONS_RESPONSE, NAMESPACE);

    public static final QName GET_DOCUMENT_SHARE_URL_REQUEST = QName.get(E_GET_DOCUMENT_SHARE_URL_REQUEST, NAMESPACE);
    public static final QName GET_DOCUMENT_SHARE_URL_RESPONSE = QName.get(E_GET_DOCUMENT_SHARE_URL_RESPONSE, NAMESPACE);

    public static final QName GET_SHARE_DETAILS_REQUEST = QName.get(E_GET_SHARE_DETAILS_REQUEST, NAMESPACE);
    public static final QName GET_SHARE_DETAILS_RESPONSE = QName.get(E_GET_SHARE_DETAILS_RESPONSE, NAMESPACE);

    public static final String E_OPERATION = "op";
    public static final String A_MARKSEEN = "markSeen";
    public static final String A_LASTSEEN = "lastSeen";
    public static final String E_NOTIFICATIONS = "notifications";
    public static final String E_FILES = "files";
    public static final String E_FILE = "file";
}
