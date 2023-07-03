// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.service.formatter;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.google.common.collect.ListMultimap;

import static org.junit.jupiter.api.Assertions.*;

import com.zimbra.common.mailbox.ContactConstants;
import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.dav.resource.AddressObject;
import com.zimbra.cs.mailbox.Contact;
import com.zimbra.cs.mailbox.VCardParamsAndValue;

public final class VCardTest {

    private static String oddities =
            "BEGIN:VCARD\r\n" +
            "\r\n" +
            "FN\n" +
            " :dr. john doe\n" +
            "ADR;HOME;WORK:;;Hambone Ltd.\\N5 Main St.;Charlotte;NC;24243\n" +
            "EMAIL:foo@bar.con\n" +
            "EMAIL:bar@goo.com\nN:doe;john;\\;\\\\;dr.;;;;\nEND:VCARD\n";

 @Test
 void oddVCard1() throws Exception {
  List<VCard> cards = VCard.parseVCard(oddities);
  assertNotNull(cards, "List of cards");
  assertEquals(1, cards.size(), "Number of cards");
 }

    private static String encodings = "BEGIN:VCARD\r\n" +
            "\r\n" +
            "FN\n" +
            " :john doe\\, jr.\n" +
            "ORG:Zimbra;Marketing;Annoying Marketing\n" +
            "A.TEL;type=fax,WORK:+1-800-555-1212\n" +
            "TEL;type=home,work,voice:+1-800-555-1313\n" +
            "NOTE;QUOTED-PRINTABLE:foo=3Dbar\n" +
            "c.D.e.NOTE;ENCODING=B;charset=iso-8859-1:SWYgeW91IGNhbiByZWFkIHRoaXMgeW8=\n" +
            "END:VCARD\n";

 @Test
 void oddVCard2() throws ServiceException {
  List<VCard> cards = VCard.parseVCard(encodings);
  assertNotNull(cards, "List of cards");
  assertEquals(1, cards.size(), "Number of cards");
 }

    private static String multicards =
                "BEGIN : VCARD\n" +
                "FN\n" +
                " :john doe\\, jr.\nAGENT:\\nBEGIN:VCARD\\nEND:VCARD\n" +
                "END:VCARD";

 @Test
 void oddVCard3() throws ServiceException {
  List<VCard> cards = VCard.parseVCard(multicards);
  assertNotNull(cards, "List of cards");
  assertEquals(1, cards.size(), "Number of cards");
 }

    private static String wasCommentedOutInVCardMain =
            "BEGIN:VCARD\r\n" +
            "\r\n" +
            "FN\n" +
            " :john doe\n" +
            "A.TEL;WORK:+1-800-555-1212\n" +
            ".:?\n" +
            ":\n" +
            "END:VCARD\n";

 @Test
 void invalidVCard1() {
  try {
   VCard.parseVCard(wasCommentedOutInVCardMain);
   fail("Expected PARSE_ERROR");
  } catch (ServiceException e) {
   assertTrue(e.getMessage().startsWith("parse error:"),
     String.format("Should start with parse error: [%s]", e.getMessage()));
  }
 }

    private static String multiX =
            "BEGIN:VCARD\r\n" +
            "FN:john doe\r\n" +
            "X-FOO:one\r\n" +
            "X-FOO:two\r\n" +
            "END:VCARD\r\n";

 @Test
 void multiX() throws ServiceException {
  List<VCard> cards = VCard.parseVCard(multiX);
  assertNotNull(cards, "List of cards");
  assertEquals(1, cards.size(), "Number of cards");
  VCard vcard = cards.get(0);
  String xprops = vcard.fields.get(ContactConstants.A_vCardXProps);
  ListMultimap<String, VCardParamsAndValue> unknowns = Contact.decodeUnknownVCardProps(xprops);
  assertEquals(2, unknowns.size(), String.format("Number of unknown properties in %s", unknowns));
  List<VCardParamsAndValue> xfoos = unknowns.get("X-FOO");
  assertEquals(2, xfoos.size(), String.format("Number of X-FOO properties in %s", xfoos));
 }

