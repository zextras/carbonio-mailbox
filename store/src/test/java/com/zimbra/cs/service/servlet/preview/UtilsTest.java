package com.zimbra.cs.service.servlet.preview;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class UtilsTest {

  @Test
  void should_return_full_url_from_request_when_query_string_is_present() {

    var req = mock(HttpServletRequest.class);
    when(req.getRequestURL()).thenReturn(new StringBuffer("https://test.mail.com/resource"));
    when(req.getQueryString()).thenReturn("param1=value1&param2=value2");

    var fullURL = Utils.getFullURLFromRequest(req);

    assertEquals("https://test.mail.com/resource?param1=value1&param2=value2", fullURL);
  }

  @Test
  void should_return_full_url_from_request_when_no_query_string_is_present() {

    var req = mock(HttpServletRequest.class);
    when(req.getRequestURL()).thenReturn(new StringBuffer("https://test.mail.com/resource"));
    when(req.getQueryString()).thenReturn(null);

    var fullURL = Utils.getFullURLFromRequest(req);

    assertEquals("https://test.mail.com/resource", fullURL);
  }

  @Test
  void should_throw_illegal_argument_exception_when_request_is_null() {

    assertThrows(IllegalArgumentException.class, () -> Utils.getFullURLFromRequest(null));
  }

  @Test
  void should_return_request_id_from_request_when_attribute_is_set() {
    var request = mock(HttpServletRequest.class);
    var expectedRequestId = "12345";

    when(request.getAttribute(Utils.REQUEST_ID_KEY)).thenReturn(expectedRequestId);

    var requestId = Utils.getRequestIdFromRequest(request);

    assertEquals(expectedRequestId, requestId);
  }

  @Test
  void should_return_request_id_from_request_when_attribute_is_not_set_but_query_param_is_set() {
    var request = mock(HttpServletRequest.class);
    var expectedRequestId = "12345";

    when(request.getParameter(Utils.REQUEST_ID_KEY)).thenReturn(expectedRequestId);

    var requestId = Utils.getRequestIdFromRequest(request);

    assertEquals(expectedRequestId, requestId);
  }

  @Test
  void should_throw_exception_when_passed_request_is_null() {
    var exception = assertThrows(IllegalArgumentException.class, () -> Utils.getRequestIdFromRequest(null));
    assertEquals("HttpServletRequest cannot be null", exception.getMessage());
  }

  @Test
  void should_return_null_if_attribute_and_query_param_missing() {
    var request = mock(HttpServletRequest.class);

    when(request.getAttribute(Utils.REQUEST_ID_KEY)).thenReturn(null);
    when(request.getParameter(Utils.REQUEST_ID_KEY)).thenReturn(null);

    assertNull(Utils.getRequestIdFromRequest(request),
        "should return null if request do not contain attribute or query param with request id key");
  }

  @ParameterizedTest
  @CsvSource({
      "https://test.mail.com/resource?disp=attachment&param=value, https://test.mail.com/resource?param=value",
      "https://test.mail.com/resource?param=value&disp=a&other=info, https://test.mail.com/resource?param=value&other=info",
      "https://test.mail.com/resource?param=value&other=info&disp=inline, https://test.mail.com/resource?param=value&other=info",
      "https://test.mail.com/resource?param=value, https://test.mail.com/resource?param=value",
      "https://test.mail.com/resource?disp=i&param1=value1&param2=value2, https://test.mail.com/resource?param1=value1&param2=value2",
      "https://test.mail.com/resource?disp=preview, https://test.mail.com/resource",
      "https://test.mail.com/resource?disp=, https://test.mail.com/resource",
      "https://test.mail.com/resource?disp, https://test.mail.com/resource"})
  void testGetRequestUrlForPreview(String requestUrl, String expectedUrl) {
    var result = Utils.removeQueryParams(requestUrl, List.of("disp"));

    assertEquals(expectedUrl, result, "should remove requested query parameters from the URL string");
  }

  @ParameterizedTest
  @CsvSource({
      "https://test.mail.com/resource?disp=attachment, attachment",
      "https://test.mail.com/resource?disp=inline, inline",
      "https://test.mail.com/resource?disp=, i",
      "https://test.mail.com/resource?param=value&disp=attachment, attachment",
      "https://test.mail.com/resource?param=value&disp=inline&other=value, inline",
      "https://test.mail.com/resource?param=value, i",
      "https://test.mail.com/resource, i",
      ", i"
  })
  void testGetDispositionTypeFromQueryParams(String requestUrl, String expectedDisposition) {
    var result = Utils.getDispositionTypeFromQueryParams(requestUrl);

    assertEquals(expectedDisposition, result, "should return disposition type from query params with name 'disp', "
        + "falling back to 'inline' or 'i'");
  }

}