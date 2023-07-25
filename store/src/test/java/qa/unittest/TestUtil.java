// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package qa.unittest;


import static org.junit.jupiter.api.Assertions.*;

import com.google.common.collect.Lists;
import com.zimbra.client.ZEmailAddress;
import com.zimbra.client.ZFolder;
import com.zimbra.client.ZGetMessageParams;
import com.zimbra.client.ZMailbox;
import com.zimbra.client.ZMailbox.ZOutgoingMessage;
import com.zimbra.client.ZMailbox.ZOutgoingMessage.MessagePart;
import com.zimbra.client.ZMessage;
import com.zimbra.client.ZMessage.ZMimePart;
import com.zimbra.client.ZSearchHit;
import com.zimbra.client.ZSearchParams;
import com.zimbra.common.mailbox.ContactConstants;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.AccountConstants;
import com.zimbra.common.soap.AdminConstants;
import com.zimbra.common.soap.Element;
import com.zimbra.common.soap.Element.Attribute;
import com.zimbra.common.soap.SoapFaultException;
import com.zimbra.common.soap.SoapTransport;
import com.zimbra.common.util.ByteUtil;
import com.zimbra.common.util.CliUtil;
import com.zimbra.common.util.StringUtil;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.Server;
import com.zimbra.cs.account.soap.SoapProvisioning;
import com.zimbra.cs.db.DbMailItem;
import com.zimbra.cs.db.DbPool;
import com.zimbra.cs.db.DbPool.DbConnection;
import com.zimbra.cs.db.DbUtil;
import com.zimbra.cs.index.SortBy;
import com.zimbra.cs.index.ZimbraHit;
import com.zimbra.cs.index.ZimbraQueryResults;
import com.zimbra.cs.mailbox.Contact;
import com.zimbra.cs.mailbox.DeliveryOptions;
import com.zimbra.cs.mailbox.Flag;
import com.zimbra.cs.mailbox.MailItem;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mailbox.Message;
import com.zimbra.cs.mailbox.OperationContext;
import com.zimbra.cs.mime.ParsedContact;
import com.zimbra.cs.mime.ParsedMessage;
import com.zimbra.cs.service.admin.AccountTestUtil;
import com.zimbra.cs.util.BuildInfo;
import com.zimbra.soap.admin.message.GetAccountRequest;
import com.zimbra.soap.admin.message.GetAccountResponse;
import com.zimbra.soap.admin.type.Attr;
import com.zimbra.soap.type.AccountSelector;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import javax.mail.MessagingException;
import org.junit.jupiter.api.Assertions;


/**
 * @author bburtin
 */
public class TestUtil {
    public static int DEFAULT_WAIT = 200;
    public static final String DEFAULT_PASSWORD = "test123";
    public static boolean fromRunUnitTests = false; /* set if run from within RunUnitTestsRequest */
    private static boolean sIsCliInitialized = false;

    /**
     * @return the <code>Account</code>, or <code>null</code> if account does not exist.
     * @throws ServiceException if name is invalid or can't determine the default domain
     */
    public static Account getAccount(String userName) throws ServiceException {
        return AccountTestUtil.getAccount(userName);
    }

    public static String getDomain() throws ServiceException {
        return AccountTestUtil.getDomain();
    }

    public static String getAddress(String userName) throws ServiceException {
        return AccountTestUtil.getAddress(userName);
    }

    public static String getAddress(String userName, String domainName) {
        return AccountTestUtil.getAddress(userName, domainName);
    }

    public static String getSoapUrl(Server server) {
        try {
            return getBaseUrl(server) + AccountConstants.USER_SERVICE_URI;
        } catch (ServiceException e) {
            ZimbraLog.test.error("Unable to determine SOAP URL", e);
        }
        return null;
    }

    public static String getBaseUrl(Server server) throws ServiceException {
        String scheme;
        Provisioning prov = Provisioning.getInstance();
        if(server == null) {
            server = prov.getLocalServer();
        }
        String hostname = server.getServiceHostname();
        int port;
        port = server.getIntAttr(Provisioning.A_zimbraMailSSLPort, 0);
        if (port > 0) {
            scheme = "https";
        } else {
            port = server.getIntAttr(Provisioning.A_zimbraMailPort, 0);
            scheme = "http";
        }
        return scheme + "://" + hostname + ":" + port;
    }

    public static Message addMessage(Mailbox mbox, String subject) throws Exception {
        return addMessage(mbox, Mailbox.ID_FOLDER_INBOX, subject);
    }

