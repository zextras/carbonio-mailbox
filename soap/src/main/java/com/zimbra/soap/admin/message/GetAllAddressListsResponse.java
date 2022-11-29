// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.message;

import java.util.Collections;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.zimbra.common.soap.AccountConstants;
import com.zimbra.common.soap.AdminConstants;
import com.zimbra.soap.account.type.AddressListInfo;
import com.zimbra.soap.json.jackson.annotate.ZimbraJsonArrayForWrapper;

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name=AdminConstants.E_GET_ALL_ADDRESS_LISTS_RESPONSE)
public class GetAllAddressListsResponse {

    /**
     * @zm-api-field-description Information about address lists
     */
    @ZimbraJsonArrayForWrapper
    @XmlElementWrapper(name=AccountConstants.E_ADDRESS_LISTS /* addressLists */, required=false)
    @XmlElement(name=AccountConstants.E_ADDRESS_LIST /* addressList */, required=false)
    private List<AddressListInfo> addressLists = Lists.newArrayList();

    public GetAllAddressListsResponse() {
    }

    public void setAddressLists(Iterable <AddressListInfo> addressLists) {
        this.addressLists.clear();
        if (addressLists != null) {
            Iterables.addAll(this.addressLists, addressLists);
        }
    }

    public GetAllAddressListsResponse addAddressList(AddressListInfo addressList) {
        this.addressLists.add(addressList);
        return this;
    }

    public List<AddressListInfo> getAddressLists() {
        return Collections.unmodifiableList(addressLists);
    }
}
