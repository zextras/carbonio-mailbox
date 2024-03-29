// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.server;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class ServerConfigTest {

    private static final String SINGLE_IP = "1.2.3.4";
    private static final String SINGLE_HOSTNAME = "host.example.com";

    private static final String MULTI_ADDR0 = "1.2.3.4";
    private static final String MULTI_ADDR1 = "host2.example.com";
    private static final String MULTI_ADDR2 = "8.9.10.11";

 @Test
 void singleIp() {
  String[] single = {SINGLE_IP};
  String[] get = ServerConfig.getAddrListCsv(single);
  assertEquals(1, get.length);
  assertEquals(SINGLE_IP, get[0]);
 }

 @Test
 void singleHostname() {
  String[] single = {SINGLE_HOSTNAME};
  String[] get = ServerConfig.getAddrListCsv(single);
  assertEquals(1, get.length);
  assertEquals(SINGLE_HOSTNAME, get[0]);
 }

 @Test
 void empty() {
  String[] empty = {};
  String[] get = ServerConfig.getAddrListCsv(empty);
  assertEquals(0, get.length);
 }


 @Test
 void multiAddrsSingleString() {
  String[] multi = {MULTI_ADDR0 + "," + MULTI_ADDR1 + "," + MULTI_ADDR2};
  assertEquals(1, multi.length);
  String[] get = ServerConfig.getAddrListCsv(multi);
  assertEquals(3, get.length);
  assertEquals(MULTI_ADDR0, get[0]);
  assertEquals(MULTI_ADDR1, get[1]);
  assertEquals(MULTI_ADDR2, get[2]);
 }
}
