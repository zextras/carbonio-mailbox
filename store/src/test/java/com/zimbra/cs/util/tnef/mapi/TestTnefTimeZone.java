// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.util.tnef.mapi;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.util.TimeZone;

import net.freeutils.tnef.RawInputStream;

import org.junit.jupiter.api.Test;

public class TestTnefTimeZone {

 @Test
 void testTnefTimeZoneFromIntIndex() throws IOException {
  TimeZone tz = null;

  tz = TnefTimeZone.getTimeZone(13, true, null);
  assertNotNull(tz);
  assertEquals(tz.getRawOffset(), -28800000);
  assertEquals(tz.getDSTSavings(), 3600000);

  tz = TnefTimeZone.getTimeZone(31, true, null);
  assertNotNull(tz);
  assertEquals(tz.getRawOffset(), 0);
  assertEquals(tz.getDSTSavings(), 0);

  tz = TnefTimeZone.getTimeZone(60, true, null); //invalid index
  assertNull(tz);

  tz = TnefTimeZone.getTimeZone(24, false, null);
  assertNotNull(tz);
  assertEquals(tz.getID(), "Asia/Dubai");
 }


 @Test
 void testLittleEndianByteArrayToIntConversions() {
  int value = 40067898;
  byte[] leByteArray = intToleByteArray(value);
  assertEquals(leByteArrayToInt(leByteArray), value);
 }

 @Test
 void testTnefTimeZoneFromInputStream() throws IOException {
  TimeZone tz = null;
  RawInputStream ris = null;

  ris = new RawInputStream(intToleByteArray(13), 0, 4);
  tz = TnefTimeZone.getTimeZone(ris);
  assertNotNull(tz);
  assertEquals(tz.getRawOffset(), -28800000);
  assertEquals(tz.getDSTSavings(), 3600000);

  // don't observe daylight saving bit is set!!
  ris = new RawInputStream(new byte[]{13, 0, 0, (byte) 128}, 0, 4);
  tz = TnefTimeZone.getTimeZone(ris);
  assertNotNull(tz);
  assertEquals(tz.getRawOffset(), -28800000);
  assertEquals(tz.getID(), "Etc/GMT+8");
 }
    
    private static final byte[] intToleByteArray(int value) {
        return new byte[] {
                (byte)value,
                (byte)(value >>> 8),
                (byte)(value >>> 16),
                (byte)(value >>> 24)};
    }
    
    private static final int leByteArrayToInt(byte[] data) {
        return (data[3] << 24) | (data[2] << 16) | (data[1] << 8) | data[0];
    }
    
    public static void main(String args[]) {
        for (int i=0; i < 60; i++)
            try {
                System.out.println("Index " + i + ": " + TnefTimeZone.getTimeZone(i, true, null));
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
    }

}