    private static String multiX2 =
            "BEGIN:VCARD\r\n" +
            "FN:john doe\r\n" +
            "X-FOO:one,two\r\n" +
            "END:VCARD\r\n";

 @Test
 void multiX2() throws ServiceException {
  List<VCard> cards = VCard.parseVCard(multiX2);
  assertNotNull(cards, "List of cards");
  assertEquals(1, cards.size(), "Number of cards");
  VCard vcard = cards.get(0);
  String xprops = vcard.fields.get(ContactConstants.A_vCardXProps);
  ListMultimap<String, VCardParamsAndValue> unknowns = Contact.decodeUnknownVCardProps(xprops);
  assertEquals(1, unknowns.size(), String.format("Number of unknown properties in %s", unknowns));
  List<VCardParamsAndValue> xfoos = unknowns.get("X-FOO");
  assertEquals(1, xfoos.size(), String.format("Number of X-FOO properties in %s", xfoos));
 }

    private static String multiXwithParams =
            "BEGIN:VCARD\r\n" +
            "FN:john doe\r\n" +
            "X-FOO;P1=A;P2=B:one\r\n" +
            "X-FOO;P3=C:two\r\n" +
            "X-FOO:three\r\n" +
            "NON-STANDARD;PARAMETER=pValue:this is fun\r\n" +
            "END:VCARD\r\n";

 @Test
 void multiXwithParams() throws ServiceException {
  List<VCard> cards = VCard.parseVCard(multiXwithParams);
  assertNotNull(cards, "List of cards");
  assertEquals(1, cards.size(), "Number of cards");
  VCard vcard = cards.get(0);
  String outCard = vcard.getFormatted();
  assertEquals(multiXwithParams, outCard, "Round tripped card");
  String xprops = vcard.fields.get(ContactConstants.A_vCardXProps);
  ListMultimap<String, VCardParamsAndValue> xp = Contact.decodeUnknownVCardProps(xprops);
  assertEquals(4, xp.size(), String.format("Number of unknown properties in %s", xp));
  List<VCardParamsAndValue> xfoos = xp.get("X-FOO");
  assertEquals(3, xfoos.size(), "Number of xfoos ");
  VCardParamsAndValue pandv = xfoos.get(0);
  assertEquals("one", pandv.getValue(), "first x-foo value");
  Set<String> params = pandv.getParams();
  assertEquals(2, params.size(), "first x-foo Number of params ");
  assertTrue(params.contains("P1=A"), "first x-foo params contains P1=A");
  assertTrue(params.contains("P2=B"), "first x-foo params contains P2=B");
  pandv = xfoos.get(1);
  assertEquals("two", pandv.getValue(), "2nd x-foo value");
  params = pandv.getParams();
  assertEquals(1, params.size(), "2nd x-foo Number of params ");
  assertTrue(params.contains("P3=C"), "2nd x-foo params contains P3=C");
  pandv = xfoos.get(2);
  assertEquals("three", pandv.getValue(), "3rd x-foo value");
  params = pandv.getParams();
  assertEquals(0, params.size(), "3rd x-foo Number of params ");

  List<VCardParamsAndValue> nonstandards = xp.get("NON-STANDARD");
  assertEquals(1, nonstandards.size(), "Number of NON-STANDARD ");
  pandv = nonstandards.get(0);
  assertEquals("this is fun", pandv.getValue(), "non-standard value");
  params = pandv.getParams();
  assertTrue(params.contains("PARAMETER=pValue"),
    String.format("non-standard params '%s' contains PARAMETER=pValue", params));
  assertEquals(1, params.size(), "non-standard Number of params ");
 }

