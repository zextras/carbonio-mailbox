// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: AGPL-3.0-only

package com.zimbra.soap.mail;

import static org.junit.jupiter.api.Assertions.*;

import com.zimbra.soap.mail.message.CreateDataSourceRequest;
import com.zimbra.soap.mail.message.GetDataSourcesResponse;
import com.zimbra.soap.mail.message.ImportDataRequest;
import com.zimbra.soap.mail.message.ModifyDataSourceRequest;
import com.zimbra.soap.mail.message.TestDataSourceRequest;
import com.zimbra.soap.mail.type.CalDataSourceNameOrId;
import com.zimbra.soap.mail.type.CaldavDataSourceNameOrId;
import com.zimbra.soap.mail.type.DataSourceNameOrId;
import com.zimbra.soap.mail.type.GalDataSourceNameOrId;
import com.zimbra.soap.mail.type.ImapDataSourceNameOrId;
import com.zimbra.soap.mail.type.MailCalDataSource;
import com.zimbra.soap.mail.type.MailCaldavDataSource;
import com.zimbra.soap.mail.type.MailDataSource;
import com.zimbra.soap.mail.type.MailGalDataSource;
import com.zimbra.soap.mail.type.MailImapDataSource;
import com.zimbra.soap.mail.type.MailPop3DataSource;
import com.zimbra.soap.mail.type.MailRssDataSource;
import com.zimbra.soap.mail.type.Pop3DataSourceNameOrId;
import com.zimbra.soap.mail.type.RssDataSourceNameOrId;
import com.zimbra.soap.type.DataSource;
import com.zimbra.soap.type.DataSource.ConnectionType;
import java.util.List;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import org.junit.jupiter.api.Test;

public class DataSourceJaxbTest {

  @Test
  void testCreateDataSourceRequest() throws JAXBException {
    JAXBContext jaxb = JAXBContext.newInstance(CreateDataSourceRequest.class);
    Unmarshaller unmarshaller = jaxb.createUnmarshaller();
    CreateDataSourceRequest req =
        (CreateDataSourceRequest)
            unmarshaller.unmarshal(
                getClass().getResourceAsStream("CreateUnknownDataSourceRequest.xml"));
    DataSource ds = req.getDataSource();
    assertNotNull(ds, "Generic DataSource should not be NULL");
    assertTrue(ds instanceof MailDataSource, "DataSource should be an instance of MailDataSource");
    assertEquals("257", ds.getFolderId(), "wrong folder ID");
    assertEquals("yahoo.com", ds.getHost(), "wrong host");
    assertEquals("blablah@yahoo.com", ds.getName(), "wrong name");
    assertEquals("com.synacor.zimbra.OAuthDataImport", ds.getImportClass(), "wrong import class");

    req =
        (CreateDataSourceRequest)
            unmarshaller.unmarshal(
                getClass().getResourceAsStream("CreateImapDataSourceRequest.xml"));
    ds = req.getDataSource();
    assertNotNull(ds, "IMAP DataSource should not be NULL");
    assertTrue(
        ds instanceof MailImapDataSource, "DataSource should be an instance of MailImapDataSource");
    assertEquals("2", ds.getFolderId(), "wrong folder ID");
    assertEquals("imap.yahoo.com", ds.getHost(), "wrong host");
    assertEquals("blablah2@yahoo.com", ds.getName(), "wrong name");

    req =
        (CreateDataSourceRequest)
            unmarshaller.unmarshal(
                getClass().getResourceAsStream("CreateCalDataSourceRequest.xml"));
    ds = req.getDataSource();
    assertNotNull(ds, "Cal DataSource should not be NULL");
    assertTrue(
        ds instanceof MailCalDataSource, "DataSource should be an instance of MailCalDataSource");
    assertEquals("5", ds.getFolderId(), "wrong folder ID");
    assertEquals("calendar.google.com", ds.getHost(), "wrong host");
    assertEquals("someCalDS", ds.getName(), "wrong name");

    req =
        (CreateDataSourceRequest)
            unmarshaller.unmarshal(
                getClass().getResourceAsStream("CreateGalDataSourceRequest.xml"));
    ds = req.getDataSource();
    assertNotNull(ds, "GAL DataSource should not be NULL");
    assertTrue(
        ds instanceof MailGalDataSource, "DataSource should be an instance of MailGalDataSource");
    assertEquals("7", ds.getFolderId(), "wrong folder ID");
    assertEquals("ldap.zimbra.com", ds.getHost(), "wrong host");
    assertEquals("zimbraGAL", ds.getName(), "wrong name");
    assertEquals("24h", ds.getPollingInterval(), "wrong polling interval");
    assertTrue(ds.isEnabled(), "wrong isEnabled");

    req =
        (CreateDataSourceRequest)
            unmarshaller.unmarshal(
                getClass().getResourceAsStream("CreatePop3DataSourceRequest.xml"));
    ds = req.getDataSource();
    assertNotNull(ds, "POP3 DataSource should not be NULL");
    assertTrue(
        ds instanceof MailPop3DataSource, "DataSource should be an instance of MailPop3DataSource");
    assertEquals("1", ds.getFolderId(), "wrong folder ID");
    assertEquals("pop.email.provider.domain", ds.getHost(), "wrong host");
    assertEquals("pop3DSForTest", ds.getName(), "wrong name");
    assertEquals("24h", ds.getPollingInterval(), "wrong polling interval");
    assertTrue(((MailPop3DataSource) ds).isLeaveOnServer(), "wrong leaveOnServer");
    assertFalse(ds.isEnabled(), "wrong isEnabled");

    req =
        (CreateDataSourceRequest)
            unmarshaller.unmarshal(
                getClass().getResourceAsStream("CreateCaldavDataSourceRequest.xml"));
    ds = req.getDataSource();
    assertNotNull(ds, "CalDAV DataSource should not be NULL");
    assertTrue(
        ds instanceof MailCaldavDataSource,
        "DataSource should be an instance of MailCaldavDataSource");
    assertEquals("3", ds.getFolderId(), "wrong folder ID");
    assertEquals("some.cal.dav.host", ds.getHost(), "wrong host");
    assertEquals("caldavDS", ds.getName(), "wrong name");
    assertEquals("1h", ds.getPollingInterval(), "wrong polling interval");
    assertFalse(ds.isEnabled(), "wrong isEnabled");

    req =
        (CreateDataSourceRequest)
            unmarshaller.unmarshal(
                getClass().getResourceAsStream("CreateRssDataSourceRequest.xml"));
    ds = req.getDataSource();
    assertNotNull(ds, "RSS DataSource should not be NULL");
    assertTrue(
        ds instanceof MailRssDataSource, "DataSource should be an instance of MailRssDataSource");
    assertEquals("260", ds.getFolderId(), "wrong folder ID");
    assertEquals("some.rss.dav.host", ds.getHost(), "wrong host");
    assertEquals("RssFeedDataSource", ds.getName(), "wrong name");
    assertEquals("30m", ds.getPollingInterval(), "wrong polling interval");
    assertTrue(ds.isEnabled(), "wrong isEnabled");
  }

