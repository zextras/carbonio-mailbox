// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package qa.unittest;

import com.zimbra.common.httpclient.HttpClientUtil;
import junit.framework.Assert;
import junit.framework.TestCase;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.util.EntityUtils;
import org.junit.Test;

public class TestSoapFault extends TestCase {
  @Test
  public void testSoapFaultTraceIpReveal() throws Exception {
    String batchRequestUrl = TestUtil.getSoapUrl() + "BatchRequest";
    String createAppointmentRequestUrl = TestUtil.getSoapUrl() + "createAppointmentRequest";
    String modifyContactRequestUrl = TestUtil.getSoapUrl() + "ModifyContactRequest";
    String noOpRequestUrl = TestUtil.getSoapUrl() + "NoOpRequest";
    String getMiniCalRequestUrl = TestUtil.getSoapUrl() + "GetMiniCalRequest";

    HttpPost batchRequestMethod = new HttpPost(batchRequestUrl);
    HttpResponse httpResponse = HttpClientUtil.executeMethod(batchRequestMethod);
    String response = EntityUtils.toString(httpResponse.getEntity());
    Assert.assertFalse("Trace contains ip address.", response.contains(batchRequestUrl));

    HttpPost createAppointmentRequestMethod = new HttpPost(createAppointmentRequestUrl);
    httpResponse = HttpClientUtil.executeMethod(createAppointmentRequestMethod);
    response = EntityUtils.toString(httpResponse.getEntity());
    Assert.assertFalse(
        "Trace contains ip address.", response.contains(createAppointmentRequestUrl));

    HttpPost modifyContactRequestMethod = new HttpPost(modifyContactRequestUrl);
    httpResponse = HttpClientUtil.executeMethod(modifyContactRequestMethod);
    response = EntityUtils.toString(httpResponse.getEntity());
    Assert.assertFalse("Trace contains ip address.", response.contains(modifyContactRequestUrl));

    HttpPost noOpRequestMethod = new HttpPost(noOpRequestUrl);
    httpResponse = HttpClientUtil.executeMethod(noOpRequestMethod);
    response = EntityUtils.toString(httpResponse.getEntity());
    Assert.assertFalse("Trace contains ip address.", response.contains(noOpRequestUrl));

    HttpPost getMiniCalRequestMethod = new HttpPost(getMiniCalRequestUrl);
    httpResponse = HttpClientUtil.executeMethod(getMiniCalRequestMethod);
    response = EntityUtils.toString(httpResponse.getEntity());
    Assert.assertFalse("Trace contains ip address.", response.contains(getMiniCalRequestUrl));
  }
}