    public static Message addMessage(Mailbox mbox, int folderId, String subject) throws Exception {
        return addMessage(mbox, folderId, subject, System.currentTimeMillis());
    }

    public static Message addMessage(Mailbox mbox, int folderId, String subject, long timestamp) throws Exception {
        String message = getTestMessage(subject, null, null, new Date(timestamp));
        ParsedMessage pm = new ParsedMessage(message.getBytes(), timestamp, false);
        DeliveryOptions dopt = new DeliveryOptions().setFolderId(folderId).setFlags(Flag.BITMASK_UNREAD);
        return mbox.addMessage(null, pm, dopt, null);
    }

    public static Message addMessage(Mailbox mbox, ParsedMessage pm) throws Exception {
        DeliveryOptions dopt = new DeliveryOptions().setFolderId(Mailbox.ID_FOLDER_INBOX).setFlags(Flag.BITMASK_UNREAD);
        return mbox.addMessage(null, pm, dopt, null);
    }

    public static Message addMessage(Mailbox mbox, String recipient, String sender, String subject, String body,
            long timestamp) throws Exception {
        String message = getTestMessage(subject, recipient, sender, body, new Date(timestamp));
        ParsedMessage pm = new ParsedMessage(message.getBytes(), timestamp, false);
        return addMessage(mbox, pm);
    }

    public static String getTestMessage(String subject) throws ServiceException, MessagingException, IOException {
        return new MessageBuilder().withSubject(subject).create();
    }

    public static String getTestMessage(String subject, String recipient, String sender, Date date)
            throws ServiceException, MessagingException, IOException {
        return new MessageBuilder().withSubject(subject).withToRecipient(recipient).withFrom(sender).withDate(date)
                .create();
    }

    public static String getTestMessage(String subject, String recipient, String sender, String body, Date date)
            throws ServiceException, MessagingException, IOException {
        return new MessageBuilder().withSubject(subject).withToRecipient(recipient).withFrom(sender).withDate(date)
                .withBody(body).create();
    }

    public static Contact createContact(Mailbox mbox, int folderId, String emailAddr) throws ServiceException {
        return mbox.createContact(null,
                new ParsedContact(Collections.singletonMap(ContactConstants.A_email, emailAddr)), folderId, null);
    }

    protected static String addDomainIfNecessary(String user) throws ServiceException {
        if (StringUtil.isNullOrEmpty(user) || user.contains("@")) {
            return user;
        }
        return String.format("%s@%s", user, getDomain());
    }

    public static String addMessage(ZMailbox mbox, String subject) throws ServiceException, IOException,
            MessagingException {
        return addMessage(mbox, subject, Integer.toString(Mailbox.ID_FOLDER_INBOX));
    }

    public static String addMessage(ZMailbox mbox, String subject, String folderId) throws ServiceException,
            IOException, MessagingException {
        String message = getTestMessage(subject);
        return mbox.addMessage(folderId, null, null, 0, message, true);
    }

    public static String addMessage(ZMailbox mbox, String subject, String folderId, String flags)
            throws ServiceException, IOException, MessagingException {
        String message = getTestMessage(subject);
        return mbox.addMessage(folderId, flags, null, 0, message, true);
    }

    public static void sendMessage(ZMailbox senderMbox, String recipientName, String subject) throws Exception {
        String body = getTestMessage(subject);
        sendMessage(senderMbox, recipientName, subject, body);
    }

    public static void sendMessage(ZMailbox senderMbox, String recipientName, String subject, String body)
            throws Exception {
        sendMessage(senderMbox, recipientName, subject, body, null);
    }

    public static void sendMessage(ZMailbox senderMbox, String recipientName, String subject, String body,
            String attachmentUploadId) throws Exception {
        ZOutgoingMessage msg = getOutgoingMessage(recipientName, subject, body, attachmentUploadId);
        senderMbox.sendMessage(msg, null, false);
    }

    public static ZOutgoingMessage getOutgoingMessage(String recipient, String subject, String body,
            String attachmentUploadId) throws ServiceException {
        ZOutgoingMessage msg = new ZOutgoingMessage();
        List<ZEmailAddress> addresses = new ArrayList<ZEmailAddress>();
        addresses.add(new ZEmailAddress(addDomainIfNecessary(recipient), null, null, ZEmailAddress.EMAIL_TYPE_TO));
        msg.setAddresses(addresses);
        msg.setSubject(subject);
        msg.setMessagePart(new MessagePart("text/plain", body));
        msg.setAttachmentUploadId(attachmentUploadId);
        return msg;
    }

