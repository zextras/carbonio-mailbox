// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.purge;

import java.util.ArrayList;
import java.util.List;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.DataSource;
import com.zimbra.cs.mailbox.Mailbox;

public class PurgeFromIncomingDataSource extends DataSourcePurge {

    public PurgeFromIncomingDataSource(Mailbox mbox) {
        super(mbox);
    }

    @Override
    List<DataSource> getPurgeableDataSources(DataSource incoming) throws ServiceException {
        List<DataSource> sources = new ArrayList<DataSource>(1);
        sources.add(incoming);
        return sources;
    }

}
