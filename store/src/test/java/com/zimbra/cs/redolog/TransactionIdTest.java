// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: AGPL-3.0-only

package com.zimbra.cs.redolog;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.zimbra.common.service.ServiceException;
import org.junit.jupiter.api.Test;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

public class TransactionIdTest {
 @Test
 void defaultId() throws Exception {
  TransactionId id = new TransactionId();
  assertEquals(id, id);
  assertEquals(0, id.hashCode());
  assertEquals(0, id.getTime());
  assertEquals(0, id.getCounter());
  assertEquals("0-0", id.encodeToString());
 }

    public void id() throws Exception {
        TransactionId id = new TransactionId(1112, 5);
        assertEquals(id, id);
        assertEquals(5, id.hashCode());
        assertEquals(1112, id.getTime());
        assertEquals(5, id.getCounter());
        assertEquals("1112-5", id.encodeToString());
    }

 @Test
 void stringEncodeDecode() throws Exception {
  TransactionId id = new TransactionId(5, 188);
  String encoded = id.encodeToString();
  assertEquals("5-188", encoded);
  assertEquals(id, TransactionId.decodeFromString(encoded), "mismatch on decode.");
 }

 @Test
 void stringBadDecode() throws Exception {
  assertThrows(ServiceException.class, () -> {
   TransactionId.decodeFromString("not-valid");
  });
 }

 @Test
 void streamEncodeDecode() throws Exception {
  TransactionId id = new TransactionId(5, 188);
  ByteArrayOutputStream os = new ByteArrayOutputStream();
  RedoLogOutput redoOut = new RedoLogOutput(os);

  id.serialize(redoOut);
  assertEquals(8, os.size());

  RedoLogInput redoIn =
    new RedoLogInput(new ByteArrayInputStream(os.toByteArray()));

  TransactionId newId = new TransactionId();
  newId.deserialize(redoIn);

  assertEquals(id, newId, "mismatch on deserialize");
 }
}
