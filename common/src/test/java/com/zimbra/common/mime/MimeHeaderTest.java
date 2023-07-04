// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.common.mime;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.charset.Charset;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.google.common.base.Strings;
import com.zimbra.common.localconfig.LC;
import com.zimbra.common.localconfig.LocalConfigTestUtil;
import com.zimbra.common.util.CharsetUtil;
import com.zimbra.common.zmime.ZInternetHeader;

public class MimeHeaderTest {

    @BeforeAll
    public static void init() throws Exception {
        if (Strings.isNullOrEmpty(System.getProperty("zimbra.config"))) {
      System.setProperty("zimbra.config", "../store/src/test/resources/localconfig-test.xml");
    }
  }

    @AfterEach
    public void reset() {
        LocalConfigTestUtil.resetLC(LC.mime_split_address_at_semicolon);
    }

  @SuppressWarnings("deprecation")
  @Test
  void addressHeader() {
    String src = "mine:=?us-ascii?Q?Bob_?=\t=?us-ascii?Q?the_Builder_1?= <bob@example.com>;,=?us-ascii?Q?Bob the Builder 2?= <bob@example.com>";
    MimeAddressHeader hdr = new MimeAddressHeader("To", src);
    List<InternetAddress> iaddrs = hdr.expandAddresses();
    assertEquals(2, iaddrs.size());
    assertEquals("Bob the Builder 1", iaddrs.get(0).getPersonal());
    assertEquals("bob@example.com", iaddrs.get(0).getAddress());
    assertEquals("Bob the Builder 2", iaddrs.get(1).getPersonal());
    assertEquals("bob@example.com", iaddrs.get(1).getAddress());
  }

  @Test
  void encode() {
    String src = "Re: Pru\u00ee Loo";
    assertEquals("=?utf-8?Q?Re=3A_Pru=C3=AE?= Loo", MimeHeader.escape(src, null, true));
    assertEquals("Re: =?utf-8?B?UHJ1w64=?= Loo", MimeHeader.escape(src, null, false));

    src = "Re: Pru\u00ee Loo ";
    assertEquals("=?utf-8?Q?Re=3A_Pru=C3=AE_Loo_?=", MimeHeader.escape(src, null, true));
    assertEquals("Re: =?utf-8?Q?Pru=C3=AE_Loo_?=", MimeHeader.escape(src, null, false));

    src = "Fwd:   Pru\u00ee Loo";
    assertEquals("=?utf-8?Q?Fwd=3A___Pru=C3=AE?= Loo", MimeHeader.escape(src, null, true));
    assertEquals("Fwd:   =?utf-8?B?UHJ1w64=?= Loo", MimeHeader.escape(src, null, false));

    src = "Prue Loo  ";
    assertEquals("\"Prue Loo  \"", MimeHeader.escape(src, null, true));
    assertEquals("Prue Loo  ", MimeHeader.escape(src, null, false));

    src = "Fwd:  Pru\u00ee Loo ";
    assertEquals("=?utf-8?Q?Fwd=3A__Pru=C3=AE_Loo_?=", MimeHeader.escape(src, null, true));
    assertEquals("Fwd:  =?utf-8?Q?Pru=C3=AE_Loo_?=", MimeHeader.escape(src, null, false));

    src = "Prue  Loo";
    assertEquals("\"Prue  Loo\"", MimeHeader.escape(src, null, true));
    assertEquals("Prue  Loo", MimeHeader.escape(src, null, false));

    src = "Prue   Loo";
    assertEquals("\"Prue   Loo\"", MimeHeader.escape(src, null, true));
    assertEquals("Prue   Loo", MimeHeader.escape(src, null, false));

    src = " Prue  Loo ";
    assertEquals("\" Prue  Loo \"", MimeHeader.escape(src, null, true));
    assertEquals(" Prue  Loo ", MimeHeader.escape(src, null, false));

    src = "Pru\u00ee";
    assertEquals("=?utf-8?B?UHJ1w64=?=", MimeHeader.escape(src, null, true));
    assertEquals("=?utf-8?B?UHJ1w64=?=", MimeHeader.escape(src, null, false));

    src = "Pru\u00ee";
    assertEquals("=?iso-8859-1?Q?Pru=EE?=", MimeHeader.escape(src, CharsetUtil.ISO_8859_1, true));
    assertEquals("=?iso-8859-1?Q?Pru=EE?=", MimeHeader.escape(src, CharsetUtil.ISO_8859_1, false));

    Charset iso_8859_7 = CharsetUtil.toCharset("iso-8859-7");
    if (iso_8859_7 != null) {
      src = "Pru\u00ee";
      assertEquals("=?utf-8?B?UHJ1w64=?=", MimeHeader.escape(src, iso_8859_7, true));
      assertEquals("=?utf-8?B?UHJ1w64=?=", MimeHeader.escape(src, iso_8859_7, false));
    }

    src = "lskdhf lkshfl aksjhlfi ahslkfu Pru\u00ee uey liufhlasuifh haskjhf lkajshf lkajshflkajhslkfj hals\u00e4kjhf laskjhdflaksjh ksjfh ka";
    assertEquals(
        "lskdhf lkshfl aksjhlfi ahslkfu =?utf-8?Q?Pru=C3=AE_uey_liufhlasuifh_haskjhf_lkajshf_lkajshflkajhslkfj_hals=C3=A4kjhf?= laskjhdflaksjh ksjfh ka",
        MimeHeader.escape(src, null, true));
    assertEquals(
        "lskdhf lkshfl aksjhlfi ahslkfu =?utf-8?Q?Pru=C3=AE_uey_liufhlasuifh_haskjhf_lkajshf_lkajshflkajhslkfj_hals=C3=A4kjhf?= laskjhdflaksjh ksjfh ka",
        MimeHeader.escape(src, null, false));

    src = "\u00eb\u00ec\u00ed\u00ee";
    assertEquals("=?utf-8?B?w6vDrMOtw64=?=", MimeHeader.escape(src, null, true));
    assertEquals("=?utf-8?B?w6vDrMOtw64=?=", MimeHeader.escape(src, null, false));

    src = "\u00eb\u00ec\u00ed\u00ee";
    assertEquals("=?iso-8859-1?B?6+zt7g==?=", MimeHeader.escape(src, CharsetUtil.ISO_8859_1, true));
    assertEquals("=?iso-8859-1?B?6+zt7g==?=", MimeHeader.escape(src, CharsetUtil.ISO_8859_1, false));

  }

