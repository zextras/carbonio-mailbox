// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.mailbox;

import com.google.common.base.Strings;
import com.zextras.mailbox.util.TestConfig;
import com.zimbra.common.calendar.WellKnownTimeZones;
import com.zimbra.common.calendar.ZCalendar.ZVCalendar;
import com.zimbra.common.localconfig.LC;
import com.zimbra.common.mime.ContentDisposition;
import com.zimbra.common.mime.MimeConstants;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.util.StringUtil;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.common.zmime.ZMimeBodyPart;
import com.zimbra.common.zmime.ZMimeMultipart;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.MockProvisioning;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.db.DbPool;
import com.zimbra.cs.db.HSQLDB;
import com.zimbra.cs.ephemeral.EphemeralStore;
import com.zimbra.cs.ephemeral.InMemoryEphemeralStore;
import com.zimbra.cs.ephemeral.migrate.InMemoryMigrationInfo;
import com.zimbra.cs.ephemeral.migrate.MigrationInfo;
import com.zimbra.cs.index.IndexStore;
import com.zimbra.cs.index.elasticsearch.ElasticSearchConnector;
import com.zimbra.cs.index.elasticsearch.ElasticSearchIndex;
import com.zimbra.cs.mailbox.calendar.Invite;
import com.zimbra.cs.mime.Mime;
import com.zimbra.cs.mime.ParsedMessage;
import com.zimbra.cs.store.MockStoreManager;
import com.zimbra.cs.store.StoreManager;
import com.zimbra.cs.store.http.HttpStoreManagerTest.MockHttpStoreManager;
import com.zimbra.cs.store.http.MockHttpStore;
import com.zimbra.cs.util.JMSession;
import com.zimbra.soap.DocumentHandler;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.MessagingException;
import javax.mail.Part;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;
import org.apache.commons.io.FileUtils;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpRequestBase;
import org.mockito.Mockito;

public final class MailboxTestUtil {

  private MailboxTestUtil() {}

  public static void index(Mailbox mbox) throws ServiceException {
    mbox.index.indexDeferredItems();
  }

  public static void setFlag(Mailbox mbox, int itemId, Flag.FlagInfo flag) throws ServiceException {
    MailItem item = mbox.getItemById(null, itemId, MailItem.Type.UNKNOWN);
    int flags = item.getFlagBitmask() | flag.toBitmask();
    mbox.setTags(null, itemId, item.getType(), flags, null, null);
  }

  public static ParsedMessage generateMessage(String subject) throws Exception {
    MimeMessage mm = new Mime.FixedMimeMessage(JMSession.getSession());
    mm.setHeader("From", "Bob Evans <bob@example.com>");
    mm.setHeader("To", "Jimmy Dean <jdean@example.com>");
    mm.setHeader("Subject", subject);
    mm.setText("nothing to see here");
    return new ParsedMessage(mm, false);
  }

  public static ParsedMessage generateHighPriorityMessage(String subject) throws Exception {
    MimeMessage mm = new Mime.FixedMimeMessage(JMSession.getSession());
    mm.setHeader("From", "Hi Bob <bob@example.com>");
    mm.setHeader("To", "Jimmy Dean <jdean@example.com>");
    mm.setHeader("Subject", subject);
    mm.addHeader("Importance", "high");
    mm.setText("nothing to see here");
    return new ParsedMessage(mm, false);
  }

  public static ParsedMessage generateLowPriorityMessage(String subject) throws Exception {
    MimeMessage mm = new Mime.FixedMimeMessage(JMSession.getSession());
    mm.setHeader("From", "Lo Bob <bob@example.com>");
    mm.setHeader("To", "Jimmy Dean <jdean@example.com>");
    mm.setHeader("Subject", subject);
    mm.addHeader("Importance", "low");
    mm.setText("nothing to see here");
    return new ParsedMessage(mm, false);
  }

  public static ParsedMessage generateMessageWithAttachment(String subject) throws Exception {
    MimeMessage mm = new Mime.FixedMimeMessage(JMSession.getSession());
    mm.setHeader("From", "Vera Oliphant <oli@example.com>");
    mm.setHeader("To", "Jimmy Dean <jdean@example.com>");
    mm.setHeader("Subject", subject);
    mm.setText("Good as gold");
    MimeMultipart multi = new ZMimeMultipart("mixed");
    ContentDisposition cdisp = new ContentDisposition(Part.ATTACHMENT);
    cdisp.setParameter("filename", "fun.txt");

    ZMimeBodyPart bp = new ZMimeBodyPart();
    // MimeBodyPart.setDataHandler() invalidates Content-Type and CTE if there is any, so make sure
    // it gets called before setting Content-Type and CTE headers.
    try {
      bp.setDataHandler(
          new DataHandler(new ByteArrayDataSource("Feeling attached.", "text/plain")));
    } catch (IOException e) {
      throw new MessagingException("could not generate mime part content", e);
    }
    bp.addHeader("Content-Disposition", cdisp.toString());
    bp.addHeader("Content-Type", "text/plain");
    bp.addHeader("Content-Transfer-Encoding", MimeConstants.ET_8BIT);
    multi.addBodyPart(bp);

    mm.setContent(multi);
    mm.saveChanges();

    return new ParsedMessage(mm, false);
  }

  public static ParsedMessage generateMessageWithXmlAttachment(String subject) throws Exception {
    MimeMessage mm = new Mime.FixedMimeMessage(JMSession.getSession());
    mm.setHeader("From", "Vera Oliphant <oli@example.com>");
    mm.setHeader("To", "Jimmy Dean <jdean@example.com>");
    mm.setHeader("Subject", subject);
    mm.setText("Good as gold");

    MimeMultipart multi = new ZMimeMultipart("mixed");
    ContentDisposition cdisp = new ContentDisposition(Part.ATTACHMENT);
    cdisp.setParameter("filename", "data.xml");

    ZMimeBodyPart bp = new ZMimeBodyPart();
    // MimeBodyPart.setDataHandler() invalidates Content-Type and CTE if there is any, so make sure
    // it gets called before setting Content-Type and CTE headers.
    try {
      String xmlContent = "<root><message>Feeling attached.</message></root>";
      DataSource ds = new ByteArrayDataSource(xmlContent, "text/xml");
      bp.setDataHandler(new DataHandler(ds));
    } catch (IOException e) {
      throw new MessagingException("could not generate mime part content", e);
    }
    bp.addHeader("Content-Disposition", cdisp.toString());
    bp.addHeader("Content-Type", "text/xml");
    bp.addHeader("Content-Transfer-Encoding", MimeConstants.ET_8BIT);
    multi.addBodyPart(bp);

    mm.setContent(multi);
    mm.saveChanges();

    return new ParsedMessage(mm, false);
  }

  public static Invite generateInvite(Account account, String fragment, ZVCalendar cals)
      throws Exception {

    List<Invite> invites = Invite.createFromCalendar(account, fragment, cals, true);

    return invites.get(0);
  }
}
