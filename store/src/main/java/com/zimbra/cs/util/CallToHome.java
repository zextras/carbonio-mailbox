// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.util;

import com.zimbra.common.service.ServiceException;
import com.zimbra.common.util.StringUtil;
import com.zimbra.common.util.ZimbraHttpConnectionManager;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.cs.account.Domain;
import com.zimbra.cs.account.NamedEntry;
import com.zimbra.cs.account.Provisioning;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

class CallToHomeRunner {
  private static final long MILLIS_IN_DAY = 1000L * 60L * 60L * 24L;
  private static final Timer timer = new Timer(true);
  private static final boolean LOGGING = false;
  private static boolean started = false;

  public static boolean isStarted() {
    return started;
  }

  public static void init() {
    if (isStarted()) {
      if (LOGGING) {
        ZimbraLog.mailbox.info("CallToHome: already running");
      }
      return;
    }
    Timer t = new Timer();
    // start service after 5 minutes
    t.schedule(
        new TimerTask() {
          @Override
          public void run() {
            CallToHome task = new CallToHome(LOGGING);
            timer.scheduleAtFixedRate(task, 0L, MILLIS_IN_DAY);
            started = true;
            if (LOGGING) {
              ZimbraLog.mailbox.info("CallToHome: Started");
            }
            t.cancel();
          }
        },
        300000L);
  }

  public boolean isRunning() {
    return started;
  }

  public void stop() {
    timer.cancel();
    started = false;
    if (LOGGING) {
      ZimbraLog.mailbox.info("CallToHome: Stopped");
    }
  }
}

public class CallToHome extends TimerTask {

  private static final String UPDATE_URL = "https://updates.zextras.com/openchat";
  private static boolean logging = false;
  Provisioning prov = Provisioning.getInstance();

  public CallToHome(boolean logging) {
    CallToHome.logging = logging;
  }

  @Override
  public void run() {
    if (logging) {
      ZimbraLog.mailbox.info("CallToHome: Running");
    }
    try {
      postDataToEndpoint(getData());
    } catch (IOException e) {
      if (logging) {
        ZimbraLog.mailbox.info("CallToHome: response: " + e.getMessage());
      }
    }
  }

  public void postDataToEndpoint(JSONObject data) throws IOException {
    if (logging) {
      throw new IOException("logging is enabled preventing posting data");
    }
    HttpPost post = new HttpPost(UPDATE_URL);
    post.setHeader("Content-Type", "application/json; charset=UTF-8");
    post.setEntity(new StringEntity(data.toString(), "UTF-8"));
    if (logging) {
      ZimbraLog.mailbox.info(
          "CallToHome: started posting data: " + IOUtils.toString(post.getEntity().getContent()));
    }
    CloseableHttpClient client =
        ZimbraHttpConnectionManager.getInternalHttpConnMgr().newHttpClient().build();
    try {
      CloseableHttpResponse httpResp = client.execute(post);
      int statusCode = httpResp.getStatusLine().getStatusCode();
      if (statusCode != HttpStatus.SC_OK) {
        throw new IOException(
            "request returned with status "
                + statusCode
                + ":"
                + httpResp.getStatusLine().getReasonPhrase(),
            null);
      } else {
        if (logging) {
          ZimbraLog.mailbox.info("CallToHome: OK");
        }
      }
    } catch (IOException e) {
      throw new IOException("unexpected error during post operation: " + e.getMessage());
    } finally {
      post.releaseConnection();
    }
  }

  private JSONObject getData() {
    JSONObject json = new JSONObject();
    try {
      json.put("zimbraVersion", getCurrentZimbraVersion());
      json.put("isNetwork", false);
      json.put("serverName", getLocalHostname());
      json.put("numAccounts", getAccountCounts(true));
    } catch (JSONException | ServiceException e) {
      if (logging) {
        ZimbraLog.mailbox.info("CallToHome: " + e.getMessage());
      }
    }
    return json;
  }

  private String getLocalHostname() {
    String localHostname = "unknown";
    try {
      localHostname = prov.getLocalServer().getName();
    } catch (ServiceException e) {
      if (logging) {
        ZimbraLog.mailbox.info(
            "CallToHome: Unable to determine local hostname using "
                + localHostname
                + " as localHostname",
            e);
      }
    }
    return localHostname;
  }

  private String getCurrentZimbraVersion() {
    String versionInfo = BuildInfo.VERSION;
    if (StringUtil.isNullOrEmpty(versionInfo)) versionInfo = "unknown";
    return versionInfo;
  }

  private int getAccountCounts(boolean usingVisitor) throws ServiceException {
    if (logging) {
      ZimbraLog.mailbox.info("CallToHome: counting using " + (usingVisitor ? "visitor" : "stream"));
    }
    AtomicInteger accountCount = new AtomicInteger();
    if (usingVisitor) {
      NamedEntry.Visitor visitor =
          entry -> {
            Provisioning.CountAccountResult result = prov.countAccount((Domain) entry);
            accountCount.addAndGet(
                result.getCountAccountByCos().stream()
                    .mapToInt(c -> (int) c.getCount())
                    .reduce(0, Integer::sum));
          };
      prov.getAllDomains(visitor, new String[] {Provisioning.A_zimbraId});
    } else {
      prov.getAllDomains().parallelStream()
          .forEach(
              domain -> {
                try {
                  accountCount.addAndGet(
                      prov.countAccount(domain).getCountAccountByCos().stream()
                          .mapToInt(c -> (int) c.getCount())
                          .reduce(0, Integer::sum));
                } catch (ServiceException e) {
                  if (logging) {
                    ZimbraLog.mailbox.error("CallToHome: unable to get domains", e);
                  }
                }
              });
    }
    return accountCount.get();
  }
}