  @Test
  void decode() {
    String src = "RE: [Bug 30944]=?UTF-8?Q?=20Meeting=20invitation=20that=E2=80=99s=20created=20within=20exchange=20containing=20=C3=A5=C3=A4=C3=B6=20will=20show=20within=20the=20calendar=20and=20acceptance=20notification=20as=20?=?????";
    assertEquals(
        "RE: [Bug 30944] Meeting invitation that\u2019s created within exchange containing \u00e5\u00e4\u00f6 will show within the calendar and acceptance notification as ?????",
        MimeHeader.decode(src));

    src = "=?utf-8?Q?Hambone_x?=";
    assertEquals("Hambone x", MimeHeader.decode(src));

    src = "=?utf-8?Q?Ha?==?utf-8?Q?mbone?= x";
    assertEquals("Hambone x", MimeHeader.decode(src));

    src = "=?utf-8?Q?Ha?=    =?utf-8?Q?mbone x?=";
    assertEquals("Hambone x", MimeHeader.decode(src));

    src = "=?utf-8?Q?Ha?=  m =?utf-8?Q?bone?=";
    assertEquals("Ha  m bone", MimeHeader.decode(src));

    src = "=?utf-8?Q?Ha?= \r\n m =?utf-8?Q?bone?=";
    assertEquals("Ha  m bone", MimeHeader.decode(src));

    src = "=?utf-8?Q?Ha?=    =?utf-8??mbone?=";
    assertEquals("Ha    =?utf-8??mbone?=", MimeHeader.decode(src));

    src = "=?utf-8??Broken?=";
    assertEquals(src, MimeHeader.decode(src));

    src = "test\r\n one";
    assertEquals("test one", MimeHeader.decode(src));

    src = "1564 =?ISO-8859-1?Q?boo_1565_?=\n =?ISO-8859-1?Q?hoo?=";
    assertEquals("1564 boo 1565 hoo", MimeHeader.decode(src));
  }

  @Test
  void unfold() {
    assertEquals("dog", MimeHeader.unfold("dog"));
    assertEquals("dog", MimeHeader.unfold("dog\n"));
    assertEquals("dog", MimeHeader.unfold("\ndog"));
    assertEquals("dog cat", MimeHeader.unfold("dog\n cat"));
  }

  @Test
  void charsetSuperset() {
    // decoding should automatically fall back to the superset charset
    boolean hasWindows1252 = CharsetUtil.toCharset(MimeConstants.P_CHARSET_WINDOWS_1252) != null;
    String euro200 = "=?ISO-8859-1?Q?=80200?=";
    byte[] euro300 = new byte[]{(byte) 0x80, '3', '0', '0'};
    if (hasWindows1252) {
      assertEquals("\u20ac200", MimeHeader.decode(euro200));
      assertEquals("\u20ac300", new MimeHeader("X-Cost", euro300).getValue("iso-8859-1"));
    } else {
      assertEquals("\u0080200", MimeHeader.decode(euro200));
      assertEquals("\u0080300", new MimeHeader("X-Cost", euro300).getValue("iso-8859-1"));
    }

    // encoding should *not* use a superset charset
    assertEquals("=?UTF-8?Q?=E2=82=AC200000?=", MimeHeader.escape("\u20ac200000", CharsetUtil.ISO_8859_1, true).toUpperCase());
  }