    /**
     * Searches a mailbox and returns the id's of all matching items.
     */
    public static List<Integer> search(Mailbox mbox, String query, MailItem.Type type) throws ServiceException {
        return search(mbox, query, Collections.singleton(type));
    }

    /**
     * Searches a mailbox and returns the id's of all matching items.
     */
    public static List<Integer> search(Mailbox mbox, String query, Set<MailItem.Type> types) throws ServiceException {
        List<Integer> ids = new ArrayList<Integer>();
        try (ZimbraQueryResults r = mbox.index.search(new OperationContext(mbox), query, types,
            SortBy.DATE_DESC, 100)) {
            while (r.hasNext()) {
                ZimbraHit hit = r.getNext();
                ids.add(new Integer(hit.getItemId()));
            }
        } catch (IOException e) {
        }
        return ids;
    }

    public static List<String> search(ZMailbox mbox, String query, String type) throws ServiceException {
        List<String> ids = new ArrayList<String>();
        ZSearchParams params = new ZSearchParams(query);
        params.setTypes(type);
        for (ZSearchHit hit : mbox.search(params).getHits()) {
            ids.add(hit.getId());
        }
        return ids;
    }

    public static List<ZMessage> search(ZMailbox mbox, String query) throws ServiceException {
        ZSearchParams params = new ZSearchParams(query);
        params.setTypes(ZSearchParams.TYPE_MESSAGE);

        return search(mbox, params);
    }

    public static List<ZMessage> search(ZMailbox mbox, ZSearchParams params) throws ServiceException {
        List<ZMessage> msgs = new ArrayList<ZMessage>();
        for (ZSearchHit hit : mbox.search(params).getHits()) {
            ZGetMessageParams msgParams = new ZGetMessageParams();
            msgParams.setId(hit.getId());
            msgs.add(mbox.getMessage(msgParams));
        }
        return msgs;
    }


    /**
     * Gets the raw content of a message.
     */
    public static String getContent(ZMailbox mbox, String msgId) throws ServiceException {
        ZGetMessageParams msgParams = new ZGetMessageParams();
        msgParams.setId(msgId);
        msgParams.setRawContent(true);
        ZMessage msg = mbox.getMessage(msgParams);
        return msg.getContent();
    }

    public static byte[] getContent(ZMailbox mbox, String msgId, String name) throws ServiceException, IOException {
        ZMessage msg = mbox.getMessageById(msgId);
        ZMimePart part = getPart(msg, name);
        if (part == null) {
            return null;
        }
        return ByteUtil.getContent(mbox.getRESTResource("?id=" + msgId + "&part=" + part.getPartName()), 1024);
    }

    /**
     * Returns the mime part with a matching name, part name, or filename.
     */
    public static ZMimePart getPart(ZMessage msg, String name) {
        return getPart(msg.getMimeStructure(), name);
    }

    private static ZMimePart getPart(ZMimePart mimeStructure, String name) {
        for (ZMimePart child : mimeStructure.getChildren()) {
            ZMimePart part = getPart(child, name);
            if (part != null) {
                return part;
            }
        }
        if (StringUtil.equalIgnoreCase(mimeStructure.getName(), name)
                || StringUtil.equalIgnoreCase(mimeStructure.getFileName(), name)
                || StringUtil.equalIgnoreCase(mimeStructure.getPartName(), name)) {
            return mimeStructure;
        }
        return null;
    }

    /**
     * Sets up the environment for command-line unit tests.
     */
    public static void cliSetup() throws ServiceException {
        if (!sIsCliInitialized) {
            if (TestUtil.fromRunUnitTests) {
                // Don't want to re-initialise log4j etc as results in redirecting away from mailbox.log
                CliUtil.toolSetup();
                Provisioning.setInstance(newSoapProvisioning());
            }
            SoapTransport.setDefaultUserAgent("Zimbra Unit Tests", BuildInfo.VERSION);
            sIsCliInitialized = true;
        }
    }

    public static SoapProvisioning newSoapProvisioning() throws ServiceException {
        SoapProvisioning sp = new SoapProvisioning();
        sp.soapSetURI("https://localhost:7071" + AdminConstants.ADMIN_SERVICE_URI);
        sp.soapZimbraAdminAuthenticate();
        return sp;
    }


    /**
     * Creates an account for the given username, with password set to {@link #DEFAULT_PASSWORD}.
     */
    public static Account createAccount(String username) throws ServiceException {
        Map<String, Object> attrs = new HashMap<String, Object>();
        attrs.put(Provisioning.A_zimbraMailHost, Provisioning.getInstance().getLocalServer().getServiceHostname());
        return createAccount(username, attrs);
    }

