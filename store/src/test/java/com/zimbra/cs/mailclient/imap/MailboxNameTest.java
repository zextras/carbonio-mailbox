// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.mailclient.imap;
import org.junit.jupiter.api.Test;
import com.zimbra.cs.mailclient.ParseException;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MailboxNameTest {

 @Test
 void testAsciiName() throws ParseException {
  String name = "testname";
  assertEquals(name, MailboxName.decode(name).toString());
 }

 @Test
 void testSpaceName() throws ParseException {
  String name = "test name";
  assertEquals(name, MailboxName.decode(name).toString());
 }

 @Test
 void testTabName() throws ParseException {
  String name = "test\tname";
  assertEquals(name, MailboxName.decode(name).toString());
 }

 @Test
 void testUtf8Name() throws ParseException {
  String name = "T\u00E5\u00FFpa";
  assertEquals(name, MailboxName.decode(name).toString());
 }

 @Test
 void testUtf8TabName() throws ParseException {
  String name = "T\u00E5\t\u00FFpa";
  assertEquals(name, MailboxName.decode(name).toString());
 }

 @Test
 void testSpaceUtf8Name() throws ParseException {
  String name = "T\u00E5\u00FFpa test";
  assertEquals(name, MailboxName.decode(name).toString());
 }

 @Test
 void testUtf7Name() throws ParseException {
  String utf7 = "Skr&AOQ-ppost";
  String name = "Skr\u00E4ppost";
  assertEquals(name, MailboxName.decode(utf7).toString());
 }

 @Test
 void testUtf7SpaceName() throws ParseException {
  String utf7 = "Skr&AOQ-ppo st";
  String name = "Skr\u00E4ppo st";
  assertEquals(name, MailboxName.decode(utf7).toString());
 }

 @Test
 void testBadAsciiName() throws ParseException {
  String name = "test\u0016test";
  assertEquals("testtest", MailboxName.decode(name).toString());
 }

 @Test
 void testBadUtf8Name() throws ParseException {
  String name = "T\u00E5\u00FFpa\u0000";
  assertEquals("T\u00E5\u00FFpa", MailboxName.decode(name).toString());
 }

 @Test
 void testBadUtf7Name() throws ParseException {
  String utf7 = "Skr&AOQ-pp\u0015ost";
  assertEquals("Skr&AOQ-ppost", MailboxName.decode(utf7).toString());
 }

}
