// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.common.util;

import java.io.OutputStream;

/**
 * <tt>OutputStream</tt> that does nothing. This is the most brilliant piece of code I've ever
 * written.
 *
 * @author bburtin
 */
public class DevNullOutputStream extends OutputStream {

  @Override
  public void write(int b) {}

  @Override
  public void write(byte[] b, int off, int len) {}
}
