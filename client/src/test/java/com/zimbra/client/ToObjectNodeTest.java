// SPDX-FileCopyrightText: 2026 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.client;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zimbra.common.localconfig.LC;
import com.zimbra.common.soap.Element;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.TimeZone;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ToObjectNodeTest {

  private static final ObjectMapper MAPPER = new ObjectMapper();

  private static final String INVITE =
      """
          <inv type='appt'>\
          <tz id='Europe/Rome' stdoff='60' dayoff='120' stdname='CET' dayname='CEST'>\
          <standard week='-1' wkday='1' mon='10' hour='3' min='0' sec='0'/>\
          <daylight week='-1' wkday='1' mon='3' hour='2' min='0' sec='0'/>\
          </tz>\
          <comp name='Meeting' loc='Room 1' isOrg='1' seq='2' compNum='0' url='http://x'>\
          <category>work</category><comment>bring laptop</comment><contact>Bob</contact>\
          <replies><reply d='1700000000000' at='bob@test.com' ptst='AC'/></replies>\
          <s d='20261116T100000' tz='Europe/Rome'/>\
          <e d='20261116T110000' tz='Europe/Rome'/>\
          <or a='alice@test.com' d='Alice' sentBy='carol@test.com'/>\
          <at a='bob@test.com' d='Bob' role='REQ' ptst='NE' rsvp='1' cutype='IND'/>\
          <recur>\
          <add><rule freq='WEE'><until d='20261231' tz='Europe/Rome'/><interval ival='1'/>\
          <byday><wkday day='MO'/><wkday ordwk='2' day='FR'/></byday>\
          <bymonth molist='1,2'/></rule></add>\
          <add><dates tz='Europe/Rome'><dtval><s d='20261120T100000' tz='Europe/Rome'/>\
          <dur h='1' m='30'/></dtval></dates></add>\
          <exclude><dates tz='Europe/Rome'><dtval><s d='20261123T100000' tz='Europe/Rome'/>\
          <e d='20261123T110000' tz='Europe/Rome'/></dtval></dates></exclude>\
          </recur>\
          <desc>plain</desc><descHtml>&lt;b&gt;html&lt;/b&gt;</descHtml>\
          </comp>\
          </inv>""";

  private static final String SHARE =
      """
          &lt;share xmlns=&quot;urn:zimbraShare&quot; version=&quot;0.1&quot; action=&quot;new&quot;&gt;\
          &lt;grantee id=&quot;g1&quot; email=&quot;bob@test.com&quot; name=&quot;Bob&quot;/&gt;\
          &lt;grantor id=&quot;g2&quot; email=&quot;alice@test.com&quot; name=&quot;Alice&quot;/&gt;\
          &lt;link id=&quot;7&quot; name=&quot;Calendar&quot; view=&quot;appointment&quot; perm=&quot;r&quot;/&gt;\
          &lt;/share&gt;""";

  @BeforeAll
  static void setUpTimezones(@TempDir File dir) throws Exception {
    File tzFile = new File(dir, "timezones.ics");
    Files.writeString(tzFile.toPath(), "BEGIN:VCALENDAR\r\nEND:VCALENDAR");
    LC.timezone_file.setDefault(tzFile.getAbsolutePath());
  }

  @Test
  void message() throws Exception {
    assertJson("message.json", createMessage());
  }

  @Test
  void folder() throws Exception {
    assertJson("folder.json", createFolder());
  }

  @Test
  void searchResult() throws Exception {
    assertJson("search-result.json", createSearchResult());
  }

  @Test
  void contact() throws Exception {
    assertJson("contact.json", createContact());
  }

  @Test
  void tag() throws Exception {
    assertJson("tag.json", createTag());
  }

  @Test
  void signature() throws Exception {
    assertJson("signature.json", createSignature());
  }

  @Test
  void ace() throws Exception {
    assertJson("ace.json", createAce());
  }

  static ZMessage createMessage() throws Exception {
    String xml =
        """
            <m id='257' f='au' t='1,2' l='2' cid='-257' d='1700000000000' sd='1699999999000' s='1234' imap='9'>
                <su>Hello</su>
                <fr>fragment</fr>
                <mid>&lt;id@test&gt;</mid>
                <e a='alice@test.com' d='Alice' p='Alice A' t='f' />
                <e a='bob@test.com' t='t' />
                <header n='X-Custom'>value</header>
                <mp part='TEXT' ct='multipart/alternative'>
                    <mp part='1' ct='text/plain' s='10' body='1'>
                        <content>body</content>
                    </mp>
                    <mp part='2' ct='image/png' s='20' cd='attachment' filename='a.png' ci='&lt;c1&gt;' />
                </mp>%s
                <shr content='%s' />
            </m>""".formatted(INVITE, SHARE);
    return new ZMessage(Element.parseXML(xml), null);
  }

  static ZFolder createFolder() throws Exception {
    String xml =
        """
            <folder id='1' uuid='u1' name='USER_ROOT' l='11' f='#' color='3' u='1' n='2' s='300'\
             view='message' i4next='5' i4ms='6' rev='1' ms='1' perm='rwidx' url='http://feed'>\
            <acl><grant gt='usr' zid='z1' d='bob@test.com' perm='r'/>\
            <grant gt='guest' zid='z2' d='guest@test.com' perm='r' pw='secret'/></acl>\
            <folder id='2' name='Inbox' l='1' view='message' rev='1' ms='1' n='0' s='0'/>\
            <search id='3' name='Saved' l='1' query='is:unread' types='message' sortBy='dateAsc'\
             rev='1' ms='1'/>\
            <link id='4' name='Shared' l='1' zid='owner' rid='10' owner='carol@test.com'\
             view='appointment' rev='1' ms='1' n='0' s='0'/>\
            </folder>""";
    return new ZFolder(Element.parseXML(xml), null, null);
  }

  static ZSearchResult createSearchResult() throws Exception {
    String xml =
        """
            <SearchResponse sortBy='dateDesc' more='1' offset='0'>\
            <c id='-1' n='2' f='u' t='1' d='1700000000000' sf='1700000000000'>\
            <su>conv</su><fr>frag</fr><m id='1'/><m id='2'/>\
            <e a='alice@test.com' t='f'/></c>\
            <m id='5' l='2' cid='-5' f='a' d='1700000000000' s='10' sf='x'>\
            <su>msg</su><fr>frag</fr><e a='alice@test.com' t='f'/><hp part='1'/></m>\
            <cn id='6' l='7' fileAsStr='Doe, John' sf='doe' rev='3' email='john@test.com'>\
            <a n='firstName'>John</a></cn>\
            <hit id='8' sf='y'/>\
            <appt id='9' l='10' name='Appt' loc='Room' d='1700000000000' dur='3600000'\
             invId='9-1' compNum='0' fba='B' transp='O' status='CONF' class='PUB'\
             ptst='AC' allDay='0' isOrg='1' priority='5' percentComplete='0'>\
            <category>work</category>\
            <inst s='1700000000000' tzo='3600000'/></appt>\
            </SearchResponse>""";
    return new ZSearchResult(Element.parseXML(xml), false, TimeZone.getTimeZone("UTC"));
  }

  static ZContact createContact() throws Exception {
    String xml =
        """
            <cn id='6' l='7' f='f' t='1' d='1700000000000' md='1700000000' rev='3'>\
            <a n='firstName'>John</a><a n='email'>john@test.com</a></cn>""";
    return new ZContact(Element.parseXML(xml), null);
  }

  static ZTag createTag() throws Exception {
    return new ZTag(Element.parseXML("<tag id='64' name='Important' color='2' u='3'/>"), null);
  }

  static ZSignature createSignature() {
    return new ZSignature("s1", "Work", "Bye", "text/plain");
  }

  static ZAce createAce() throws Exception {
    return new ZAce(Element.parseXML("<ace gt='usr' d='bob@test.com' zid='z1' right='sendAs'/>"));
  }

  private static void assertJson(String expectedResource, ToZJSONObject object) throws Exception {
    JsonNode expected = MAPPER.readTree(readResource(expectedResource));
    JsonNode actual = MAPPER.readTree(object.toObjectNode().toString());
    assertEquals(expected, actual);
  }

  private static String readResource(String name) throws IOException {
    try (InputStream in = ToObjectNodeTest.class.getResourceAsStream("to-object-node/" + name)) {
      return new String(in.readAllBytes(), StandardCharsets.UTF_8);
    }
  }
}