  @Test
  void testModifyDataSourceRequest() throws JAXBException {
    JAXBContext jaxb = JAXBContext.newInstance(ModifyDataSourceRequest.class);
    Unmarshaller unmarshaller = jaxb.createUnmarshaller();
    ModifyDataSourceRequest req =
        (ModifyDataSourceRequest)
            unmarshaller.unmarshal(
                getClass().getResourceAsStream("ModifyUnknownDataSourceRequest.xml"));
    DataSource ds = req.getDataSource();
    assertNotNull(ds, "Generic DataSource should not be NULL");
    assertNotNull(ds.getId(), "Generic DataSource ID should not be NULL");
    assertEquals(
        "11e1c69c-bbb3-4f5d-8903-14ef8bdacbcc", ds.getId(), "Wrong ID for generic DataSource");
    assertTrue(ds instanceof MailDataSource, "DataSource should be an instance of MailDataSource");
    assertEquals("yahoo.com", ds.getHost(), "wrong host");
    assertEquals("com.synacor.zimbra.OAuthDataImport", ds.getImportClass(), "wrong import class");
    assertEquals("60s", ds.getPollingInterval(), "wrong polling interval");

    req =
        (ModifyDataSourceRequest)
            unmarshaller.unmarshal(
                getClass().getResourceAsStream("ModifyImapDataSourceRequest.xml"));
    ds = req.getDataSource();
    assertNotNull(ds, "IMAP DataSource should not be NULL");
    assertNotNull(ds.getId(), "IMAP DataSource ID should not be NULL");
    assertEquals(
        "71e1c69c-bbb3-4f5d-8903-14ef8bdacbcc", ds.getId(), "Wrong ID for IMAP DataSource");
    assertTrue(
        ds instanceof MailImapDataSource, "DataSource should be an instance of MailImapDataSource");
    assertEquals("30m", ds.getPollingInterval(), "wrong polling interval");

    req =
        (ModifyDataSourceRequest)
            unmarshaller.unmarshal(
                getClass().getResourceAsStream("ModifyCalDataSourceRequest.xml"));
    ds = req.getDataSource();
    assertNotNull(ds, "Cal DataSource should not be NULL");
    assertNotNull(ds.getId(), "Cal DataSource ID should not be NULL");
    assertEquals("61e1c69c-bbb3-4f5d-8903-14ef8bdacbcc", ds.getId(), "Wrong ID for Cal DataSource");
    assertTrue(
        ds instanceof MailCalDataSource, "DataSource should be an instance of MailCalDataSource");
    assertEquals("333", ds.getFolderId(), "wrong folder ID");

    req =
        (ModifyDataSourceRequest)
            unmarshaller.unmarshal(
                getClass().getResourceAsStream("ModifyGalDataSourceRequest.xml"));
    ds = req.getDataSource();
    assertNotNull(ds, "GAL DataSource should not be NULL");
    assertNotNull(ds.getId(), "GAL DataSource ID should not be NULL");
    assertEquals("51e1c69c-bbb3-4f5d-8903-14ef8bdacbcc", ds.getId(), "Wrong ID for GAL DataSource");
    assertTrue(
        ds instanceof MailGalDataSource, "DataSource should be an instance of MailGalDataSource");
    assertEquals("69s", ds.getPollingInterval(), "wrong polling interval");

    req =
        (ModifyDataSourceRequest)
            unmarshaller.unmarshal(
                getClass().getResourceAsStream("ModifyPop3DataSourceRequest.xml"));
    ds = req.getDataSource();
    assertNotNull(ds, "POP3 DataSource should not be NULL");
    assertNotNull(ds.getId(), "POP3 DataSource ID should not be NULL");
    assertEquals(
        "41e1c69c-bbb3-4f5d-8903-14ef8bdacbcc", ds.getId(), "Wrong ID for POP3 DataSource");
    assertTrue(
        ds instanceof MailPop3DataSource, "DataSource should be an instance of MailPop3DataSource");
    assertEquals("1m", ds.getPollingInterval(), "wrong polling interval");
    assertFalse(((MailPop3DataSource) ds).isLeaveOnServer(), "wrong leaveOnServer");

    req =
        (ModifyDataSourceRequest)
            unmarshaller.unmarshal(
                getClass().getResourceAsStream("ModifyCaldavDataSourceRequest.xml"));
    ds = req.getDataSource();
    assertNotNull(ds, "Caldav DataSource should not be NULL");
    assertNotNull(ds.getId(), "Caldav DataSource ID should not be NULL");
    assertEquals(
        "31e1c69c-bbb3-4f5d-8903-14ef8bdacbcc", ds.getId(), "Wrong ID for Caldav DataSource");
    assertTrue(
        ds instanceof MailCaldavDataSource,
        "DataSource should be an instance of MailCaldavDataSource");
    assertEquals("60s", ds.getPollingInterval(), "wrong polling interval");

    req =
        (ModifyDataSourceRequest)
            unmarshaller.unmarshal(
                getClass().getResourceAsStream("ModifyRssDataSourceRequest.xml"));
    ds = req.getDataSource();
    assertNotNull(ds, "RSS DataSource should not be NULL");
    assertNotNull(ds.getId(), "RSS DataSource ID should not be NULL");
    assertEquals("21e1c69c-bbb3-4f5d-8903-14ef8bdacbcc", ds.getId(), "Wrong ID for RSS DataSource");
    assertTrue(
        ds instanceof MailRssDataSource, "DataSource should be an instance of MailRssDataSource");
    assertEquals("2d", ds.getPollingInterval(), "wrong polling interval");
    assertFalse(ds.isEnabled(), "wrong isEnabled");
  }

