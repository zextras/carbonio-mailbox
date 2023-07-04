// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.mailbox.util;

import java.util.Collection;

import org.junit.jupiter.api.Test;

import com.google.common.collect.Multimap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.zimbra.cs.mailbox.MailItem.Type;
import com.zimbra.cs.mailbox.util.PagedDelete;
import com.zimbra.cs.mailbox.util.TypedIdList;


/**
 * @author zimbra
 *
 */
public class PagedDeleteTest {

 @Test
 void testTrimDeletesTillPageLimit() {
  TypedIdList tombstone = new TypedIdList();
  tombstone.add(Type.MESSAGE, 3, "", 100);
  tombstone.add(Type.MESSAGE, 4, "", 101);
  tombstone.add(Type.MESSAGE, 1, "", 101);
  tombstone.add(Type.MESSAGE, 5, "", 100);
  tombstone.add(Type.MESSAGE, 9, "", 103);
  PagedDelete pgDelete = new PagedDelete(tombstone, false);
  pgDelete.trimDeletesTillPageLimit(3);
  Collection<Integer> ids = pgDelete.getAllIds();
  assertEquals(3, ids.size());
  assertTrue(ids.contains(3));
  assertTrue(ids.contains(5));
  assertTrue(ids.contains(1));
  assertTrue(pgDelete.isDeleteOverFlow());
  assertEquals(pgDelete.getCutOffModsequnce(), 101);
  assertEquals(pgDelete.getLastItemId(), 4);
 }

 @Test
 void testTypedDeletesTillPageLimit() {
  TypedIdList tombstone = new TypedIdList();
  tombstone.add(Type.MESSAGE, 3, "", 100);
  tombstone.add(Type.MESSAGE, 4, "", 101);
  tombstone.add(Type.APPOINTMENT, 1, "", 101);
  tombstone.add(Type.APPOINTMENT, 5, "", 100);
  tombstone.add(Type.MESSAGE, 9, "", 103);
  PagedDelete pgDelete = new PagedDelete(tombstone, true);
  pgDelete.trimDeletesTillPageLimit(3);
  Collection<Integer> ids = pgDelete.getAllIds();
  assertEquals(3, ids.size());
  assertTrue(ids.contains(3));
  assertTrue(ids.contains(5));
  assertTrue(ids.contains(1));
  assertTrue(pgDelete.isDeleteOverFlow());
  assertEquals(pgDelete.getCutOffModsequnce(), 101);
  assertEquals(pgDelete.getLastItemId(), 4);

  Multimap<Type, Integer> ids2Type = pgDelete.getTypedItemIds();
  assertEquals(3, ids2Type.size());
  assertTrue(ids2Type.containsEntry(Type.MESSAGE, 3));
  assertTrue(ids2Type.containsEntry(Type.APPOINTMENT, 5));
  assertTrue(ids2Type.containsEntry(Type.APPOINTMENT, 1));
 }

 @Test
 void testRemoveBeforeCutoff() {
  TypedIdList tombstone = new TypedIdList();
  tombstone.add(Type.MESSAGE, 3, "", 100);
  tombstone.add(Type.MESSAGE, 4, "", 101);
  tombstone.add(Type.APPOINTMENT, 1, "", 101);
  tombstone.add(Type.APPOINTMENT, 5, "", 100);
  tombstone.add(Type.APPOINTMENT, 9, "", 103);
  tombstone.add(Type.MESSAGE, 2, "", 99);
  tombstone.add(Type.MESSAGE, 22, "", 105);
  tombstone.add(Type.MESSAGE, 24, "", 103);
  PagedDelete pgDelete = new PagedDelete(tombstone, true);
  pgDelete.removeBeforeCutoff(4, 101);
  Collection<Integer> ids = pgDelete.getAllIds();
  assertEquals(4, ids.size());
  pgDelete.trimDeletesTillPageLimit(3);
  ids = pgDelete.getAllIds();
  assertEquals(3, ids.size());
  assertTrue(ids.contains(4));
  assertTrue(ids.contains(9));
  assertTrue(ids.contains(24));
  assertTrue(pgDelete.isDeleteOverFlow());
  assertEquals(pgDelete.getCutOffModsequnce(), 105);
  assertEquals(pgDelete.getLastItemId(), 22);
  Multimap<Type, Integer> ids2Type = pgDelete.getTypedItemIds();
  assertEquals(3, ids2Type.size());
  assertTrue(ids2Type.containsEntry(Type.MESSAGE, 4));
  assertTrue(ids2Type.containsEntry(Type.APPOINTMENT, 9));
  assertTrue(ids2Type.containsEntry(Type.MESSAGE, 24));
 }

 @Test
 void testRemoveBeforeAfterCutoff() {
  TypedIdList tombstone = new TypedIdList();
  tombstone.add(Type.MESSAGE, 3, "", 100);
  tombstone.add(Type.MESSAGE, 4, "", 101);
  tombstone.add(Type.APPOINTMENT, 1, "", 101);
  tombstone.add(Type.APPOINTMENT, 5, "", 100);
  tombstone.add(Type.APPOINTMENT, 9, "", 103);
  tombstone.add(Type.MESSAGE, 2, "", 99);
  tombstone.add(Type.MESSAGE, 22, "", 105);
  tombstone.add(Type.MESSAGE, 24, "", 103);
  tombstone.add(Type.MESSAGE, 28, "", 106);
  PagedDelete pgDelete = new PagedDelete(tombstone, true);
  pgDelete.removeBeforeCutoff(4, 101);
  Collection<Integer> ids = pgDelete.getAllIds();
  assertEquals(5, ids.size());
  pgDelete.trimDeletesTillPageLimit(5);
  ids = pgDelete.getAllIds();
  assertEquals(5, ids.size());
  pgDelete.removeAfterCutoff(105);
  ids = pgDelete.getAllIds();
  assertEquals(4, ids.size());
  assertTrue(ids.contains(4));
  assertTrue(ids.contains(9));
  assertTrue(ids.contains(24));
  assertTrue(ids.contains(22));
  assertTrue(pgDelete.isDeleteOverFlow());
  assertEquals(pgDelete.getCutOffModsequnce(), 106);
  assertEquals(pgDelete.getLastItemId(), 28);
  Multimap<Type, Integer> ids2Type = pgDelete.getTypedItemIds();
  assertEquals(4, ids2Type.size());
  assertTrue(ids2Type.containsEntry(Type.MESSAGE, 4));
  assertTrue(ids2Type.containsEntry(Type.APPOINTMENT, 9));
  assertTrue(ids2Type.containsEntry(Type.MESSAGE, 24));
  assertTrue(ids2Type.containsEntry(Type.MESSAGE, 22));
 }

}
