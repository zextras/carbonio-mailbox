// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.util.calltohome;

import com.zimbra.common.util.ZimbraLog;
import java.util.Timer;
import java.util.TimerTask;

public class CallToHomeRunner {

  private static final long MILLIS_IN_DAY = 1000L * 60L * 60L * 24L;
  private static Timer timer = new Timer(true);
  private static volatile boolean started = false;

  private CallToHomeRunner() {
  }

  public static CallToHomeRunner getInstance() {
    return Holder.INSTANCE;
  }

  public synchronized boolean isStarted() {
    return started;
  }

  public synchronized void init(long startupDelay) {
    if (isStarted()) {
      ZimbraLog.misc.debug("CallToHome: already running");
      return;
    }else{
      ZimbraLog.misc.debug("CallToHome: init(), scheduled to start in " + startupDelay + " milliseconds");
    }

    timer.schedule(
        new TimerTask() {
          @Override
          public void run() {
            CallToHome task = new CallToHome();
            timer.scheduleAtFixedRate(task, 0L, MILLIS_IN_DAY);
            started = true;
            ZimbraLog.misc.debug("CallToHome: Started");
          }
        },
        startupDelay
    );
  }

  public synchronized void stop() {
    if (!isStarted()) {
      ZimbraLog.misc.debug("CallToHome: not running");
      return;
    }

    timer.cancel();
    timer = new Timer(true);
    started = false;
    ZimbraLog.misc.debug("CallToHome: Stopped");
  }

  private static class Holder {

    private static final CallToHomeRunner INSTANCE = new CallToHomeRunner();
  }
}

