// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.redolog;

import com.zimbra.common.service.ServiceException;
import java.io.File;

/**
 * Mock {@link RedoLogProvider} for unit test.
 *
 * @author ysasaki
 */
public class MockRedoLogProvider extends RedoLogProvider {

  public MockRedoLogProvider() {
    mRedoLogManager =
        new RedoLogManager(
            new File("build/test/redo/redo.log"), new File("build/test/redo"), false);
  }

  @Override
  public boolean isMaster() {
    return true;
  }

  @Override
  public boolean isSlave() {
    return false;
  }

  @Override
  public void startup() throws ServiceException {}

  @Override
  public void shutdown() throws ServiceException {}

  @Override
  public void initRedoLogManager() {}
}
