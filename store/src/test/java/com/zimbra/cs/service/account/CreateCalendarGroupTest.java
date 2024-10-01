package com.zimbra.cs.service.account;

import com.zextras.mailbox.soap.SoapTestSuite;
import com.zextras.mailbox.util.MailboxTestUtil;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.util.StringUtil;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.soap.JaxbUtil;
import com.zimbra.soap.mail.message.CreateCalendarGroupRequest;
import com.zimbra.soap.mail.message.CreateCalendarGroupResponse;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.util.EntityUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static com.zimbra.common.soap.Element.parseXML;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

@Tag("api")
class CreateCalendarGroupTest extends SoapTestSuite {

  private static MailboxTestUtil.AccountCreator.Factory accountCreatorFactory;
  private static Provisioning provisioning;

  private Account account;

  @BeforeAll
  static void init() {
    provisioning = Provisioning.getInstance();
    accountCreatorFactory = new MailboxTestUtil.AccountCreator.Factory(provisioning);
  }

  @BeforeEach
  void setUp() throws Exception {
    account = accountCreatorFactory.get().create();
  }

  @Test
  void createGroup() throws Exception {
    final var request = new CreateCalendarGroupRequest();
    request.setName("Test Group");
    request.setCalendarIds(List.of("10", "420", "421"));

    final var soapResponse = getSoapClient().executeSoap(account, request);

    assertEquals(HttpStatus.SC_OK, soapResponse.getStatusLine().getStatusCode());
    final var response = parseSoapResponse(soapResponse);
    var group = response.getGroup();
    assertFalse(StringUtil.isNullOrEmpty(group.getId()));
    assertEquals("Test Group", group.getName());
    assertEquals(List.of("10", "420", "421"), group.getCalendarIds());
  }

  @Test
  void cannotCreateExistingGroup() throws Exception {
    var sameGroupName = "Test Group";
    createGroupFor(account, sameGroupName, List.of("10", "420", "421"));

    final var request = new CreateCalendarGroupRequest();
    request.setName(sameGroupName);
    request.setCalendarIds(List.of("10", "420", "421"));

    final var soapResponse = getSoapClient().executeSoap(account, request);

    assertEquals(HttpStatus.SC_UNPROCESSABLE_ENTITY, soapResponse.getStatusLine().getStatusCode());
  }

  private void createGroupFor(Account acc, String groupName, List<String> calendarIds) throws Exception {
    final var request = new CreateCalendarGroupRequest();
    request.setName(groupName);
    request.setCalendarIds(calendarIds);

    getSoapClient().executeSoap(acc, request);
  }

  private static CreateCalendarGroupResponse parseSoapResponse(HttpResponse httpResponse)
      throws IOException, ServiceException {
    final var responseBody = EntityUtils.toString(httpResponse.getEntity());
    final var rootElement =
        parseXML(responseBody)
            .getElement("Body")
            .getElement(CreateCalendarGroupResponse.class.getSimpleName());
    return JaxbUtil.elementToJaxb(rootElement, CreateCalendarGroupResponse.class);
  }
}
