package com.zimbra.cs.service.mail;

import com.google.common.collect.Maps;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.Element;
import com.zimbra.common.soap.MailConstants;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.Server;
import com.zimbra.cs.mailbox.MailboxTestUtil;
import com.zimbra.soap.JaxbUtil;
import com.zimbra.soap.mail.message.GetPreviewRequest;
import java.util.Map;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class GetPreviewTest {

  private static Server localServer = null;

  @BeforeClass
  public static void init() throws Exception {
    MailboxTestUtil.initServer();
    Provisioning prov = Provisioning.getInstance();

    Map<String, Object> attrs = Maps.newHashMap();
    prov.createAccount("test@zimbra.com", "secret", attrs);

    localServer = prov.getLocalServer();
  }

  @Before
  public void setUp() throws Exception {
    MailboxTestUtil.clearData();
  }

  @Test
  public void shouldGetServerBaseUrlWithoutPort() throws ServiceException {
    String baseUrl = "http://localhost";
    Assert.assertEquals(baseUrl, GetPreview.getServerBaseUrl(localServer, false));
  }

  @Test
  public void shouldGetServerBaseUrlWithPort() throws ServiceException {
    String baseUrl = "http://localhost:0";
    Assert.assertEquals(baseUrl, GetPreview.getServerBaseUrl(localServer, true));
  }

  @Test
  public void shouldReturnPdfParamsAsQueryStringWhenAskedForPreviewTypeThumbnail()
      throws ServiceException {
    GetPreviewRequest requestElement = new GetPreviewRequest();
    Element element = JaxbUtil.jaxbToElement(requestElement);
    Element pdfEle = element.addUniqueElement(MailConstants.E_P_PDF);
    pdfEle.addAttribute(MailConstants.A_P_PREVIEW_TYPE, "thumbnail");
    pdfEle.addAttribute(MailConstants.A_P_AREA, "100x200");
    pdfEle.addAttribute(MailConstants.A_P_OUTPUT_FORMAT, "jpeg");
    pdfEle.addAttribute(MailConstants.A_P_QUALITY, "high");
    String expectedString = "/100x200/thumbnail/?quality=high&output_format=jpeg";
    Assert.assertEquals(expectedString, GetPreview.getPdfParamsAsQueryString(element));
  }

  @Test
  public void shouldReturnPdfParamsAsQueryStringWhenAskedForPreviewTypeFull()
      throws ServiceException {
    GetPreviewRequest requestElement = new GetPreviewRequest();
    Element element = JaxbUtil.jaxbToElement(requestElement);
    Element pdfEle = element.addUniqueElement(MailConstants.E_P_PDF);
    pdfEle.addAttribute(MailConstants.A_P_PREVIEW_TYPE, "full");
    pdfEle.addAttribute(MailConstants.A_P_FIRST_PAGE, "0");
    pdfEle.addAttribute(MailConstants.A_P_LAST_PAGE, "1");
    String expectedString = "?first_page=0&last_page=1";
    Assert.assertEquals(expectedString, GetPreview.getPdfParamsAsQueryString(element));
  }

  @Test
  public void shouldReturnImageParamsAsQueryStringWhenAskedForPreviewTypeThumbnail()
      throws ServiceException {
    GetPreviewRequest requestElement = new GetPreviewRequest();
    Element element = JaxbUtil.jaxbToElement(requestElement);
    Element imgEle = element.addUniqueElement(MailConstants.E_P_IMAGE);
    imgEle.addAttribute(MailConstants.A_P_PREVIEW_TYPE, "thumbnail");
    imgEle.addAttribute(MailConstants.A_P_AREA, "500x300");
    imgEle.addAttribute(MailConstants.A_P_CROP, "false");
    imgEle.addAttribute(MailConstants.A_P_OUTPUT_FORMAT, "jpeg");
    imgEle.addAttribute(MailConstants.A_P_QUALITY, "high");
    String expectedString = "/500x300/thumbnail/?quality=high&output_format=jpeg";
    Assert.assertEquals(expectedString, GetPreview.getImageParamsAsQueryString(element));
  }

  @Test
  public void shouldReturnImageParamsAsQueryStringWhenAskedForPreviewTypeFull()
      throws ServiceException {
    GetPreviewRequest requestElement = new GetPreviewRequest();
    Element element = JaxbUtil.jaxbToElement(requestElement);
    Element imgEle = element.addUniqueElement(MailConstants.E_P_IMAGE);
    imgEle.addAttribute(MailConstants.A_P_PREVIEW_TYPE, "full");
    imgEle.addAttribute(MailConstants.A_P_AREA, "500x300");
    imgEle.addAttribute(MailConstants.A_P_CROP, "false");
    imgEle.addAttribute(MailConstants.A_P_OUTPUT_FORMAT, "jpeg");
    imgEle.addAttribute(MailConstants.A_P_QUALITY, "high");
    String expectedString = "/500x300/?crop=false&quality=high&output_format=jpeg";
    Assert.assertEquals(expectedString, GetPreview.getImageParamsAsQueryString(element));
  }
}
