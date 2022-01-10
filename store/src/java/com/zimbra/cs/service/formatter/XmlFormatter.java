// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.service.formatter;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.Element;
import com.zimbra.cs.mailbox.CalendarItem;
import com.zimbra.cs.mailbox.MailItem;
import com.zimbra.cs.mailbox.CalendarItem.Instance;
import com.zimbra.cs.service.UserServletContext;
import com.zimbra.cs.service.formatter.FormatterFactory.FormatType;
import com.zimbra.cs.service.mail.ToXML;
import com.zimbra.cs.service.util.ItemIdFormatter;

public class XmlFormatter extends Formatter {

    @Override
    public FormatType getType() {
        return FormatType.XML;
    }

    @Override
    public Set<MailItem.Type> getDefaultSearchTypes() {
         return SEARCH_FOR_EVERYTHING;
    }

    @Override
    public void formatCallback(UserServletContext context) throws ServiceException, IOException {
        Element elt = getFactory().createElement("items");
        ItemIdFormatter ifmt = new ItemIdFormatter(context.getAuthAccount(), context.targetMailbox, false);

        Iterator<? extends MailItem> iterator = null;
        try {
            long start = context.getStartTime();
            long end = context.getEndTime();
            boolean hasTimeRange = start != TIME_UNSPECIFIED && end != TIME_UNSPECIFIED;
            iterator = getMailItems(context, start, end, Integer.MAX_VALUE);
            // this is lame
            while (iterator.hasNext()) {
                MailItem item = iterator.next();
                if (item instanceof CalendarItem && hasTimeRange) {
                    // Skip appointments that have no instance in the time range.
                    CalendarItem calItem = (CalendarItem) item;
                    Collection<Instance> instances = calItem.expandInstances(start, end, false);
                    if (instances.isEmpty())
                        continue;
                }
                ToXML.encodeItem(elt, ifmt, context.opContext, item, ToXML.NOTIFY_FIELDS);
            }

            context.resp.getOutputStream().write(elt.toUTF8());
        } finally {
            if (iterator instanceof QueryResultIterator)
                ((QueryResultIterator) iterator).finished();
        }
    }

    Element.ElementFactory getFactory() {
        return Element.XMLElement.mFactory;
    }
}
