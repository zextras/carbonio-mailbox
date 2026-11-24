package com.zimbra.cs.service.admin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.zextras.mailbox.soap.SoapTestSuite;
import com.zimbra.soap.admin.message.GetAllEffectiveRightsRequest;
import com.zimbra.soap.admin.type.GranteeSelector;
import com.zimbra.soap.admin.type.GranteeSelector.GranteeBy;
import com.zimbra.soap.type.GranteeType;
import org.apache.hc.core5.http.HttpStatus;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("api")
class GetAllEffectiveRightsTest extends SoapTestSuite {

  @Test
  void returnsTheRightsOfAGlobalAdmin() throws Exception {
    final var admin = createAccount().asGlobalAdmin().create();
    final var request =
        new GetAllEffectiveRightsRequest(
            new GranteeSelector(GranteeType.usr, GranteeBy.name, admin.getName()), false, false);

    final var response = getSoapClient().executeSoap(admin, request);

    assertEquals(HttpStatus.SC_OK, response.statusCode(), response.body());
    assertTrue(response.body().contains("<target type=\"domain\""), response.body());
  }
}