    private static String groupCard =
            "BEGIN:VCARD\n" +
            "VERSION:3.0\n" +
            "X-ADDRESSBOOKSERVER-KIND:group\n" +
            "X-ADDRESSBOOKSERVER-MEMBER:user1@example.org\n" +
            "X-ADDRESSBOOKSERVER-MEMBER:user03@example.com\n" +
            "PRODID:-//Apple Inc.//AddressBook 6.1//EN\n" +
            "UID:760d9b28-5a13-4880-b7eb-5769e6600fa3\n" +
            "FN:Changed Group\n" +
            "N:ChangedGroup;;;;\n" +
            "REV:20120503T194243Z\n" +
            "END:VCARD\n";

 @Test
 void groupCard() throws ServiceException {
  List<VCard> cards = VCard.parseVCard(groupCard);
  assertNotNull(cards, "List of cards");
  assertEquals(1, cards.size(), "Number of cards");
  VCard vcard = cards.get(0);
  String xprops = vcard.fields.get(ContactConstants.A_vCardXProps);
  ListMultimap<String, VCardParamsAndValue> xp = Contact.decodeUnknownVCardProps(xprops);
  assertEquals(3, xp.size(), String.format("Number of unknown properties in %s", xp));
  List<VCardParamsAndValue> kinds = xp.get(AddressObject.XABSKIND);
  assertEquals(1, kinds.size(), String.format("Number of %s", AddressObject.XABSKIND));
  List<VCardParamsAndValue> members = xp.get(AddressObject.XABSMEMBER);
  assertEquals(2, members.size(), String.format("Number of %s", AddressObject.XABSMEMBER));
 }

    private static String smallBusyMacAttach =
            "BEGIN:VCARD\n" +
            "VERSION:3.0\n" +
            "PRODID:-//BusyMac LLC//BusyContacts 1.0.2//EN\n" +
            "FN:John Smith\n" +
            "N:Smith;John;;;\n" +
            "REV:2015-04-05T09:51:09Z\n" +
            "UID:99E01E16-03B3-4487-AAEF-AEB496852C06\n" +
            "X-BUSYMAC-ATTACH;ENCODING=b;X-FILENAME=favicon.ico:AAABAAEAEBAAAAEAIABoBAAA\n" +
            " FgAAACgAAAAQAAAAIAAAAAEAIAAAAAAAQAQAABMLAAATCwAAAAAAAAAAAAAAAAAAw4cAY8OHAM\n" +
            " nDhwD8w4cA/8OHAP/DhwD/w4cA/8OHAP/DhwD/w4cA/8OHAP/DhwD8w4cAycOHAGMAAAAAw4cA\n" +
            " Y8OHAP/DhwD/w4cA/8OHAP/DhwD/w4cA/8OHAP/DhwD/w4cA/8OHAP/DhwD/w4cA/8OHAP/Dhw\n" +
            " D/w4cAY8OHAMnDhwD/w4cA/7yYSv/y5Mb/8uXH//Llx//z5sr/8+bK//Pmyv/z58v/8+bK/8qq\n" +
            " Y//DhwD/w4cA/8OHAMnDhwDhw4cA/8OHAP++q4D///////////////7////+//////////////\n" +
            " /////////Yyan/w4cA/8OHAP/DhwDhw4cA4cOHAP/DhwD/t4QR/9/azv//////5t3K/9StVv/b\n" +
            " t2b/27dm/9u3Z//cuGn/wpAh/8OHAP/DhwD/w4cA4cOHAOHDhwD/w4cA/8OHAP+2jzr/+fj2//\n" +
            " n49f/BnU7/w4cA/8OHAP/DhwD/w4cA/8OHAP/DhwD/w4cA/8OHAOHDhwDhw4cA/8OHAP/DhwD/\n" +
            " w4cA/7ihbf//////8u/p/8GRJv/DhwD/w4cA/8OHAP/DhwD/w4cA/8OHAP/DhwDhw4cA4cOHAP\n" +
            " /DhwD/w4cA/8OHAP/BhgP/0siz///////d1L//wYgI/8OHAP/DhwD/w4cA/8OHAP/DhwD/w4cA\n" +
            " 4cOHAOHDhwD/w4cA/8OHAP/DhwD/w4cA/7eIIP/n49v//////8e0iP/DhwD/w4cA/8OHAP/Dhw\n" +
            " D/w4cA/8OHAOHDhwDhw4cA/8OHAP/DhwD/w4cA/8OHAP/DhwD/rItA//39/P/6+vj/w6BQ/8OH\n" +
            " AP/DhwD/w4cA/8OHAP/DhwDhw4cA4cOHAP/DhwD/w4cA/8OHAP/DhwD/w4cA/8OHAP+8p3r//v\n" +
            " 79/+3p4v+8ix3/w4cA/8OHAP/DhwD/w4cA4cOHAOHDhwD/w4cA/8CHB//VsFz/3rxx/926bf/c\n" +
            " uWv/xadh//Ht5///////1suz/7+HCv/DhwD/w4cA/8OHAOHDhwDhw4cA/8OHAP+wjT//+/r5//\n" +
            " /////////////////////+/v7///////7+/v+8n17/w4cA/8OHAP/DhwDhw4cAycOHAP/DhwD/\n" +
            " t4gd/+bYuP/16tP/9OjP//Toz//06M//8+fN//Pozv/t4MH/vZIx/8OHAP/DhwD/w4cAycOHAG\n" +
            " DDhwD/w4cA/8OHAP/DhwD/w4cA/8OHAP/DhwD/w4cA/8OHAP/DhwD/w4cA/8OHAP/DhwD/w4cA\n" +
            " /8OHAGAAAAAAw4cAWsOHAMnDhwD8w4cA/8OHAP/DhwD/w4cA/8OHAP/DhwD/w4cA/8OHAP/Dhw\n" +
            " D8w4cAycOHAFoAAAAAgAEAAAAAAAAAAAAAAABoQAAAAAAAAPC/AAAAAAAAAAAAAAAAAAAiQAAA\n" +
            " AAAAAAAAAAAAAAAAAAAAAAAAgAEAAA==\n" +
            "X-BUSYMAC-MODIFIED-BY:Gren Elliot\n" +
            "X-CREATED:2015-04-05T09:50:44Z\n" +
            "END:VCARD\n";

