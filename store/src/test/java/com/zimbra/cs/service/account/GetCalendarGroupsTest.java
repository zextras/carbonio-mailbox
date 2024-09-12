package com.zimbra.cs.service.account;

import static com.zimbra.common.soap.Element.parseXML;
import static org.junit.jupiter.api.Assertions.*;

import com.zextras.mailbox.soap.SoapTestSuite;
import com.zextras.mailbox.util.MailboxTestUtil;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.Element;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.soap.JaxbUtil;
import com.zimbra.soap.mail.message.GetCalendarGroupsRequest;
import com.zimbra.soap.mail.message.GetCalendarGroupsResponse;
import java.io.IOException;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.util.EntityUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("api")
class GetCalendarGroupsTest extends SoapTestSuite {

  private static MailboxTestUtil.AccountCreator.Factory accountCreatorFactory;
  private static Provisioning provisioning;

  private Account account;

  @BeforeAll
  static void init() throws Exception {
    provisioning = Provisioning.getInstance();
    accountCreatorFactory = new MailboxTestUtil.AccountCreator.Factory(provisioning);
  }

  @BeforeEach
  void setUp() throws Exception {
    account = accountCreatorFactory.get().create();
  }

  /*
   Tests:
   - add a calendar, assert that it is in the "All Calendars" group
   - assert that an account cannot see another account's calendar groups
  */

  @Test
  void alwaysReturnsAllCalendarsDefaultGroup() throws Exception {
    final var request = new GetCalendarGroupsRequest();

    final var soapResponse = getSoapClient().executeSoap(account, request);

    assertEquals(HttpStatus.SC_OK, soapResponse.getStatusLine().getStatusCode());
    final var response = parseSoapResponse(soapResponse);
    assertEquals(1, response.getGroups().size());
    assertEquals("All Calendars", response.getGroups().get(0).getName());
    assertEquals(1, response.getGroups().get(0).getCalendarIds().size());
  }

  private static GetCalendarGroupsResponse parseSoapResponse(HttpResponse httpResponse)
      throws IOException, ServiceException {
    final String responseBody = EntityUtils.toString(httpResponse.getEntity());
    final Element rootElement =
        parseXML(responseBody).getElement("Body").getElement("GetCalendarGroupsResponse");
    return JaxbUtil.elementToJaxb(rootElement, GetCalendarGroupsResponse.class);
  }
}
