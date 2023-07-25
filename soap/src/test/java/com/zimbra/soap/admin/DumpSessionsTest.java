// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin;

import static org.junit.jupiter.api.Assertions.*;

import com.google.common.collect.Lists;
import com.zimbra.soap.admin.message.DumpSessionsResponse;
import com.zimbra.soap.admin.type.AccountSessionInfo;
import com.zimbra.soap.admin.type.InfoForSessionType;
import com.zimbra.soap.admin.type.SessionInfo;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Element;

/**
 * Unit test for {@link DumpSessionsResponse}. Mostly checking the handling of @XmlAnyElement in
 * {@link SessionInfo} test xml files are hand generated and are not taken from real world data.
 */
public class DumpSessionsTest {

  private static final Logger LOG = LogManager.getLogger(DumpSessionsTest.class);

  private static Unmarshaller unmarshaller;
  private static Marshaller marshaller;

  static {
    com.zimbra.common.util.LogManager.setThisLogAndRootToLevel(LOG, Level.INFO);
  }

    @BeforeAll
  public static void init() throws Exception {
    JAXBContext jaxb = JAXBContext.newInstance(DumpSessionsResponse.class);
    unmarshaller = jaxb.createUnmarshaller();
    marshaller = jaxb.createMarshaller();
  }

    private static void checkImapSession(SessionInfo session,
            boolean nameAndIdPresent) {
        assertEquals(1300295279211L, session.getCreatedDate(), "Create Date");
        assertEquals(1300295279212L, session.getLastAccessedDate(), "Last Access Date");
    if (nameAndIdPresent) {
            assertEquals("user1@gren-elliots-macbook-pro.local", session.getName(), "Session Name");
            assertEquals("006584ae-cba0-400a-8414-f764ba3c7418", session.getZimbraId(), "Session Zimbra Id");
    } else {
            assertNull(session.getName(), "Session Name");
            assertNull(session.getZimbraId(), "Session Zimbra Id");
    }
        assertEquals("223", session.getSessionId(), "Session Id");
    Map<QName, Object> extraAttribs = session.getExtraAttributes();
        assertEquals(0, extraAttribs.size(), "Number of extra Attribs");
    List<Element> extraElements = session.getExtraElements();
        assertNotNull(extraElements, "Extra Elements");
        assertEquals(1, extraElements.size(), "Number of extra Elements");
    Element elem = extraElements.get(0);
        assertEquals("imap", elem.getNodeName(), "imap Element nodeName");
  }

  @Test
  @Disabled("add required xml files to run")
  void unmarshallDumpSessionsResponseTest()
      throws Exception {
    InputStream is = getClass().getResourceAsStream("DumpSessionsResponse-listSess.xml");
    DumpSessionsResponse resp = (DumpSessionsResponse) unmarshaller.unmarshal(is);
    assertEquals(1, resp.getTotalActiveSessions(), "Total active sessions");
    assertNull(resp.getAdminSessions(), "Admin Sessions");
    assertNull(resp.getSoapSessions(), "Soap Sessions");
    assertNull(resp.getWikiSessions(), "Wiki Sessions");
    assertNull(resp.getWaitsetSessions(), "Waitset Sessions");
    InfoForSessionType imapInfo = resp.getImapSessions();
    assertEquals(new Integer(1), imapInfo.getActiveAccounts(), "Imap active accounts");
    assertEquals(1, imapInfo.getActiveSessions(), "Imap active sessions");
    List<SessionInfo> sessions = imapInfo.getSessions();
    List<AccountSessionInfo> accts = imapInfo.getAccounts();
    assertEquals(0, accts.size(), "Number of top level accts");
    assertEquals(1, sessions.size(), "Number of top level sessionInfos");
    SessionInfo session = sessions.get(0);
    checkImapSession(session, true);
  }

  @Test
  @Disabled("add required xml files to run")
  void unmarshallDumpSessionsResponseGroupedTest()
      throws Exception {
    InputStream is = getClass().getResourceAsStream("DumpSessionsResponse-grouped.xml");
    DumpSessionsResponse resp = (DumpSessionsResponse) unmarshaller.unmarshal(is);
    assertEquals(1, resp.getTotalActiveSessions(), "Total active sessions");
    assertNull(resp.getAdminSessions(), "Admin Sessions");
    assertNull(resp.getSoapSessions(), "Soap Sessions");
    assertNull(resp.getWikiSessions(), "Wiki Sessions");
    assertNull(resp.getWaitsetSessions(), "Waitset Sessions");
    InfoForSessionType imapInfo = resp.getImapSessions();
    assertEquals(new Integer(1), imapInfo.getActiveAccounts(), "Imap active accounts");
    assertEquals(1, imapInfo.getActiveSessions(), "Imap active sessions");
    List<SessionInfo> sessions = imapInfo.getSessions();
    assertEquals(0, sessions.size(), "Number of top level sessionInfos");
    List<AccountSessionInfo> accts = imapInfo.getAccounts();
    assertEquals(1, accts.size(), "Number of top level accts");
    AccountSessionInfo acct = accts.get(0);
    sessions = acct.getSessions();
    assertEquals(1, sessions.size(), "Number of top level sessionInfos");
    SessionInfo session = sessions.get(0);
    checkImapSession(session, false);
  }

  @Test
  void marshallDumpSessionsResponse() throws Exception {
    javax.xml.parsers.DocumentBuilder w3DomBuilder =
        javax.xml.parsers.DocumentBuilderFactory.newInstance().newDocumentBuilder();
    DumpSessionsResponse gsr = new DumpSessionsResponse(1);
    InfoForSessionType imapSessions = new InfoForSessionType(1, 1);
    AccountSessionInfo account =
        new AccountSessionInfo(
            "user1@gren-elliots-macbook-pro.local", "006584ae-cba0-400a-8414-f764ba3c7418");
    SessionInfo session = new SessionInfo(null, null, "223", 1300295279211L, 1300295279212L);
    org.w3c.dom.Document doc = w3DomBuilder.newDocument();
    org.w3c.dom.Element extraElement = doc.createElementNS("urn:zimbraAdmin", "imap");
    extraElement.setAttribute("folder", "INBOX");
    extraElement.setAttribute("expunged", "0");
    extraElement.setAttribute("writable", "1");
    extraElement.setAttribute("dirty", "0");
    extraElement.setAttribute("size", "222");
    session.addExtraElement(extraElement);
    QName qn = new QName("push");
    session.addExtraAttribute(qn, "true");
    account.addSession(session);
    List<AccountSessionInfo> accounts = Lists.newArrayList();
    accounts.add(account);
    imapSessions.setAccounts(accounts);
    gsr.setImapSessions(imapSessions);
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    marshaller.marshal(gsr, out);
    String xml = out.toString("UTF-8");
    if (LOG.isInfoEnabled())
      LOG.info("Xml:\n" + xml);
    assertTrue(xml.indexOf("push=\"true\"") > 0, "Marshalled XML should contain 'push=\"true\"'");
    assertTrue(xml.indexOf("size=\"222\"") > 0, "Marshalled XML should contain 'size=\"222\"'");
    assertTrue(
        xml.endsWith("DumpSessionsResponse>"), "Marshalled XML should end with 'DumpSessionsResponse>'");
  }
}
