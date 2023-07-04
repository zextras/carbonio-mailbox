// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.common.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.Map;

import org.junit.jupiter.api.Test;

/**
 * Unit test for {@link BlobMetaData}.
 *
 * @author ysasaki
 */
public final class BlobMetaDataTest {

  @Test
  void test() throws Exception {
    StringBuilder buf = new StringBuilder();
    BlobMetaData.encodeMetaData("name1", "value1", buf);
    BlobMetaData.encodeMetaData("name2", 1, buf);
    BlobMetaData.encodeMetaData("name3", 1L, buf);
    BlobMetaData.encodeMetaData("name4", true, buf);
    BlobMetaData.encodeMetaData("name5", false, buf);
    Map<Object, Object> map = BlobMetaData.decode(buf.toString());

    assertEquals("value1", BlobMetaData.getString(map, "name1"));
    assertEquals(1, BlobMetaData.getInt(map, "name2"));
    assertEquals(1L, BlobMetaData.getLong(map, "name3"));
    assertEquals(true, BlobMetaData.getBoolean(map, "name4"));
    assertEquals(false, BlobMetaData.getBoolean(map, "name5"));

    assertEquals("value1", BlobMetaData.getString(map, "no", "value1"));
    assertEquals(1, BlobMetaData.getInt(map, "no", 1));
    assertEquals(1L, BlobMetaData.getLong(map, "no", 1L));
    assertEquals(true, BlobMetaData.getBoolean(map, "no", true));
    assertEquals(false, BlobMetaData.getBoolean(map, "no", false));
  }

  @Test
  void lengthCorrupted() throws Exception {
    try {
      BlobMetaData.decode("x=10");
      fail();
    } catch (BlobMetaDataEncodingException expected) {
    }
  }

  @Test
  void valueCorrupted() throws Exception {
    try {
      BlobMetaData.decode("x=1:x");
      fail();
    } catch (BlobMetaDataEncodingException expected) {
    }
  }
}