  @SuppressWarnings("deprecation")
  @Test
  void semiColonSeparatedGroup() {
    for (int i = 0;i < 2;i++) {
      //this one should be the same regardless of split at semicolor for addresses on/off
      String src = "mine:=?us-ascii?Q?Bob_?=\t=?us-ascii?Q?the_Builder_1?= <bob@example.com>, Group User 2 <user2@group.com>;=?us-ascii?Q?Bob the Builder 2?= <bob@example.com>";
      MimeAddressHeader hdr = new MimeAddressHeader("To", src);
      List<InternetAddress> iaddrs = hdr.expandAddresses();
      assertEquals(3, iaddrs.size());
      assertEquals("Bob the Builder 1", iaddrs.get(0).getPersonal());
      assertEquals("bob@example.com", iaddrs.get(0).getAddress());
      assertEquals("Group User 2", iaddrs.get(1).getPersonal());
      assertEquals("user2@group.com", iaddrs.get(1).getAddress());
      assertEquals("Bob the Builder 2", iaddrs.get(2).getPersonal());
      assertEquals("bob@example.com", iaddrs.get(2).getAddress());
      LocalConfigTestUtil.setLC(LC.mime_split_address_at_semicolon, "false");
    }
  }

  @SuppressWarnings("deprecation")
  @Test
  void semiColonSeparatedGroupMixed() {
    String src = "mine:=?us-ascii?Q?Bob_?=\t=?us-ascii?Q?the_Builder_1?= <bob@example.com>, Group User 2 <user2@group.com>;=?us-ascii?Q?Bob the Builder 2?= <bob@example.com>;=?us-ascii?Q?Bob the Builder 3?= <bob3@example.com>";
    MimeAddressHeader hdr = new MimeAddressHeader("To", src);
    List<InternetAddress> iaddrs = hdr.expandAddresses();
    assertEquals(4, iaddrs.size());
    assertEquals("Bob the Builder 1", iaddrs.get(0).getPersonal());
    assertEquals("bob@example.com", iaddrs.get(0).getAddress());
    assertEquals("Group User 2", iaddrs.get(1).getPersonal());
    assertEquals("user2@group.com", iaddrs.get(1).getAddress());
    assertEquals("Bob the Builder 2", iaddrs.get(2).getPersonal());
    assertEquals("bob@example.com", iaddrs.get(2).getAddress());
    assertEquals("Bob the Builder 3", iaddrs.get(3).getPersonal());
    assertEquals("bob3@example.com", iaddrs.get(3).getAddress());
    LocalConfigTestUtil.setLC(LC.mime_split_address_at_semicolon, "false");

    hdr = new MimeAddressHeader("To", src);
    iaddrs = hdr.expandAddresses();
    assertEquals(3, iaddrs.size());
    assertEquals("Bob the Builder 1", iaddrs.get(0).getPersonal());
    assertEquals("bob@example.com", iaddrs.get(0).getAddress());
    assertEquals("Group User 2", iaddrs.get(1).getPersonal());
    assertEquals("user2@group.com", iaddrs.get(1).getAddress());
    assertEquals("Bob the Builder 2", iaddrs.get(2).getPersonal());
    assertEquals("bob@example.com", iaddrs.get(2).getAddress());
  }

  @SuppressWarnings("deprecation")
  @Test
  void semiColonSeparated() {
    String src = "user1@example.com;user2@example.com";
    MimeAddressHeader hdr = new MimeAddressHeader("To", src);
    List<InternetAddress> iaddrs = hdr.expandAddresses();
    assertEquals(2, iaddrs.size());
    assertEquals("user1@example.com", iaddrs.get(0).getAddress());
    assertEquals("user2@example.com", iaddrs.get(1).getAddress());
  }

  @SuppressWarnings("deprecation")
  @Test
  void semiColonSeparationDisabled() {
    LocalConfigTestUtil.setLC(LC.mime_split_address_at_semicolon, "false");
    String src = "user1@example.com;user2@example.com";
    MimeAddressHeader hdr = new MimeAddressHeader("To", src);
    List<InternetAddress> iaddrs = hdr.expandAddresses();
    assertEquals(1, iaddrs.size());
    assertEquals(src, iaddrs.get(0).getAddress());
  }

  @Test
  void testMalformedEndcodedHeader() {
    String subjectHeaderInvalid = "=?euc-jp?B?=1B?=";
    String subjectHeaderValid = "=?utf-8?B?SGVsbG8gV29ybGQh?=";
    // if encoded header has malformed value, return the value as it is
    // without throwing exception
    assertEquals(subjectHeaderInvalid, ZInternetHeader.decode(subjectHeaderInvalid));
    // if encoded header has valid value, return the decoded string
    assertEquals("Hello World!", ZInternetHeader.decode(subjectHeaderValid));
  }
}
