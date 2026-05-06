// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.account.message;

import java.util.Collections;
import java.util.List;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElementWrapper;
import jakarta.xml.bind.annotation.XmlRootElement;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.zimbra.common.soap.AccountConstants;
import com.zimbra.soap.account.type.AddressListInfo;
import com.zimbra.soap.json.jackson.annotate.ZimbraJsonArrayForWrapper;

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name=AccountConstants.E_GET_ALL_ADDRESS_LISTS_RESPONSE)
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
