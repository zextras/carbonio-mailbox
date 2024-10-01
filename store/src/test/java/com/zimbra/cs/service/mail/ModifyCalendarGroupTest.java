package com.zimbra.cs.service.mail;

import com.zextras.mailbox.soap.SoapTestSuite;
import com.zextras.mailbox.util.MailboxTestUtil;
import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.soap.JaxbUtil;
import com.zimbra.soap.mail.message.CreateCalendarGroupRequest;
import com.zimbra.soap.mail.message.CreateCalendarGroupResponse;
import com.zimbra.soap.mail.message.ModifyCalendarGroupRequest;
import com.zimbra.soap.mail.message.ModifyCalendarGroupResponse;
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

@Tag("api")
class ModifyCalendarGroupTest extends SoapTestSuite {

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
  void addOneCalendarToExistingGroup() throws Exception {
    var res = addGroupFor(account, "Ranocchia Group Calendar", List.of("101", "420"));
    var request = new ModifyCalendarGroupRequest();
    String id = res.getGroup().getId();
    request.setId(id);
    request.setName("Modified Group Calendar");
    request.setCalendarIds(List.of("101", "420", "421"));

    final var soapResponse = getSoapClient().executeSoap(account, request);
    assertEquals(HttpStatus.SC_OK, soapResponse.getStatusLine().getStatusCode());
    var response = parseSoapResponse(soapResponse, ModifyCalendarGroupResponse.class);
    var group = response.getGroup();
    assertEquals("Modified Group Calendar", group.getName());
    assertEquals(List.of("101", "420", "421"), group.getCalendarIds());
  }

  private CreateCalendarGroupResponse addGroupFor(Account acc, String groupName, List<String> calendarIds) throws Exception {
    final var request = new CreateCalendarGroupRequest();
    request.setName(groupName);
    request.setCalendarIds(calendarIds);

    var soapResponse = getSoapClient().executeSoap(acc, request);
    assertEquals(HttpStatus.SC_OK, soapResponse.getStatusLine().getStatusCode());
    return parseSoapResponse(soapResponse, CreateCalendarGroupResponse.class);
  }

  private static <T> T parseSoapResponse(HttpResponse httpResponse, Class<T> clazz)
          throws IOException, ServiceException {
    final var responseBody = EntityUtils.toString(httpResponse.getEntity());
    final var rootElement =
            parseXML(responseBody)
                    .getElement("Body")
                    .getElement(clazz.getSimpleName());
    return JaxbUtil.elementToJaxb(rootElement, clazz);
  }
}
