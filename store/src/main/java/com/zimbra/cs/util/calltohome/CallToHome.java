// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.util.calltohome;

import com.zimbra.common.account.ZAttrProvisioning;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.util.StringUtil;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.cs.account.Domain;
import com.zimbra.cs.account.NamedEntry;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.httpclient.HttpProxyUtil;
import com.zimbra.cs.util.BuildInfo;
import java.io.IOException;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.json.JSONException;
import org.json.JSONObject;

public class CallToHome extends TimerTask {

  private static final String UPDATE_URL = "https://updates.zextras.com/openchat";
  private final Provisioning prov = Provisioning.getInstance();

  @Override
  public void run() {
    ZimbraLog.misc.debug("CallToHome: Running...");
    try {
      postDataToEndpoint(getData());
    } catch (IOException e) {
      ZimbraLog.misc.error("CallToHome: Error posting data", e);
    }
  }

  private void postDataToEndpoint(JSONObject data) throws IOException {
    var post = new HttpPost(UPDATE_URL);
    post.setHeader("Content-Type", "application/json; charset=UTF-8");
    post.setEntity(new StringEntity(data.toString(), "UTF-8"));
    ZimbraLog.misc.debug("CallToHome: Posting data: " + data);

    var httpClientBuilder = HttpClients.custom();
    HttpProxyUtil.configureProxy(httpClientBuilder);
    try (var client = httpClientBuilder.build();
        var httpResp = client.execute(post)) {
      var statusCode = httpResp.getStatusLine().getStatusCode();
      if (statusCode != HttpStatus.SC_OK) {
        var reason = httpResp.getStatusLine().getReasonPhrase();
        ZimbraLog.misc.error("CallToHome: Request returned with status " + statusCode + ": " + reason);
        throw new IOException("Request failed with status " + statusCode + ": " + reason);
      }

      ZimbraLog.misc.debug("CallToHome: Data posted successfully");

    } catch (IOException e) {
      ZimbraLog.misc.error("CallToHome: Unexpected error during post operation", e);
      throw e;
    }
  }

  private JSONObject getData() {
    var json = new JSONObject();
    try {
      json.put("zimbraVersion", getCurrentZimbraVersion());
      json.put("isNetwork", false);
      json.put("serverName", getLocalHostname());
      json.put("numAccounts", getAccountCounts());
    } catch (JSONException | ServiceException e) {
      ZimbraLog.misc.error("CallToHome: Error gathering data", e);
    }
    return json;
  }

  private String getLocalHostname() {
    try {
      return prov.getLocalServer().getName();
    } catch (ServiceException e) {
      ZimbraLog.misc.error("CallToHome: Unable to determine local hostname", e);
      return "unknown";
    }
  }

  private String getCurrentZimbraVersion() {
    var versionInfo = BuildInfo.VERSION;
    if (StringUtil.isNullOrEmpty(versionInfo)) {
      versionInfo = "unknown";
    }
    return versionInfo;
  }

  private int getAccountCounts() throws ServiceException {
    ZimbraLog.misc.debug("CallToHome: Counting accounts...");
    var accountCount = new AtomicInteger();

    var visitor = (NamedEntry.Visitor) entry -> {
      try {
        var result = prov.countAccount((Domain) entry);
        accountCount.addAndGet(result.getCountAccountByCos().stream()
            .mapToInt(c -> (int) c.getCount())
            .sum());
      } catch (ServiceException e) {
        ZimbraLog.misc.error("CallToHome: Error counting accounts for domain: " + entry.getName(), e);
      }
    };

    prov.getAllDomains(visitor, new String[]{ZAttrProvisioning.A_zimbraId});
    ZimbraLog.misc.debug("CallToHome: Counting accounts done!");

    return accountCount.get();
  }
}

