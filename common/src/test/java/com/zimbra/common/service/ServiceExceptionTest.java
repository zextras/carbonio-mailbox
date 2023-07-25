// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.common.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;


import org.junit.jupiter.api.Test;

import com.zimbra.common.service.ServiceException.Argument;

public class ServiceExceptionTest {

  @Test
  void testArgumentEquals() {
    Argument arg1a = new Argument("1", "one", Argument.Type.STR);
    Argument arg1b = new Argument("1", "one", Argument.Type.STR);
    Argument arg1c = new Argument("1", "two", Argument.Type.STR);
    Argument arg2 = new Argument("2", "one", Argument.Type.STR);

    assertNotEquals(arg1a, null);
    assertEquals(arg1a, arg1b);
    assertNotEquals(arg1a, arg1c);
    assertNotEquals(arg1a, arg2);
  }
}
