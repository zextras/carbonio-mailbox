// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.index;

import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.MailConstants;
import com.zimbra.common.soap.Element;

/**
 * A {@link ZimbraHit} which is being proxied from another server: i.e. we did a SOAP request somewhere else and are now
 * wrapping results we got from request.
 */
public final class ProxiedContactHit extends ProxiedHit  {

    /**
     * @param sortValue - typically A_FILE_AS_STR rather than A_SORT_FIELD (the value for general ProxiedHits)
     */
    public ProxiedContactHit(ZimbraQueryResultsImpl results, Element elt, String sortValue) {
        super(results, elt, sortValue);
    }

    @Override
    String getName() throws ServiceException {
        return super.getElement().getAttribute(MailConstants.A_FILE_AS_STR);
    }
}
