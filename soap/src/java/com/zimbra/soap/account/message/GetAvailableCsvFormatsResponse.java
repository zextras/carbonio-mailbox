// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.account.message;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import java.util.Collections;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.zimbra.common.soap.AccountConstants;
import com.zimbra.soap.type.NamedElement;

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name=AccountConstants.E_GET_AVAILABLE_CSV_FORMATS_RESPONSE)
public class GetAvailableCsvFormatsResponse {

    /**
     * @zm-api-field-description The known CSV formats that can be used for import and export of addressbook.
     */
    @XmlElement(name=AccountConstants.E_CSV, required=false)
    private List<NamedElement> csvFormats = Lists.newArrayList();

    public GetAvailableCsvFormatsResponse() {
    }

    public void setCsvFormats(Iterable <NamedElement> csvFormats) {
        this.csvFormats.clear();
        if (csvFormats != null) {
            Iterables.addAll(this.csvFormats,csvFormats);
        }
    }

    public GetAvailableCsvFormatsResponse addCsvFormat(NamedElement csvFormat) {
        this.csvFormats.add(csvFormat);
        return this;
    }

    public List<NamedElement> getCsvFormats() {
        return Collections.unmodifiableList(csvFormats);
    }
}
