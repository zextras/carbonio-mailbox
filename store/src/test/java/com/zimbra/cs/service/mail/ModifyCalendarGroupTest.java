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
  void addOneCalendar() throws Exception {
    var res = addGroupFor(account, "Group Calendar", List.of("101", "420"));
    var request = new ModifyCalendarGroupRequest();
    var id = res.getGroup().getId();
    request.setId(id);
    var modifiedCalendarList = List.of("101", "420", "421");
    request.setCalendarIds(modifiedCalendarList);

    final var soapResponse = getSoapClient().executeSoap(account, request);
    assertEquals(HttpStatus.SC_OK, soapResponse.getStatusLine().getStatusCode());
    var response = parseSoapResponse(soapResponse, ModifyCalendarGroupResponse.class);
    var group = response.getGroup();
    assertEquals(modifiedCalendarList, group.getCalendarIds());
  }

  @Test
  void renameGroup() throws Exception {
    var calendarIds = List.of("101", "420");
    var res = addGroupFor(account, "Group Calendar", calendarIds);
    var request = new ModifyCalendarGroupRequest();
    var id = res.getGroup().getId();
    request.setId(id);
    String groupNameModified = "Modified - Group Calendar";
    request.setName(groupNameModified);

    final var soapResponse = getSoapClient().executeSoap(account, request);
    assertEquals(HttpStatus.SC_OK, soapResponse.getStatusLine().getStatusCode());
    var response = parseSoapResponse(soapResponse, ModifyCalendarGroupResponse.class);
    var group = response.getGroup();
    assertEquals(groupNameModified, group.getName());
    assertEquals(calendarIds, group.getCalendarIds());
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
