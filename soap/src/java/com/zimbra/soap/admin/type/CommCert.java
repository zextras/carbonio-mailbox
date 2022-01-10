// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.type;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import java.util.Collections;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

import com.zimbra.common.soap.CertMgrConstants;

@XmlAccessorType(XmlAccessType.NONE)
public class CommCert {

    /**
     * @zm-api-field-description Certificate information
     */
    @XmlElement(name=CertMgrConstants.E_cert /* cert */, required=false)
    private AidAndFilename cert;

    /**
     * @zm-api-field-description rootCA information
     */
    @XmlElement(name=CertMgrConstants.E_rootCA /* rootCA */, required=false)
    private AidAndFilename rootCA;

    /**
     * @zm-api-field-description intermediateCA information
     */
    @XmlElement(name=CertMgrConstants.E_intermediateCA /* intermediateCA */, required=false)
    private List<AidAndFilename> intermediateCAs = Lists.newArrayList();

    public CommCert() {
    }

    public void setCert(AidAndFilename cert) { this.cert = cert; }
    public void setRootCA(AidAndFilename rootCA) { this.rootCA = rootCA; }
    public void setIntermediateCAs(Iterable <AidAndFilename> intermediateCAs) {
        this.intermediateCAs.clear();
        if (intermediateCAs != null) {
            Iterables.addAll(this.intermediateCAs,intermediateCAs);
        }
    }

    public void addIntermediateCA(AidAndFilename intermediateCA) {
        this.intermediateCAs.add(intermediateCA);
    }

    public AidAndFilename getCert() { return cert; }
    public AidAndFilename getRootCA() { return rootCA; }
    public List<AidAndFilename> getIntermediateCAs() {
        return Collections.unmodifiableList(intermediateCAs);
    }

    public MoreObjects.ToStringHelper addToStringInfo(
                MoreObjects.ToStringHelper helper) {
        return helper
            .add("cert", cert)
            .add("rootCA", rootCA)
            .add("intermediateCAs", intermediateCAs);
    }

    @Override
    public String toString() {
        return addToStringInfo(MoreObjects.toStringHelper(this))
                .toString();
    }
}