 @Test
 void smallBusyMacAttach() throws ServiceException {
  List<VCard> cards = VCard.parseVCard(smallBusyMacAttach);
  assertNotNull(cards, "List of cards");
  assertEquals(1, cards.size(), "Number of cards");
  VCard vcard = cards.get(0);
  String xprops = vcard.fields.get(ContactConstants.A_vCardXProps);
  ListMultimap<String, VCardParamsAndValue> xp = Contact.decodeUnknownVCardProps(xprops);
  assertEquals(3, xp.size(), String.format("Number of unknown properties in %s", xp));
  List<VCardParamsAndValue> busymacAttaches = xp.get("X-BUSYMAC-ATTACH");
  assertEquals(1, busymacAttaches.size(), String.format("Number of %s", "X-BUSYMAC-ATTACH"));
  VCardParamsAndValue attach = busymacAttaches.get(0);
  assertTrue(attach.getValue().startsWith("AAABAAEAEBAAAAEAIABoBAAAF"),
    String.format("Value fo X-BUSYMAC-ATTACH property %s starts as expected", attach.getValue()));
  assertEquals(2, attach.getParams().size(), String.format("X-BUSYMAC-ATTACH params=%s number", attach.getParams()));
 }

    private static String noColon =
            "BEGIN:VCARD\n" +
            "VERSION:3.0\n" +
            "FN:John Smith\n" +
            "N:Smith;John;;;\n" +
            "NONSENSE and very long and not very exciting line which does not contain a colon\n" +
            "X-BUSYMAC-MODIFIED-BY:Gren Elliot\n" +
            "END:VCARD\n";

 @Test
 void noColon() throws ServiceException {
  try {
   VCard.parseVCard(noColon);
   fail("Should detect problem with property which doesn't contain a colon");
  } catch (ServiceException se) {
   assertTrue(se.getMessage().contains("parse error: missing ':'"),
     String.format("Exception msg [%s] contains 'parse error: missing ':']", se.getMessage()));
  }
 }
}