    /** Creates an account for the given username, and password. */
    public static Account createAccount(String username, String password, Map<String, Object> attrs)
    throws ServiceException {
        Provisioning prov = Provisioning.getInstance();
        String address = getAddress(username);
        return prov.createAccount(address, password, attrs);
    }

    /**
     * Creates an account for the given username, with password set to {@link #DEFAULT_PASSWORD}.
     */
    public static Account createAccount(String username, Map<String, Object> attrs) throws ServiceException {
        return createAccount(username, DEFAULT_PASSWORD, attrs);
    }

    /**
     * Deletes the account for the given username. Consider using {@link deleteAccountIfExists} as alternative
     * to reduce logging where the account may not exist.
     */
    public static void deleteAccount(String username) throws ServiceException {
        Provisioning prov = Provisioning.getInstance();

        // If this code is running on the server, call SoapProvisioning explicitly
        // so that both the account and mailbox are deleted.
        if (!(prov instanceof SoapProvisioning)) {
            prov = newSoapProvisioning();
        }
        SoapProvisioning soapProv = (SoapProvisioning) prov;
        GetAccountRequest gaReq = new GetAccountRequest(AccountSelector.fromName(username), false,
                Lists.newArrayList(Provisioning.A_zimbraId));
        try {
            GetAccountResponse resp = soapProv.invokeJaxb(gaReq);
            if (resp != null) {
                String id = null;
                for (Attr attr : resp.getAccount().getAttrList()) {
                    if (Provisioning.A_zimbraId.equals(attr.getKey())) {
                        id = attr.getValue();
                        break;
                    }
                }
                if (null == id) {
                    ZimbraLog.test.error("GetAccountResponse for '%s' did not contain the zimbraId", username);
                    return;
                }
                prov.deleteAccount(id);
            }
        } catch (SoapFaultException sfe) {
            if (!sfe.getMessage().contains("no such account")) {
                ZimbraLog.test.error("GetAccountResponse for '%s' hit unexpected problem", username, sfe);
            }
        }
    }

    public static void setServerAttr(String attrName, String attrValue) throws ServiceException {
        Provisioning prov = Provisioning.getInstance();
        Server server = prov.getLocalServer();
        Map<String, Object> attrs = new HashMap<String, Object>();
        attrs.put(attrName, attrValue);
        prov.modifyAttrs(server, attrs);
    }

    public static ZMessage getMessage(ZMailbox mbox, String query) throws Exception {
        List<ZMessage> results = search(mbox, query);
        String errorMsg = String.format("Unexpected number of messages returned by query '%s'", query);
        Assertions.assertEquals(1, results.size(), errorMsg);
        return results.get(0);
    }

    public static ZFolder createFolder(ZMailbox mbox, String path) throws ServiceException {
        return createFolder(mbox, path, ZFolder.View.message);
    }

    public static ZFolder createFolder(ZMailbox mbox, String path, ZFolder.View view) throws ServiceException {
        String parentId = Integer.toString(Mailbox.ID_FOLDER_USER_ROOT);
        String name = null;
        int idxLastSlash = path.lastIndexOf('/');

        if (idxLastSlash < 0) {
            name = path;
        } else if (idxLastSlash == 0) {
            name = path.substring(1);
        } else {
            String parentPath = path.substring(0, idxLastSlash);
            name = path.substring(idxLastSlash + 1);
            ZFolder parent = mbox.getFolderByPath(parentPath);
            if (parent == null) {
                String msg = String.format("Creating folder %s: parent %s does not exist", name, parentPath);
                throw ServiceException.FAILURE(msg, null);
            }
            parentId = parent.getId();
        }

        return mbox.createFolder(parentId, name, view, null, null, null);
    }

    public static ZFolder createFolder(ZMailbox mbox, String parentId, String folderName) throws ServiceException {
        return mbox.createFolder(parentId, folderName, ZFolder.View.message, null, null, null);
    }

    /**
     * Asserts that two elements and all children in their hierarchy are equal.
     */
    public static void assertEquals(Element expected, Element actual) {
        assertEquals(expected, actual, expected.prettyPrint(), actual.prettyPrint());
    }

