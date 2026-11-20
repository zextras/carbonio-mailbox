package com.zimbra.cs.service.account;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.zextras.mailbox.soap.SoapTestSuite;
import com.zimbra.soap.account.message.DiscoverRightsRequest;
import com.zimbra.soap.account.message.DistributionListActionRequest;
import com.zimbra.soap.account.type.DistributionListAction;
import com.zimbra.soap.account.type.DistributionListGranteeSelector;
import com.zimbra.soap.account.type.DistributionListRightSpec;
import com.zimbra.soap.type.DistributionListBy;
import com.zimbra.soap.type.DistributionListGranteeBy;
import com.zimbra.soap.type.DistributionListSelector;
import com.zimbra.soap.type.GranteeType;
import java.util.HashMap;
import java.util.List;
import org.apache.hc.core5.http.HttpStatus;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("api")
class DiscoverRightsTest extends SoapTestSuite {

  @Test
  void returnsDistributionListTheAccountCanSendAs() throws Exception {
    final var admin = createAccount().asGlobalAdmin().create();
    final var grantee = createAccount().create();
    final var dl =
        getProvisioning().createDistributionList("discover-dl@test.com", new HashMap<>());
    final var rightSpec = new DistributionListRightSpec("sendAsDistList");
    rightSpec.addGrantee(
        new DistributionListGranteeSelector(
            GranteeType.email, DistributionListGranteeBy.name, grantee.getName()));
    final var action = new DistributionListAction(DistributionListAction.Operation.grantRights);
    action.setRights(List.of(rightSpec));
    getSoapClient()
        .executeSoap(
            admin,
            new DistributionListActionRequest(
                new DistributionListSelector(DistributionListBy.id, dl.getId()), action));

    final var response =
        getSoapClient().executeSoap(grantee, new DiscoverRightsRequest(List.of("sendAsDistList")));

    assertEquals(HttpStatus.SC_OK, response.statusCode());
    assertTrue(
        response
            .body()
            .contains(
                """
                <targets right="sendAsDistList"><target type="dl"><email addr="discover-dl@test.com"/></target></targets>"""));
  }
}
