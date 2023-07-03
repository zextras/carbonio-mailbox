// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: AGPL-3.0-only

package com.zimbra.cs.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.zimbra.common.localconfig.LC;
import com.zimbra.cs.account.MockProvisioning;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.mailbox.MailboxTestUtil;
import org.apache.commons.io.IOUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import java.io.IOException;

public class CallToHomeTest {

  private static final String UPDATE_URL = "https://updates.zextras.com/openchat";

  @BeforeAll
  public static void init() throws Exception {
    MailboxTestUtil.initServer();
    LC.zimbra_attrs_directory.setDefault(MailboxTestUtil.getZimbraServerDir("") + "conf/attrs");
    MockProvisioning prov = new MockProvisioning();
    prov.getLocalServer().setSmtpPort(25);
    Provisioning.setInstance(prov);
  }

  private JSONObject fakeData() throws JSONException {
    JSONObject json = new JSONObject();
    json.put("zimbraVersion", "8.8.15_GA_1001");
    json.put("isNetwork", false);
    json.put("serverName", "test_server_name");
    json.put("numAccounts", 0);
    return json;
  }

 @Test
 void whenGetServerName() throws Exception {

  Provisioning provisioning = Provisioning.getInstance();
  assertEquals(
    "localhost", provisioning.getLocalServer().getName(), "localserver name is ");
 }

 @Test
 void whenGetData() throws JSONException {
  JSONObject parsed = new JSONObject(fakeData().toString());
  assertEquals(fakeData().toString(), parsed.toString(), "parsed json is valid");
 }

 @Test
 void whenPostDataToEndpoint() throws IOException, JSONException {
  JSONObject data = fakeData();
  HttpPost post = new HttpPost(UPDATE_URL);
  post.setHeader("Content-Type", "application/json; charset=UTF-8");
  StringEntity stringEntity = new StringEntity(data.toString());
  post.setEntity(stringEntity);
  CloseableHttpClient client = HttpClients.createDefault();
  try {
   CloseableHttpResponse httpResp = client.execute(post);
   int statusCode = httpResp.getStatusLine().getStatusCode();
   assertEquals(200, statusCode, "status code is not 200");
  } catch (Exception e) {
   throw new IOException("unexpected error during post operation: " + e.getMessage());
  } finally {
   post.releaseConnection();
  }
 }
}