    private static void assertEquals(Element expected, Element actual, String expectedDump, String actualDump) {
        Assertions.assertEquals(expected.getName(), actual.getName());
        List<Element> expectedChildren = expected.listElements();
        List<Element> actualChildren = actual.listElements();
        String context = String.format("Element %s, expected:\n%s\nactual:\n%s", expected.getName(), expectedDump,
                actualDump);
        Assertions.assertEquals(getElementNames(expectedChildren), getElementNames(actualChildren), context + " children");

        // Compare child elements
        for (int i = 0; i < expectedChildren.size(); i++) {
            assertEquals(expectedChildren.get(i), actualChildren.get(i), expectedDump, actualDump);
        }

        // Compare text
        Assertions.assertEquals(expected.getTextTrim(), actual.getTextTrim());

        // Compare attributes
        Set<Attribute> expectedAttrs = expected.listAttributes();
        Set<Attribute> actualAttrs = actual.listAttributes();
        Assertions.assertEquals(getAttributesAsString(expectedAttrs), getAttributesAsString(actualAttrs), context + " attributes");
    }

    /**
     * Asserts that two byte arrays are equal.
     */
    public static void assertEquals(byte[] expected, byte[] actual) {
        if (expected == null && actual == null) {
            return;
        }
        if (expected == null) {
            fail("expected was null but actual was not.");
            return; // shuts up warnings in Eclipse
        }
        if (actual == null) {
            fail("expected was not null but actual was.");
            return; // shuts up warnings in Eclipse
        }
        Assertions.assertEquals(expected.length, actual.length, "Arrays have different length.");
        for (int i = 0; i < expected.length; i++) {
            Assertions.assertEquals(expected[i], actual[i], "Data mismatch at byte " + i);
        }
    }

    private static String getElementNames(List<Element> elements) {
        StringBuilder buf = new StringBuilder();
        for (Element e : elements) {
            if (buf.length() > 0) {
                buf.append(",");
            }
            buf.append(e.getName());
        }
        return buf.toString();
    }

    private static String getAttributesAsString(Set<Attribute> attrs) {
        Set<String> attrStrings = new TreeSet<String>();
        for (Attribute attr : attrs) {
            attrStrings.add(String.format("%s=%s", attr.getKey(), attr.getValue()));
        }
        return StringUtil.join(",", attrStrings);
    }

    public static byte[] readInputStream(InputStream is) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int i = -1;
        while ((i = is.read()) >= 0) {
            baos.write(i);
        }
        return baos.toByteArray();
    }

    public static boolean bytesEqual(byte[] b1, InputStream is) throws IOException {
        return bytesEqual(b1, readInputStream(is));
    }

    public static boolean bytesEqual(byte[] b1, byte[] b2) {
        if (b1.length != b2.length) {
            return false;
        } else {
            for (int i = 0; i < b1.length; i++) {
                if (b1[i] != b2[i]) {
                    return false;
                }
            }
            return true;
        }
    }

    public static void updateMailItemChangeDateAndFlag(Mailbox mbox, int itemId, long changeDate, int flagValue)
            throws ServiceException {
        DbConnection conn = DbPool.getConnection(mbox);
        try {
            StringBuilder sql = new StringBuilder();
            sql.append("UPDATE ").append(DbMailItem.getMailItemTableName(mbox)).append(" SET change_date = ")
                    .append(changeDate);
            if (flagValue > 0) {
                sql.append(", flags = ").append(flagValue);
            }
            sql.append(" WHERE id = ").append(itemId);
            DbUtil.executeUpdate(conn, sql.toString());
        } finally {
            conn.commit();
            conn.closeQuietly();
        }
    }

    public static class UserInfo {
        private final String name;
        private Account acct;
        private UserInfo(String acctName) {
            try {
                acctName = AccountTestUtil.getAddress(acctName);
            } catch (ServiceException e) {
            }
            name = acctName;
            acct = null;
        }

        private void ensureAcctExists() throws ServiceException {
            if (null != acct) {
                return;
            }
            try {
                acct = getAccount(name);
            } catch (Exception se) {
                ZimbraLog.test.debug("ensureAcctExists getAccount exception '%s'", name, se);
            }
            if (null != acct) {
                return;
            }
            acct = create();
        }

        public Account create() throws ServiceException {
            acct = TestUtil.createAccount(name);
            return acct;
        }

        @Override
        public String toString() {
            return name;
        }

        public void cleanup() {
            if (null == acct) {
                return;  // Assumes only user UserInfo for creation/deletion of accounts
            }
            try {
                TestUtil.deleteAccount(name);
            } catch (Exception ex) {
                ZimbraLog.test.info("Exception thrown when deleting account '%s'", name, ex);
            }
            acct = null;
        }
    }

}
