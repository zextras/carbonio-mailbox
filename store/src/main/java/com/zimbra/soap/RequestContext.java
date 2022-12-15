// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap;



/**
 * @author zimbra
 *
 */
public class RequestContext {


    /**
     * @return the reqHost
     */
    public String getVirtualHost() {
        return virtualHost;
    }


    /**
     * @param reqHost the reqHost to set
     */
    public void setVirtualHost(String reqHost) {
        this.virtualHost = reqHost;
    }

    private String virtualHost;

}
