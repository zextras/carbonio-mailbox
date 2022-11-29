// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: AGPL-3.0-only

package com.zimbra.cs.ephemeral.migrate;

import com.zimbra.common.util.ZimbraLog;

public class ShutdownHook extends Thread {
    private AttributeMigration.CSVReports csvReports;
    private Exception exception;
    private boolean finished;

    public ShutdownHook () {
        this.csvReports = null;
        this.exception = null;
        this.finished = false;
    }

    public void setException(Exception exception) {
        this.exception = exception;
    }

    public void setFinished(boolean finished) {
        this.finished = finished;
    }

    public void setCSVReports(AttributeMigration.CSVReports csvReports) {
        this.csvReports = csvReports;
    }

    public void run() {
        if (this.finished) {
            csvReports.zimbraLogFinalSummary(true);
        }
        else {
            csvReports.zimbraLogFinalSummary(false);
            if (this.exception != null) {
                ZimbraLog.ephemeral.error("Failure during migration: %s", this.exception);
            }
        }
    }
}
