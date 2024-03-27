package com.zimbra.soap.mail.message;

import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.Element.JSONElement;
import com.zimbra.common.soap.Element.XMLElement;
import com.zimbra.soap.JaxbUtil;
import com.zimbra.soap.mail.type.SmartLink;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CreateSmartLinksResponseTest {
  @Test
  void convertToJson() throws ServiceException {
    var response = new CreateSmartLinksResponse(Arrays.asList(
        new SmartLink("one"),
        new SmartLink("two")
    ));

    assertEquals(
        "{\"smartLinks\":[{\"publicUrl\":\"one\"},{\"publicUrl\":\"two\"}],\"_jsns\":\"urn:zimbraMail\"}",
        toJson(response)
    );
  }

  @Test
  void convertToXml() throws ServiceException {
    var response = new CreateSmartLinksResponse(Arrays.asList(
        new SmartLink("one"),
        new SmartLink("two")
    ));

    assertEquals(
        "<CreateSmartLinksResponse xmlns=\"urn:zimbraMail\">" +
            "<smartLinks publicUrl=\"one\"/>" +
            "<smartLinks publicUrl=\"two\"/>" +
            "</CreateSmartLinksResponse>",
        toXml(response)
    );
  }

  private static String toJson(CreateSmartLinksResponse response) throws ServiceException {
    JSONElement element = (JSONElement) JaxbUtil.jaxbToElement(response, JSONElement.mFactory);
    return element.toString();
  }

  private static String toXml(CreateSmartLinksResponse response) throws ServiceException {
    XMLElement element = (XMLElement) JaxbUtil.jaxbToElement(response, XMLElement.mFactory);
    return element.toString();
  }
}