// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.common.soap;

import org.dom4j.Namespace;
import org.dom4j.QName;

public final class ClusterConstants {
    public static final String NAMESPACE_STR = AdminConstants.NAMESPACE_STR;
    public static final Namespace NAMESPACE = Namespace.get(NAMESPACE_STR);

    public static final String E_GET_CLUSTER_STATUS_REQUEST = "GetClusterStatusRequest";
    public static final String E_GET_CLUSTER_STATUS_RESPONSE = "GetClusterStatusResponse";
    public static final String E_FAILOVER_CLUSTER_SERVICE_REQUEST = "FailoverClusterServiceRequest";
    public static final String E_FAILOVER_CLUSTER_SERVICE_RESPONSE = "FailoverClusterServiceResponse";

    public static final QName GET_CLUSTER_STATUS_REQUEST = QName.get(E_GET_CLUSTER_STATUS_REQUEST, NAMESPACE);
    public static final QName GET_CLUSTER_STATUS_RESPONSE = QName.get(E_GET_CLUSTER_STATUS_RESPONSE, NAMESPACE);
    public static final QName FAILOVER_CLUSTER_SERVICE_REQUEST = QName.get(E_FAILOVER_CLUSTER_SERVICE_REQUEST, NAMESPACE);
    public static final QName FAILOVER_CLUSTER_SERVICE_RESPONSE = QName.get(E_FAILOVER_CLUSTER_SERVICE_RESPONSE, NAMESPACE);

    public static final String A_CLUSTER_SERVERS = "servers";
    public static final String A_CLUSTER_SERVER = "server";
    public static final String A_CLUSTER_SERVICES = "services";
    public static final String A_CLUSTER_SERVICE = "service";
    public static final String A_CLUSTER_SERVER_NAME = "name";
    public static final String A_CLUSTER_SERVER_STATUS = "status";
    public static final String A_CLUSTER_SERVICE_NAME = "name";
    public static final String A_CLUSTER_SERVICE_STATE = "state";
    public static final String A_CLUSTER_SERVICE_OWNER = "owner";
    public static final String A_CLUSTER_SERVICE_LAST_OWNER = "lastOwner";
    public static final String E_CLUSTER_NAME = "clusterName";

    public static final String A_FAILOVER_NEW_SERVER = "newServer";

}
