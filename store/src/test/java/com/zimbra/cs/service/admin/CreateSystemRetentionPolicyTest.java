package com.zimbra.cs.service.admin;

import com.zextras.mailbox.soap.SoapTestSuite;
import com.zextras.mailbox.util.MailboxTestUtil.AccountCreator;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.Cos;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.soap.admin.message.CreateSystemRetentionPolicyRequest;
import com.zimbra.soap.admin.type.CosSelector;
import com.zimbra.soap.admin.type.CosSelector.CosBy;
import com.zimbra.soap.mail.type.Policy;
import java.util.HashMap;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.util.EntityUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("api")
class CreateSystemRetentionPolicyTest extends SoapTestSuite {
  private static AccountCreator.Factory accountCreatorFactory;

  @BeforeAll
  static void setUp() throws Exception {
    accountCreatorFactory = new AccountCreator.Factory(Provisioning.getInstance());
  }

  @Test
  void shouldSuccessfullyCreateSystemRetentionPolicy() throws Exception {
    final Account adminAccount = accountCreatorFactory.get().asGlobalAdmin().create();
    final CreateSystemRetentionPolicyRequest request = CreateSystemRetentionPolicyRequest.newPurgeRequest(
        Policy.newSystemPolicy("PurgePolicy", "10d"));

    final HttpResponse response =
        getSoapClient().newRequest().setCaller(adminAccount).setSoapBody(request).execute();

    Assertions.assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());
    final String responseBody = EntityUtils.toString(response.getEntity());
    Assertions.assertTrue(responseBody.contains("policy name=\"PurgePolicy\" lifetime=\"10d\""));
  }

  @Test
  void shouldSuccessfullyCreateSystemRetentionPolicyForCOS() throws Exception {
    final Account adminAccount = accountCreatorFactory.get().asGlobalAdmin().create();
    final Cos cos = Provisioning.getInstance().createCos("testCOS", new HashMap<>());

    final CreateSystemRetentionPolicyRequest request = CreateSystemRetentionPolicyRequest.newPurgeRequest(
        Policy.newSystemPolicy("PurgePolicy", "10d"));
    request.setCos(new CosSelector(CosBy.id, cos.getId()));

    final HttpResponse response =
        getSoapClient().newRequest().setCaller(adminAccount).setSoapBody(request).execute();

    Assertions.assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());
    final String responseBody = EntityUtils.toString(response.getEntity());
    Assertions.assertTrue(responseBody.contains("policy name=\"PurgePolicy\" lifetime=\"10d\""));
  }

  @Test
  void shouldDenyWhenNoSuchCOS() throws Exception {
    final Account adminAccount = accountCreatorFactory.get().asGlobalAdmin().create();

    final CreateSystemRetentionPolicyRequest request = CreateSystemRetentionPolicyRequest.newPurgeRequest(
        Policy.newSystemPolicy("PurgePolicy", "10d"));
    request.setCos(new CosSelector(CosBy.id, "No such COS"));

    final HttpResponse response =
        getSoapClient().newRequest().setCaller(adminAccount).setSoapBody(request).execute();

    Assertions.assertEquals(HttpStatus.SC_INTERNAL_SERVER_ERROR, response.getStatusLine().getStatusCode());
    final String responseBody = EntityUtils.toString(response.getEntity());
    Assertions.assertTrue(responseBody.contains("no such cos: No such COS"));
  }

  @Test
  void shouldDenyWhenPurgeSystemRetentionPolicyNotSpecified() throws Exception {
    final Account adminAccount = accountCreatorFactory.get().asGlobalAdmin().create();
    final CreateSystemRetentionPolicyRequest request = new CreateSystemRetentionPolicyRequest();

    final HttpResponse response =
        getSoapClient().newRequest().setCaller(adminAccount).setSoapBody(request).execute();

    Assertions.assertEquals(HttpStatus.SC_INTERNAL_SERVER_ERROR, response.getStatusLine().getStatusCode());
    final String responseBody = EntityUtils.toString(response.getEntity());
    Assertions.assertTrue(responseBody.contains("No purge policy specified."));
  }

  @Test
  void shouldDenyWhenNotAdminAccount() throws Exception {
    final Account userAccount = accountCreatorFactory.get().create();
    final CreateSystemRetentionPolicyRequest request = new CreateSystemRetentionPolicyRequest();

    final HttpResponse response =
        getSoapClient().newRequest().setCaller(userAccount).setSoapBody(request).execute();

    Assertions.assertEquals(HttpStatus.SC_INTERNAL_SERVER_ERROR, response.getStatusLine().getStatusCode());
    final String responseBody = EntityUtils.toString(response.getEntity());
    Assertions.assertTrue(responseBody.contains("permission denied: need adequate admin token"));
  }
}