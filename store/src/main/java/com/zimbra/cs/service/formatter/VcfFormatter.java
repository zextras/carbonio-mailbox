// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.service.formatter;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.mail.Part;
import javax.servlet.http.HttpServletResponse;

import com.zimbra.common.mime.MimeConstants;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.util.HttpUtil;
import com.zimbra.cs.mailbox.Contact;
import com.zimbra.cs.mailbox.Folder;
import com.zimbra.cs.mailbox.MailItem;
import com.zimbra.cs.service.UserServletContext;
import com.zimbra.cs.service.UserServletException;
import com.zimbra.cs.service.formatter.FormatterFactory.FormatType;

public class VcfFormatter extends Formatter {

    @Override
    public FormatType getType() {
        return FormatType.VCF;
    }

    @Override
    public String[] getDefaultMimeTypes() {
        return new String[] {
                MimeConstants.CT_TEXT_VCARD,
                MimeConstants.CT_TEXT_VCARD_LEGACY,
                MimeConstants.CT_TEXT_VCARD_LEGACY2
        };
    }

    @Override
    public Set<MailItem.Type> getDefaultSearchTypes() {
        return EnumSet.of(MailItem.Type.CONTACT);
    }

    @Override
    public void formatCallback(UserServletContext context) throws IOException, ServiceException {
        Charset charset = context.getCharset();
        Iterator<? extends MailItem> iterator = null;
        try {
            iterator = getMailItems(context, getDefaultStartTime(), getDefaultEndTime(), Integer.MAX_VALUE);

            String filename = context.target instanceof Contact ?
                    ((Contact) context.target).getFileAsString() : "contacts";
            String cd = HttpUtil.createContentDisposition(context.req, Part.ATTACHMENT,filename +".vcf");
            context.resp.addHeader("Content-Disposition", cd);
            context.resp.setContentType(MimeConstants.CT_TEXT_VCARD_LEGACY);  // for backward compatibility
            context.resp.setCharacterEncoding(charset.name());

            int count = 0;
            while (iterator.hasNext()) {
                MailItem item = iterator.next();
                if (!(item instanceof Contact))
                    continue;
                VCard vcf = VCard.formatContact((Contact) item);
                context.resp.getOutputStream().write(vcf.getFormatted().getBytes(charset));
                count++;
            }
        } finally {
            if (iterator instanceof QueryResultIterator)
                ((QueryResultIterator) iterator).finished();
        }
    }

    @Override
    public boolean supportsSave() {
        return true;
    }

    @Override
    public void saveCallback(UserServletContext context, String contentType, Folder folder, String filename)
        throws ServiceException, IOException, UserServletException {

        byte[] body = context.getPostBody();
        List<VCard> cards = VCard.parseVCard(new String(body, context.getCharset()));

        if (cards == null || cards.size() == 0 ||
                (cards.size() == 1 && cards.get(0).fields.isEmpty())) {
            throw new UserServletException(HttpServletResponse.SC_BAD_REQUEST,
                    "no contact fields found in vcard");
        }

        for (VCard vcf : cards) {
            if (vcf.fields.isEmpty())
                continue;
            folder.getMailbox().createContact(context.opContext, vcf.asParsedContact(), folder.getId(), null);
        }
    }
}
