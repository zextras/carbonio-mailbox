// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.mail.message;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import com.zimbra.common.soap.MailConstants;
import com.zimbra.soap.mail.type.MailSearchParams;
import com.zimbra.soap.type.ZmBoolean;

/**
 * @zm-api-command-auth-required true
 * @zm-api-command-admin-auth-required false
 * @zm-api-command-description Search
 * <br />
 * For a response, the order of the returned results represents the sorted order.  There is not a separate index
 * attribute or element.
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name=MailConstants.E_SEARCH_REQUEST)
public final class SearchRequest extends MailSearchParams {

    /**
     * @zm-api-field-description Warmup: When this option is specified, all other options are simply ignored, so you
     * can't include this option in regular search requests. This option gives a hint to the index system to open the
     * index data and primes it for search. The client should send this warm-up request as soon as the user puts the
     * cursor on the search bar. This will not only prime the index but also opens a persistent HTTP connection
     * (HTTP 1.1 Keep-Alive) to the server, hence smaller latencies in subseqent search requests. Sending this warm-up
     * request too early (e.g. login time) will be in vain in most cases because the index data is evicted from the
     * cache due to inactivity timeout by the time you actually send a search request.
     */
    @XmlAttribute(name=MailConstants.A_WARMUP /* warmup */, required=false)
    private ZmBoolean warmup;

    public void setWarmup(Boolean warmup) { this.warmup = ZmBoolean.fromBool(warmup); }
    public Boolean getWarmup() { return ZmBoolean.toBool(warmup); }
}