  @Test
  void testTestDataSourceRequest() throws Exception {
    JAXBContext jaxb = JAXBContext.newInstance(TestDataSourceRequest.class);
    Unmarshaller unmarshaller = jaxb.createUnmarshaller();
    TestDataSourceRequest req =
        (TestDataSourceRequest)
            unmarshaller.unmarshal(
                getClass().getResourceAsStream("TestUnknownDataSourceRequest.xml"));
    DataSource ds = req.getDataSource();
    assertNotNull(ds, "Generic DataSource should not be NULL");
    assertNotNull(ds.getId(), "Generic DataSource ID should not be NULL");
    assertEquals(
        "11e1c69c-bbb3-4f5d-8903-14ef8bdacbcc", ds.getId(), "Wrong ID for generic DataSource");
    assertTrue(ds instanceof MailDataSource, "DataSource should be an instance of MailDataSource");

    req =
        (TestDataSourceRequest)
            unmarshaller.unmarshal(getClass().getResourceAsStream("TestImapDataSourceRequest.xml"));
    ds = req.getDataSource();
    assertNotNull(ds, "IMAP DataSource should not be NULL");
    assertNotNull(ds.getId(), "IMAP DataSource ID should not be NULL");
    assertEquals(
        "71e1c69c-bbb3-4f5d-8903-14ef8bdacbcc", ds.getId(), "Wrong ID for IMAP DataSource");
    assertTrue(
        ds instanceof MailImapDataSource, "DataSource should be an instance of MailImapDataSource");

    req =
        (TestDataSourceRequest)
            unmarshaller.unmarshal(getClass().getResourceAsStream("TestCalDataSourceRequest.xml"));
    ds = req.getDataSource();
    assertNotNull(ds, "Cal DataSource should not be NULL");
    assertNotNull(ds.getId(), "Cal DataSource ID should not be NULL");
    assertEquals("31e1c69c-bbb3-4f5d-8903-14ef8bdacbcc", ds.getId(), "Wrong ID for Cal DataSource");
    assertTrue(
        ds instanceof MailCalDataSource, "DataSource should be an instance of MailCalDataSource");

    req =
        (TestDataSourceRequest)
            unmarshaller.unmarshal(getClass().getResourceAsStream("TestGalDataSourceRequest.xml"));
    ds = req.getDataSource();
    assertNotNull(ds, "GAL DataSource should not be NULL");
    assertNotNull(ds.getId(), "GAL DataSource ID should not be NULL");
    assertEquals("51e1c69c-bbb3-4f5d-8903-14ef8bdacbcc", ds.getId(), "Wrong ID for GAL DataSource");
    assertTrue(
        ds instanceof MailGalDataSource, "DataSource should be an instance of MailGalDataSource");

    req =
        (TestDataSourceRequest)
            unmarshaller.unmarshal(getClass().getResourceAsStream("TestPop3DataSourceRequest.xml"));
    ds = req.getDataSource();
    assertNotNull(ds, "POP3 DataSource should not be NULL");
    assertNotNull(ds.getId(), "POP3 DataSource ID should not be NULL");
    assertEquals(
        "41e1c69c-bbb3-4f5d-8903-14ef8bdacbcc", ds.getId(), "Wrong ID for POP3 DataSource");
    assertTrue(
        ds instanceof MailPop3DataSource, "DataSource should be an instance of MailPop3DataSource");

    req =
        (TestDataSourceRequest)
            unmarshaller.unmarshal(
                getClass().getResourceAsStream("TestCaldavDataSourceRequest.xml"));
    ds = req.getDataSource();
    assertNotNull(ds, "Caldav DataSource should not be NULL");
    assertNotNull(ds.getId(), "Caldav DataSource ID should not be NULL");
    assertEquals(
        "31e1c69c-bbb3-4f5d-8903-14ef8bdacbcc", ds.getId(), "Wrong ID for Caldav DataSource");
    assertTrue(
        ds instanceof MailCaldavDataSource,
        "DataSource should be an instance of MailCaldavDataSource");

    req =
        (TestDataSourceRequest)
            unmarshaller.unmarshal(getClass().getResourceAsStream("TestRssDataSourceRequest.xml"));
    ds = req.getDataSource();
    assertNotNull(ds, "RSS DataSource should not be NULL");
    assertNotNull(ds.getId(), "RSS DataSource ID should not be NULL");
    assertEquals("21e1c69c-bbb3-4f5d-8903-14ef8bdacbcc", ds.getId(), "Wrong ID for RSS DataSource");
    assertTrue(
        ds instanceof MailRssDataSource, "DataSource should be an instance of MailRssDataSource");
  }

