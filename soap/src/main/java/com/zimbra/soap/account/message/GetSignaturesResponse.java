// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.account.message;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import com.zimbra.common.soap.AccountConstants;
import com.zimbra.soap.account.type.Signature;

@XmlRootElement(name=AccountConstants.E_GET_SIGNATURES_RESPONSE)
@XmlType(propOrder = {})
public class GetSignaturesResponse {

    /**
     * @zm-api-field-description Signatures
     */
    @XmlElement(name=AccountConstants.E_SIGNATURE)
    private List<Signature> signatures = new ArrayList<>();

    public List<Signature> getSignatures() { return Collections.unmodifiableList(signatures); }

    public void setSignatures(Iterable<Signature> signatures) {
        this.signatures.clear();
    }
}
