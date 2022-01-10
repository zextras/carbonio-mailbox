// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.type;

import java.util.Arrays;

import javax.xml.bind.annotation.XmlEnum;

import com.zimbra.common.mailbox.ZimbraSortBy;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.zclient.ZClientException;

@XmlEnum
public enum SearchSortBy {
    // case must match protocol
    dateDesc(ZimbraSortBy.dateDesc),
    dateAsc(ZimbraSortBy.dateAsc),
    idDesc(ZimbraSortBy.idDesc),
    idAsc(ZimbraSortBy.idAsc),
    subjDesc(ZimbraSortBy.subjDesc),
    subjAsc(ZimbraSortBy.subjAsc),
    nameDesc(ZimbraSortBy.nameDesc),
    nameAsc(ZimbraSortBy.nameAsc),
    durDesc(ZimbraSortBy.durDesc),
    durAsc(ZimbraSortBy.durAsc),
    none(ZimbraSortBy.none),
    taskDueAsc(ZimbraSortBy.taskDueAsc),
    taskDueDesc(ZimbraSortBy.taskDueDesc),
    taskStatusAsc(ZimbraSortBy.taskStatusAsc),
    taskStatusDesc(ZimbraSortBy.taskStatusDesc),
    taskPercCompletedAsc(ZimbraSortBy.taskPercCompletedAsc),
    taskPercCompletedDesc(ZimbraSortBy.taskPercCompletedDesc),
    rcptAsc(ZimbraSortBy.rcptAsc),
    rcptDesc(ZimbraSortBy.rcptDesc),
    readAsc(ZimbraSortBy.readAsc),
    readDesc(ZimbraSortBy.readDesc);

    private ZimbraSortBy zsb;

    private SearchSortBy(ZimbraSortBy zsb) {
        this.zsb = zsb;
    }

    public ZimbraSortBy toZimbraSortBy() {
        return zsb;
    }

    public static SearchSortBy fromZimbraSortBy(ZimbraSortBy zsb) {
        if (zsb == null) {
            return null;
        }
        for (SearchSortBy val :SearchSortBy.values()) {
            if (val.zsb == zsb) {
                return val;
            }
        }
        throw new IllegalArgumentException("Unrecognised ZimbraSortBy:" + zsb);
    }

    public static SearchSortBy fromString(String s)
    throws ServiceException {
        try {
            return SearchSortBy.valueOf(s);
        } catch (IllegalArgumentException e) {
            throw ZClientException.CLIENT_ERROR("unknown 'sortBy' key: " + s + ", valid values: " +
                   Arrays.asList(SearchSortBy.values()), null);
        }
    }
}