  @Test
  void testGetDataSourcesResponse() throws Exception {
    // response with one Unknown datasource
    JAXBContext jaxb = JAXBContext.newInstance(GetDataSourcesResponse.class);
    Unmarshaller unmarshaller = jaxb.createUnmarshaller();
    GetDataSourcesResponse resp =
        (GetDataSourcesResponse)
            unmarshaller.unmarshal(
                getClass().getResourceAsStream("GetUnknownDataSourcesResponse.xml"));
    List<DataSource> dsList = resp.getDataSources();
    assertNotNull(dsList, "datasources should not be NULL");
    assertFalse(dsList.isEmpty(), "list of datasources should not be empty");
    assertEquals(1, dsList.size(), "expecting one datasource in the list");
    DataSource ds = dsList.get(0);
    assertNotNull(ds, "Generic DataSource should not be NULL");
    assertNotNull(ds.getId(), "Generic DataSource ID should not be NULL");
    assertEquals(
        "8d17e182-fdc6-4f6c-b83f-d478c9b04bfd", ds.getId(), "Wrong ID for generic DataSource");
    assertTrue(ds instanceof MailDataSource, "DataSource should be an instance of MailDataSource");
    assertEquals("yahoo.com", ds.getHost(), "wrong host");
    assertEquals("com.synacor.zimbra.OAuthDataImport", ds.getImportClass(), "wrong import class");
    assertEquals("blablah@yahoo.com", ds.getName(), "wrong datasource name");

    // response with one IMAP datasource
    resp =
        (GetDataSourcesResponse)
            unmarshaller.unmarshal(
                getClass().getResourceAsStream("GetImapDataSourcesResponse.xml"));
    dsList = resp.getDataSources();
    assertNotNull(dsList, "datasources should not be NULL");
    assertFalse(dsList.isEmpty(), "list of datasources should not be empty");
    assertEquals(1, dsList.size(), "expecting 1 datasource in the list");
    ds = dsList.get(0);
    assertNotNull(ds, "IMAP DataSource should not be NULL");
    assertNotNull(ds.getId(), "IMAP DataSource ID should not be NULL");
    assertEquals(
        "d96e9a7d-6af9-4625-ba68-37bcd73fce6d", ds.getId(), "Wrong ID for IMAP DataSource");
    assertTrue(
        ds instanceof MailImapDataSource, "DataSource should be an instance of MailImapDataSource");
    assertEquals(ConnectionType.cleartext, ds.getConnectionType(), "wrong connectionType");
    assertEquals("imap.zimbra.com", ds.getHost(), "wrong host");
    assertEquals("myIMAPSource", ds.getName(), "wrong datasource name");

    // response with an Unknown and an IMAP datasources
    resp =
        (GetDataSourcesResponse)
            unmarshaller.unmarshal(getClass().getResourceAsStream("GetTwoDataSourcesResponse.xml"));
    dsList = resp.getDataSources();
    assertNotNull(dsList, "datasources should not be NULL");
    assertFalse(dsList.isEmpty(), "list of datasources should not be empty");
    assertEquals(2, dsList.size(), "expecting 2 datasources in the list");
    ds = dsList.get(0);
    assertNotNull(ds, "Generic DataSource should not be NULL");
    assertNotNull(ds.getId(), "Generic DataSource ID should not be NULL");
    assertEquals(
        "8d17e182-fdc6-4f6c-b83f-d478c9b04bfd", ds.getId(), "Wrong ID for generic DataSource");
    assertTrue(ds instanceof MailDataSource, "DataSource should be an instance of MailDataSource");
    assertEquals("yahoo.com", ds.getHost(), "wrong host");
    assertEquals("com.synacor.zimbra.OAuthDataImport", ds.getImportClass(), "wrong import class");
    assertEquals("blablah@yahoo.com", ds.getName(), "wrong datasource name");

    ds = dsList.get(1);
    assertNotNull(ds, "IMAP DataSource should not be NULL");
    assertNotNull(ds.getId(), "IMAP DataSource ID should not be NULL");
    assertEquals(
        "d96e9a7d-6af9-4625-ba68-37bcd73fce6d", ds.getId(), "Wrong ID for IMAP DataSource");
    assertTrue(
        ds instanceof MailImapDataSource, "DataSource should be an instance of MailImapDataSource");
    assertEquals(ConnectionType.cleartext, ds.getConnectionType(), "wrong connectionType");
    assertEquals("imap.zimbra.com", ds.getHost(), "wrong host");
    assertEquals("myIMAPSource", ds.getName(), "wrong datasource name");

    // Response with one element of each type of datasource
    resp =
        (GetDataSourcesResponse)
            unmarshaller.unmarshal(
                getClass().getResourceAsStream("GetOneEachDataSourcesResponse.xml"));
    dsList = resp.getDataSources();
    assertNotNull(dsList, "datasources should not be NULL");
    assertFalse(dsList.isEmpty(), "list of datasources should not be empty");
    assertEquals(7, dsList.size(), "expecting 7 datasources in the list");
    ds = dsList.get(0);
    assertNotNull(ds, "Generic DataSource should not be NULL");
    assertNotNull(ds.getId(), "Generic DataSource ID should not be NULL");
    assertEquals(
        "8d17e182-fdc6-4f6c-b83f-d478c9b04bfd", ds.getId(), "Wrong ID for generic DataSource");
    assertTrue(ds instanceof MailDataSource, "DataSource should be an instance of MailDataSource");
    assertEquals("yahoo.com", ds.getHost(), "wrong host");
    assertEquals("com.synacor.zimbra.OAuthDataImport", ds.getImportClass(), "wrong import class");
    assertEquals("blablah@yahoo.com", ds.getName(), "wrong datasource name");

    ds = dsList.get(1);
    assertNotNull(ds, "IMAP DataSource should not be NULL");
    assertNotNull(ds.getId(), "IMAP DataSource ID should not be NULL");
    assertEquals(
        "d96e9a7d-6af9-4625-ba68-37bcd73fce6d", ds.getId(), "Wrong ID for IMAP DataSource");
    assertTrue(
        ds instanceof MailImapDataSource, "DataSource should be an instance of MailImapDataSource");
    assertEquals(ConnectionType.cleartext, ds.getConnectionType(), "wrong connectionType");
    assertEquals("imap.zimbra.com", ds.getHost(), "wrong host");
    assertEquals("myIMAPSource", ds.getName(), "wrong datasource name");

    ds = dsList.get(2);
    assertNotNull(ds, "POP3 DataSource should not be NULL");
    assertNotNull(ds.getId(), "POP3 DataSource ID should not be NULL");
    assertEquals(
        "b5e98a1f-5f93-4e19-a1a4-956c4c95af1b", ds.getId(), "Wrong ID for POP3 DataSource");
    assertTrue(
        ds instanceof MailPop3DataSource, "DataSource should be an instance of MailPop3DataSource");
    assertEquals(ConnectionType.cleartext, ds.getConnectionType(), "wrong connectionType");
    assertEquals("pop.zimbra.com", ds.getHost(), "wrong host");
    assertEquals("myPop3Mail", ds.getName(), "wrong datasource name");
    assertTrue(((MailPop3DataSource) ds).isLeaveOnServer(), "wrong leaveOnServer");

    ds = dsList.get(3);
    assertNotNull(ds, "RSS DataSource should not be NULL");
    assertNotNull(ds.getId(), "RSS DataSource ID should not be NULL");
    assertEquals("89bca37f-9096-419d-9471-62149a58cbdc", ds.getId(), "Wrong ID for RSS DataSource");
    assertTrue(
        ds instanceof MailRssDataSource, "DataSource should be an instance of MailRssDataSource");
    assertEquals(ConnectionType.cleartext, ds.getConnectionType(), "wrong connectionType");
    assertEquals("rss.zimbra.com", ds.getHost(), "wrong host");
    assertEquals("myRssFeed", ds.getName(), "wrong datasource name");
    assertEquals("43200000", ds.getPollingInterval(), "wrong polling interval");
    assertEquals("260", ds.getFolderId(), "wrong FolderId");

    ds = dsList.get(4);
    assertNotNull(ds, "Cal DataSource should not be NULL");
    assertNotNull(ds.getId(), "Cal DataSource ID should not be NULL");
    assertEquals("112da07b-43e3-41ab-a0b3-5c109169ee49", ds.getId(), "Wrong ID for Cal DataSource");
    assertTrue(
        ds instanceof MailCalDataSource, "DataSource should be an instance of MailCalDataSource");
    assertEquals("calendar.zimbra.com", ds.getHost(), "wrong host");
    assertEquals("GCal", ds.getName(), "wrong datasource name");
    assertEquals("63100000", ds.getPollingInterval(), "wrong polling interval");

    ds = dsList.get(5);
    assertNotNull(ds, "GAL DataSource should not be NULL");
    assertNotNull(ds.getId(), "GAL DataSource ID should not be NULL");
    assertEquals("ed408f4d-f8d5-4597-bf49-563ed62b64de", ds.getId(), "Wrong ID for GAL DataSource");
    assertTrue(
        ds instanceof MailGalDataSource, "DataSource should be an instance of MailCalDataSource");
    assertEquals("ldap.somehost.local", ds.getHost(), "wrong host");
    assertEquals("corpAddressBook", ds.getName(), "wrong datasource name");

    ds = dsList.get(6);
    assertNotNull(ds, "Caldav DataSource should not be NULL");
    assertNotNull(ds.getId(), "Caldav DataSource ID should not be NULL");
    assertEquals(
        "95c066a8-5ad6-40fa-a094-06f8b3531878", ds.getId(), "Wrong ID for Caldav DataSource");
    assertTrue(
        ds instanceof MailCaldavDataSource,
        "DataSource should be an instance of MailCaldavDataSource");
    assertEquals("dav.zimbra.com", ds.getHost(), "wrong host");
    assertEquals("externalDAV", ds.getName(), "wrong datasource name");

    // Response with multiple instances of some types of data sources
    resp =
        (GetDataSourcesResponse)
            unmarshaller.unmarshal(
                getClass().getResourceAsStream("GetManyDataSourcesResponse.xml"));
    dsList = resp.getDataSources();
    assertNotNull(dsList, "datasources should not be NULL");
    assertFalse(dsList.isEmpty(), "list of datasources should not be empty");
    assertEquals(10, dsList.size(), "expecting 10 datasources in the list");
    ds = dsList.get(0);
    assertNotNull(ds, "Generic DataSource should not be NULL");
    assertNotNull(ds.getId(), "Generic DataSource ID should not be NULL");
    assertEquals(
        "8d17e182-fdc6-4f6c-b83f-d478c9b04bfd", ds.getId(), "Wrong ID for generic DataSource");
    assertTrue(ds instanceof MailDataSource, "DataSource should be an instance of MailDataSource");
    assertEquals("yahoo.com", ds.getHost(), "wrong host");
    assertEquals("com.synacor.zimbra.OAuthDataImport", ds.getImportClass(), "wrong import class");
    assertEquals("blablah@yahoo.com", ds.getName(), "wrong datasource name");

    ds = dsList.get(1);
    assertNotNull(ds, "IMAP DataSource should not be NULL");
    assertNotNull(ds.getId(), "IMAP DataSource ID should not be NULL");
    assertEquals(
        "d96e9a7d-6af9-4625-ba68-37bcd73fce6d", ds.getId(), "Wrong ID for IMAP DataSource");
    assertTrue(
        ds instanceof MailImapDataSource, "DataSource should be an instance of MailImapDataSource");
    assertEquals(ConnectionType.cleartext, ds.getConnectionType(), "wrong connectionType");
    assertEquals("imap.zimbra.com", ds.getHost(), "wrong host");
    assertEquals("myIMAPSource", ds.getName(), "wrong datasource name");
    assertEquals(143, ds.getPort().intValue(), "wrong port");

    ds = dsList.get(2);
    assertNotNull(ds, "POP3 DataSource should not be NULL");
    assertNotNull(ds.getId(), "POP3 DataSource ID should not be NULL");
    assertEquals(
        "b5e98a1f-5f93-4e19-a1a4-956c4c95af1b", ds.getId(), "Wrong ID for POP3 DataSource");
    assertTrue(
        ds instanceof MailPop3DataSource, "DataSource should be an instance of MailPop3DataSource");
    assertEquals(ConnectionType.cleartext, ds.getConnectionType(), "wrong connectionType");
    assertEquals("pop.zimbra.com", ds.getHost(), "wrong host");
    assertEquals("myPop3Mail", ds.getName(), "wrong datasource name");
    assertTrue(((MailPop3DataSource) ds).isLeaveOnServer(), "wrong leaveOnServer");

    ds = dsList.get(3);
    assertNotNull(ds, "RSS DataSource should not be NULL");
    assertNotNull(ds.getId(), "RSS DataSource ID should not be NULL");
    assertEquals("89bca37f-9096-419d-9471-62149a58cbdc", ds.getId(), "Wrong ID for RSS DataSource");
    assertTrue(
        ds instanceof MailRssDataSource, "DataSource should be an instance of MailRssDataSource");
    assertEquals(ConnectionType.cleartext, ds.getConnectionType(), "wrong connectionType");
    assertEquals("rss.zimbra.com", ds.getHost(), "wrong host");
    assertEquals("myRssFeed", ds.getName(), "wrong datasource name");
    assertEquals("43200000", ds.getPollingInterval(), "wrong polling interval");
    assertEquals("260", ds.getFolderId(), "wrong FolderId");

    ds = dsList.get(4);
    assertNotNull(ds, "Cal DataSource should not be NULL");
    assertNotNull(ds.getId(), "Cal DataSource ID should not be NULL");
    assertEquals("112da07b-43e3-41ab-a0b3-5c109169ee49", ds.getId(), "Wrong ID for Cal DataSource");
    assertTrue(
        ds instanceof MailCalDataSource, "DataSource should be an instance of MailCalDataSource");
    assertEquals("calendar.zimbra.com", ds.getHost(), "wrong host");
    assertEquals("GCal", ds.getName(), "wrong datasource name");
    assertEquals("63100000", ds.getPollingInterval(), "wrong polling interval");

    ds = dsList.get(5);
    assertNotNull(ds, "GAL DataSource should not be NULL");
    assertNotNull(ds.getId(), "GAL DataSource ID should not be NULL");
    assertEquals("ed408f4d-f8d5-4597-bf49-563ed62b64de", ds.getId(), "Wrong ID for GAL DataSource");
    assertTrue(
        ds instanceof MailGalDataSource, "DataSource should be an instance of MailCalDataSource");
    assertEquals("ldap.somehost.local", ds.getHost(), "wrong host");
    assertEquals("corpAddressBook", ds.getName(), "wrong datasource name");

    ds = dsList.get(6);
    assertNotNull(ds, "Caldav DataSource should not be NULL");
    assertNotNull(ds.getId(), "Caldav DataSource ID should not be NULL");
    assertEquals(
        "95c066a8-5ad6-40fa-a094-06f8b3531878", ds.getId(), "Wrong ID for Caldav DataSource");
    assertTrue(
        ds instanceof MailCaldavDataSource,
        "DataSource should be an instance of MailCaldavDataSource");
    assertEquals("dav.zimbra.com", ds.getHost(), "wrong host");
    assertEquals("externalDAV", ds.getName(), "wrong datasource name");

    ds = dsList.get(7);
    assertNotNull(ds, "2d RSS DataSource should not be NULL");
    assertNotNull(ds.getId(), "2d RSS DataSource ID should not be NULL");
    assertEquals(
        "f32349af-9a78-4c26-80a1-338203378930", ds.getId(), "Wrong ID for the 2d RSS DataSource");
    assertTrue(
        ds instanceof MailRssDataSource, "DataSource should be an instance of MailRssDataSource");
    assertEquals(ConnectionType.cleartext, ds.getConnectionType(), "wrong connectionType");
    assertEquals("news.yahoo.com", ds.getHost(), "wrong host");
    assertEquals("myYahoo", ds.getName(), "wrong datasource name");
    assertEquals("43200000", ds.getPollingInterval(), "wrong polling interval");
    assertEquals("261", ds.getFolderId(), "wrong FolderId");

    ds = dsList.get(8);
    assertNotNull(ds, "2d IMAP DataSource should not be NULL");
    assertNotNull(ds.getId(), "2d IMAP DataSource ID should not be NULL");
    assertEquals(
        "b2e929f5-e124-47a0-b1b4-a7fbcd14fb31", ds.getId(), "Wrong ID for the 2d IMAP DataSource");
    assertTrue(
        ds instanceof MailImapDataSource, "DataSource should be an instance of MailImapDataSource");
    assertEquals(ConnectionType.tls_if_available, ds.getConnectionType(), "wrong connectionType");
    assertEquals("imap3.zimbra.com", ds.getHost(), "wrong host");
    assertEquals(193, ds.getPort().intValue(), "wrong port");
    assertEquals("forgottenMail", ds.getName(), "wrong datasource name");

    ds = dsList.get(9);
    assertNotNull(ds, "2d Generic DataSource should not be NULL");
    assertNotNull(ds.getId(), "2d Generic DataSource ID should not be NULL");
    assertEquals(
        "82e3b467-5a0f-4cff-ad8d-533ed6fc4992",
        ds.getId(),
        "Wrong ID for the 2d generic DataSource");
    assertTrue(ds instanceof MailDataSource, "DataSource should be an instance of MailDataSource");
    assertEquals("abook.gmail.com", ds.getHost(), "wrong host");
    assertEquals("com.synacor.zimbra.OAuthDataImport", ds.getImportClass(), "wrong import class");
    assertEquals("someone@gmail.com", ds.getName(), "wrong datasource name");
  }

