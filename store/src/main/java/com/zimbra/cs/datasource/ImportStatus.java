// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

/** */
package com.zimbra.cs.datasource;

import com.zimbra.cs.account.DataSource;

/**
 * Keeps track of the status of an import from a {@link DataSource}.
 *
 * @author bburtin
 */
public class ImportStatus {
  private String mDataSourceId;
  boolean mIsRunning = false;
  boolean mSuccess = false;
  String mError = null;
  boolean mHasRun = false;

  ImportStatus(String dataSourceId) {
    mDataSourceId = dataSourceId;
  }

  ImportStatus(ImportStatus status) {
    mDataSourceId = status.mDataSourceId;
    mIsRunning = status.isRunning();
    mSuccess = status.getSuccess();
    mError = status.getError();
    mHasRun = status.hasRun();
  }

  public String getDataSourceId() {
    return mDataSourceId;
  }

  public boolean isRunning() {
    return mIsRunning;
  }

  public boolean getSuccess() {
    return mSuccess;
  }

  public String getError() {
    return mError;
  }

  /** Returns <code>true</code> if an import process has ever started on this data source. */
  public boolean hasRun() {
    return mHasRun;
  }

  @Override
  public String toString() {
    return String.format(
        "ImportStatus: { dataSourceId=%s, isRunning=%b, success=%b, error=%s, hasRun=%b }",
        mDataSourceId, mIsRunning, mSuccess, mError, mHasRun);
  }
}
