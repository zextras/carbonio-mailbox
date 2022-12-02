// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.account.type;

import java.util.List;

import com.google.common.base.Joiner;
import com.google.common.base.MoreObjects;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlValue;

import com.zimbra.common.soap.AccountConstants;
import com.zimbra.soap.type.SourceLookupOpt;
import com.zimbra.soap.type.StoreLookupOpt;

@XmlAccessorType(XmlAccessType.NONE)
public class SMIMEPublicCertsStoreSpec {
    private static Joiner COMMA_JOINER = Joiner.on(",");

    /**
     * @zm-api-field-description Lookup option related to stores.
     * <ul>
     * <li> <b>ANY</b> (default) : While iterating through stores, stop if any certs are found in a store and just
     *                             return those certs - remaining stores will not be attempted.
     * <li> <b>ALL</b>: Always iterate through all specified stores.
     * </ul>
     */
    @XmlAttribute(name=AccountConstants.A_SMIME_STORE_LOOKUP_OPT /* storeLookupOpt */, required=false)
    private StoreLookupOpt storeLookupOpt;

    /**
     * @zm-api-field-description Lookup option related to sources configured for stores.
     * <ul>
     * <li> <b>ANY</b> : While iterating through multiple sources configured for a store, stop if any certificates
     *                   are found in one source - remaining configured sources will not be attempted.
     * <li> <b>ALL</b> (default) : Always iterate through all configured sources.
     * </ul>
     * Note: this only applies to the <b>LDAP</b> store.
     */
    @XmlAttribute(name=AccountConstants.A_SMIME_SOURCE_LOOKUP_OPT /* sourceLookupOpt */, required=false)
    private SourceLookupOpt sourceLookupOpt;

    public SMIMEPublicCertsStoreSpec() {
    }

    public void setStoreLookupOpt(StoreLookupOpt storeLookupOpt) {
        this.storeLookupOpt = storeLookupOpt;
    }
    public void setSourceLookupOpt(SourceLookupOpt sourceLookupOpt) {
        this.sourceLookupOpt = sourceLookupOpt;
    }

    public StoreLookupOpt getStoreLookupOpt() { return storeLookupOpt; }
    public SourceLookupOpt getSourceLookupOpt() { return sourceLookupOpt; }

    private List<String> storeTypes = Lists.newArrayList();

    /**
     * @zm-api-field-description Comma separated list of store types
     * <br />
     * Valid store types:
     * <ol>
     * <li><b>CONTACT</b> - contacts
     * <li><b>GAL</b> - Global Address List (internal and external)
     * <li><b>LDAP</b> - external LDAP (see GetSMIMEConfig and ModifySMIMEConfig)
     * </ol>
     */
    @XmlValue
    public String getStoreTypes() {
        return COMMA_JOINER.join(storeTypes);
    }

    public void addStoreType(String storeType) {
        this.storeTypes.add(storeType);
    }

    public void addStoreTypes(Iterable<String> storeTypes) {
        if (storeTypes != null) {
            Iterables.addAll(this.storeTypes, storeTypes);
        }
    }

    public MoreObjects.ToStringHelper addToStringInfo(
                MoreObjects.ToStringHelper helper) {
        return helper
            .add("storeLookupOpt", storeLookupOpt)
            .add("sourceLookupOpt", sourceLookupOpt)
            .add("storeTypes", storeTypes);
    }

    @Override
    public String toString() {
        return addToStringInfo(MoreObjects.toStringHelper(this))
                .toString();
    }
}
