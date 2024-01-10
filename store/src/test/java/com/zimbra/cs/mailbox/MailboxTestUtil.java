// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.mailbox;

import com.google.common.base.Strings;
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
import java.util.List;
import javax.activation.DataHandler;
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

  /** Initializes the provisioning. */
  public static void initProvisioning() throws Exception {
    initProvisioning("");
  }

  /**
   * Initializes the provisioning.
   *
   * @param zimbraServerDir the directory that contains the ZimbraServer project
   * @throws Exception
   */
  public static void initProvisioning(String zimbraServerDir) throws Exception {
    zimbraServerDir = getZimbraServerDir(zimbraServerDir);
    System.setProperty("log4j.configuration", "log4j-test.properties");
    System.setProperty(
        "zimbra.config", zimbraServerDir + "src/test/resources/localconfig-test.xml");
    LC.reload();
    // substitute test TZ file
    String timezonefilePath = zimbraServerDir + "src/test/resources/timezones-test.ics";
    File d = new File(timezonefilePath);
    if (!d.exists()) {
      throw new FileNotFoundException("timezones-test.ics not found in " + timezonefilePath);
    }
    LC.timezone_file.setDefault(timezonefilePath);
    WellKnownTimeZones.loadFromFile(d);
    LC.zimbra_rights_directory.setDefault("");
    LC.zimbra_attrs_directory.setDefault(zimbraServerDir + "conf/attrs");
    LC.zimbra_tmp_directory.setDefault(zimbraServerDir + "tmp");
    // substitute test DS config file
    String dsfilePath = zimbraServerDir + "src/test/resources/datasource-test.xml";
    d = new File(dsfilePath);
    if (!d.exists()) {
      throw new FileNotFoundException("datasource-test.xml not found in " + dsfilePath);
    }
    LC.data_source_config.setDefault(dsfilePath);
    // default MIME handlers are now set up in MockProvisioning constructor
    Provisioning.setInstance(new MockProvisioning());
  }

  /**
   * This method will set the provisioning singleton as {@link Mockito#mock} of {@link Provisioning}
   */
  public static void setMockitoProvisioning() {
    Provisioning.setInstance(Mockito.mock(Provisioning.class));
  }

  public static String getZimbraServerDir(String zimbraServerDir) {
    String serverDir = zimbraServerDir;
    if (StringUtil.isNullOrEmpty(serverDir)) {
      serverDir = Strings.nullToEmpty(System.getProperty("server.dir"));
      if (serverDir.isEmpty()) {
        serverDir = Strings.nullToEmpty(System.getProperty("user.dir"));
      }
    }
    if (!serverDir.endsWith("/")) {
      serverDir = serverDir + "/";
    }
    return serverDir;
  }

  /** Initializes the provisioning, database, index and store manager. */
  public static void initServer() throws Exception {
    initServer(MockStoreManager.class);
  }

  /**
   * Initializes the provisioning, database, index and store manager.
   *
   * @param zimbraServerDir the directory that contains the ZimbraServer project
   * @throws Exception
   */
  public static void initServer(String zimbraServerDir) throws Exception {
    initServer(MockStoreManager.class, zimbraServerDir);
  }

  public static void initServer(Class<? extends StoreManager> storeManagerClass) throws Exception {
    initServer(storeManagerClass, "");
  }

  public static void initServer(
      Class<? extends StoreManager> storeManagerClass, String zimbraServerDir) throws Exception {
    MigrationInfo.setFactory(InMemoryMigrationInfo.Factory.class);
    EphemeralStore.setFactory(InMemoryEphemeralStore.Factory.class);
    initProvisioning(zimbraServerDir);

    LC.zimbra_class_database.setDefault(HSQLDB.class.getName());
    DbPool.startup();
    HSQLDB.createDatabase(zimbraServerDir);

    MailboxManager.setInstance(null);
    IndexStore.setFactory(LC.zimbra_class_index_store_factory.value());

    LC.zimbra_class_store.setDefault(storeManagerClass.getName());
    StoreManager.getInstance().startup();
  }

  /** Clears the database and index. */
  public static void clearData() throws Exception {
    clearData("");
    MailboxManager.getInstance().clearAdditionalQuotaProviders();
  }

  /**
   * Clears the database and index.
   *
   * @param zimbraServerDir the directory that contains the ZimbraServer project
   */
  public static void clearData(String zimbraServerDir) throws Exception {
    HSQLDB.clearDatabase(zimbraServerDir);
    MailboxManager.getInstance().clearCache();
    MailboxIndex.shutdown();
    File index = new File("build/test/index");
    if (index.isDirectory()) {
      deleteDirContents(index);
    }
    StoreManager sm = StoreManager.getInstance();
    if (sm instanceof MockStoreManager) {
      ((MockStoreManager) sm).purge();
    } else if (sm instanceof MockHttpStoreManager) {
      MockHttpStore.purge();
    }
    DocumentHandler.resetLocalHost();
    EphemeralStore.getFactory().shutdown();
  }

  private static void deleteDirContents(File dir) throws IOException {
    deleteDirContents(dir, 0);
  }

  private static void deleteDirContents(File dir, int recurCount) throws IOException {
    try {
      FileUtils.deleteDirectory(dir);
    } catch (IOException ioe) {
      if (recurCount > 10) {
        throw new IOException("Gave up after multiple IOExceptions", ioe);
      }
      ZimbraLog.test.info(
          "delete dir='%s' failed due to IOException '%s' (probably files still in use)."
              + "Waiting a moment and trying again",
          dir, ioe.getMessage());
      // wait a moment and try again; this can bomb if files still being written by some thread
      try {
        Thread.sleep(2500);
      } catch (InterruptedException ie) {

      }
      deleteDirContents(dir, recurCount + 1);
    }
  }

  public static void cleanupIndexStore(Mailbox mbox) {
    IndexStore index = mbox.index.getIndexStore();
    if (index instanceof ElasticSearchIndex) {
      String key = mbox.getAccountId();
      String indexUrl = String.format("%s%s/", LC.zimbra_index_elasticsearch_url_base.value(), key);
      HttpRequestBase method = new HttpDelete(indexUrl);
      try {
        ElasticSearchConnector connector = new ElasticSearchConnector();
        int statusCode = connector.executeMethod(method);
        if (statusCode == HttpStatus.SC_OK) {
          boolean ok = connector.getBooleanAtJsonPath(new String[] {"ok"}, false);
          boolean acknowledged =
              connector.getBooleanAtJsonPath(new String[] {"acknowledged"}, false);
          if (!ok || !acknowledged) {
            ZimbraLog.index.debug("Delete index status ok=%b acknowledged=%b", ok, acknowledged);
          }
        } else {
          String error = connector.getStringAtJsonPath(new String[] {"error"});
          if (error != null && error.startsWith("IndexMissingException")) {
            ZimbraLog.index.debug("Unable to delete index for key=%s.  Index is missing", key);
          } else {
            ZimbraLog.index.error("Problem deleting index for key=%s error=%s", key, error);
          }
        }
      } catch (IOException e) {
        ZimbraLog.index.error("Problem Deleting index with key=" + key, e);
      }
    }
  }

  public static void setFlag(Mailbox mbox, int itemId, Flag.FlagInfo flag) throws ServiceException {
    MailItem item = mbox.getItemById(null, itemId, MailItem.Type.UNKNOWN);
    int flags = item.getFlagBitmask() | flag.toBitmask();
    mbox.setTags(null, itemId, item.getType(), flags, null, null);
  }

  public static void unsetFlag(Mailbox mbox, int itemId, Flag.FlagInfo flag)
      throws ServiceException {
    MailItem item = mbox.getItemById(null, itemId, MailItem.Type.UNKNOWN);
    int flags = item.getFlagBitmask() & ~flag.toBitmask();
    mbox.setTags(null, itemId, item.getType(), flags, null, null);
  }

  public static void index(Mailbox mbox) throws ServiceException {
    mbox.index.indexDeferredItems();
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

  public static Invite generateInvite(Account account, String fragment, ZVCalendar cals)
      throws Exception {

    List<Invite> invites = Invite.createFromCalendar(account, fragment, cals, true);

    return invites.get(0);
  }
}