  @Test
  void testImportDataRequest() throws Exception {
    JAXBContext jaxb = JAXBContext.newInstance(ImportDataRequest.class);
    Unmarshaller unmarshaller = jaxb.createUnmarshaller();
    ImportDataRequest resp =
        (ImportDataRequest)
            unmarshaller.unmarshal(getClass().getResourceAsStream("UnknownImportDataRequest.xml"));
    List<DataSourceNameOrId> dsList = resp.getDataSources();
    assertNotNull(dsList, "datasources should not be NULL");
    assertFalse(dsList.isEmpty(), "list of datasources should not be empty");
    assertEquals(1, dsList.size(), "expecting one datasource in the list");
    DataSourceNameOrId ds = dsList.get(0);
    assertNotNull(ds, "Generic DataSourceNameOrId should not be NULL");
    assertNotNull(ds.getId(), "Generic DataSourceNameOrId ID should not be NULL");
    assertEquals(
        "8d17e182-fdc6-4f6c-b83f-d478c9b04bfd", ds.getId(), "Wrong ID for generic DataSource");
    assertTrue(
        ds instanceof DataSourceNameOrId, "DataSource should be an instance of DataSourceNameOrId");

    resp =
        (ImportDataRequest)
            unmarshaller.unmarshal(getClass().getResourceAsStream("ImapImportDataRequest.xml"));
    dsList = resp.getDataSources();
    assertNotNull(dsList, "datasources should not be NULL");
    assertFalse(dsList.isEmpty(), "list of datasources should not be empty");
    assertEquals(1, dsList.size(), "expecting 1 datasource in the list");
    ds = dsList.get(0);
    assertNotNull(ds, "IMAP DataSourceNameOrId should not be NULL");
    assertNotNull(ds.getId(), "IMAP DataSourceNameOrId ID should not be NULL");
    assertEquals(
        "d96e9a7d-6af9-4625-ba68-37bcd73fce6d", ds.getId(), "Wrong ID for IMAP DataSourceNameOrId");
    assertTrue(
        ds instanceof ImapDataSourceNameOrId,
        "DataSourceNameOrId should be an instance of ImapDataSourceNameOrId");

    resp =
        (ImportDataRequest)
            unmarshaller.unmarshal(getClass().getResourceAsStream("Pop3ImportDataRequest.xml"));
    dsList = resp.getDataSources();
    assertNotNull(dsList, "datasources should not be NULL");
    assertFalse(dsList.isEmpty(), "list of datasources should not be empty");
    assertEquals(1, dsList.size(), "expecting 1 datasource in the list");
    ds = dsList.get(0);
    assertNotNull(ds, "POP3 DataSourceNameOrId should not be NULL");
    assertNotNull(ds.getId(), "POP3 DataSourceNameOrId ID should not be NULL");
    assertEquals(
        "d96e9a7d-6af9-4625-ba68-37bcd73fce6d", ds.getId(), "Wrong ID for POP3 DataSourceNameOrId");
    assertTrue(
        ds instanceof Pop3DataSourceNameOrId,
        "DataSourceNameOrId should be an instance of Pop3DataSourceNameOrId");

    resp =
        (ImportDataRequest)
            unmarshaller.unmarshal(getClass().getResourceAsStream("RssImportDataRequest.xml"));
    dsList = resp.getDataSources();
    assertNotNull(dsList, "datasources should not be NULL");
    assertFalse(dsList.isEmpty(), "list of datasources should not be empty");
    assertEquals(1, dsList.size(), "expecting 1 datasource in the list");
    ds = dsList.get(0);
    assertNotNull(ds, "POP3 DataSourceNameOrId should not be NULL");
    assertNotNull(ds.getId(), "POP3 DataSourceNameOrId ID should not be NULL");
    assertEquals(
        "21e1c69c-bbb3-4f5d-8903-14ef8bdacbcc", ds.getId(), "Wrong ID for RSS DataSourceNameOrId");
    assertTrue(
        ds instanceof RssDataSourceNameOrId,
        "DataSourceNameOrId should be an instance of RssDataSourceNameOrId");

    resp =
        (ImportDataRequest)
            unmarshaller.unmarshal(getClass().getResourceAsStream("CaldavImportDataRequest.xml"));
    dsList = resp.getDataSources();
    assertNotNull(dsList, "datasources should not be NULL");
    assertFalse(dsList.isEmpty(), "list of datasources should not be empty");
    assertEquals(1, dsList.size(), "expecting 1 datasource in the list");
    ds = dsList.get(0);
    assertNotNull(ds, "Caldav DataSourceNameOrId should not be NULL");
    assertNotNull(ds.getId(), "Caldav DataSourceNameOrId ID should not be NULL");
    assertEquals(
        "31e1c69c-bbb3-4f5d-8903-14ef8bdacbcc",
        ds.getId(),
        "Wrong ID for Caldav DataSourceNameOrId");
    assertTrue(
        ds instanceof CaldavDataSourceNameOrId,
        "DataSourceNameOrId should be an instance of CaldavDataSourceNameOrId");

    resp =
        (ImportDataRequest)
            unmarshaller.unmarshal(getClass().getResourceAsStream("CalImportDataRequest.xml"));
    dsList = resp.getDataSources();
    assertNotNull(dsList, "datasources should not be NULL");
    assertFalse(dsList.isEmpty(), "list of datasources should not be empty");
    assertEquals(1, dsList.size(), "expecting 1 datasource in the list");
    ds = dsList.get(0);
    assertNotNull(ds, "Cal DataSourceNameOrId should not be NULL");
    assertNotNull(ds.getId(), "Cal DataSourceNameOrId ID should not be NULL");
    assertEquals(
        "61e1c69c-bbb3-4f5d-8903-14ef8bdacbcc",
        ds.getId(),
        "Wrong ID for Caldav DataSourceNameOrId");
    assertTrue(
        ds instanceof CalDataSourceNameOrId,
        "DataSourceNameOrId should be an instance of CalDataSourceNameOrId");

    resp =
        (ImportDataRequest)
            unmarshaller.unmarshal(getClass().getResourceAsStream("GalImportDataRequest.xml"));
    dsList = resp.getDataSources();
    assertNotNull(dsList, "datasources should not be NULL");
    assertFalse(dsList.isEmpty(), "list of datasources should not be empty");
    assertEquals(1, dsList.size(), "expecting 1 datasource in the list");
    ds = dsList.get(0);
    assertNotNull(ds, "GAL DataSourceNameOrId should not be NULL");
    assertNotNull(ds.getId(), "GAL DataSourceNameOrId ID should not be NULL");
    assertEquals(
        "51e1c69c-bbb3-4f5d-8903-14ef8bdacbcc",
        ds.getId(),
        "Wrong ID for Caldav DataSourceNameOrId");
    assertTrue(
        ds instanceof GalDataSourceNameOrId,
        "DataSourceNameOrId should be an instance of CalDataSourceNameOrId");
  }
}
