// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.html.owasp;

import static org.junit.jupiter.api.Assertions.*;

import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;
import com.zimbra.common.localconfig.LC;
import com.zimbra.common.util.ByteUtil;
import com.zimbra.cs.mailbox.MailboxTestUtil;
import com.zimbra.cs.mime.MPartInfo;
import com.zimbra.cs.mime.Mime;
import com.zimbra.cs.mime.ParsedMessage;
import com.zimbra.cs.servlet.ZThreadLocal;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;


public class OwaspHtmlSanitizerTest {

  /*
   * Cut down version of original Bug 101227 test data which was significantly
   * larger
   */
  private static final String urlWithInlinePNG =
      "background-image:\n  url('data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAA+/AA\n"
          + "WAP457/97wejL8Ovcj5LuH/FEVEf7d+/etuVtN/OIknPPDGFlCoJlWx/lkf/78/mGP2zUhTzC0AAAAASUVORK5CYII=');\n"
          + "    background-repeat: no-repeat; background-position: center;";
  private static final String defangedUrlWithInlinePNG = "background-repeat:no-repeat;background-position:center";
  private static String EMAIL_BASE_DIR = "data/unittest/email/";
  private static String htmlTemplateForUrlWithInlinePNG = "<table><tbody><tr><td style=\"%s\"></td></tr></tbody></table>";
  /*
   * Verified in Firefox that multi-line url with this html displays the
   * background image (give a valid paper.gif file from
   * http://www.w3schools.com/cssref/paper.gif) So we definitely should be
   * stripping out multi-line functions Owasp policy doesn't allow text inside
   * style by default
   */
  private static String templateHtmlWithNonInlinedBackgroundImageURL = "<html><head><style>"
      + "</style></head><body><h1>Hello World!</h1></body></html>";
  private static String nonInlinedBackgroundImageURL = "url(\n" + "            \"paper.gif\"\n"
      + "            )";
  
  public String testName;

  @BeforeAll
  public static void init() throws Exception {
    MailboxTestUtil.initServer();
    LC.zimbra_use_owasp_html_sanitizer.value();
    EMAIL_BASE_DIR = MailboxTestUtil.getZimbraServerDir("") + EMAIL_BASE_DIR;
  }

 @BeforeEach
 public void setUp(TestInfo testInfo) throws Exception {
  Optional<Method> testMethod = testInfo.getTestMethod();
  if (testMethod.isPresent()) {
   this.testName = testMethod.get().getName();
  }
  MailboxTestUtil.clearData();
  LC.zimbra_use_owasp_html_sanitizer.setDefault(true);
 }

  @AfterEach
  public void tearDown() throws Exception {
    MailboxTestUtil.clearData();
  }

  private void defangHtmlString(String html, String expected) throws IOException {
    String result = new OwaspHtmlSanitizer(html, true, null).sanitize();
    assertEquals(expected, result, "Defanged HTML result");
  }

  /**
   * Utility method that gets the html body part from a mime message and returns its input stream
   *
   * @param fileName The name of the email file to load from the unit test data dir
   * @return The input stream for the html body if successful
   * @throws Exception
   */
  private InputStream getHtmlBody(String fileName) throws Exception {
    // Get an input stream of a test pdf to test with
    InputStream inputStream = new FileInputStream(EMAIL_BASE_DIR + fileName);
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    ByteUtil.copy(inputStream, true, baos, true);

    ParsedMessage msg = new ParsedMessage(baos.toByteArray(), false);
    Set<MPartInfo> bodyparts = Mime.getBody(msg.getMessageParts(), true);

    InputStream htmlStream = null;
    for (MPartInfo body : bodyparts) {
      if (body.getContentType().contains("html")) {
        htmlStream = body.getMimePart().getInputStream();
      }
    }
    return htmlStream;
  }

 @Test
 void testHtmlWithStyleValueContainingMultiLineUrl() throws Exception {
  defangHtmlString(String.format(htmlTemplateForUrlWithInlinePNG, urlWithInlinePNG),
    String.format(htmlTemplateForUrlWithInlinePNG, defangedUrlWithInlinePNG));
  defangHtmlString(
    String.format(templateHtmlWithNonInlinedBackgroundImageURL,
      nonInlinedBackgroundImageURL),
    String.format(templateHtmlWithNonInlinedBackgroundImageURL, ""));
 }


 @Test
 void testBug98215() throws Exception {
  String html = "<a href=\"vbscript:alert(parent.csrfToken)\">CLICK</a>";
  String result = new OwaspHtmlSanitizer(html, true, null).sanitize();
  assertEquals(result, "CLICK"); // a tag removed

  html = "<a href=\"Vbscr&amp;#0009;ip&#009;t:alert(parent.csrfToken)\">CLICK</a>";
  result = new OwaspHtmlSanitizer(html, true, null).sanitize();
  assertFalse(result.contains("Vbscript:alert(parent.csrfToken)"));

  html = "<a href=\"java&amp;Tab;script:alert(parent.csrfToken)\">CLICK</a>";
  result = new OwaspHtmlSanitizer(html, true, null).sanitize();
  assertEquals(result, "CLICK"); // a tag removed

  html = "<a href=\"&amp;Tab;javascript:alert(parent.csrfToken)\">CLICK</a>";
  result = new OwaspHtmlSanitizer(html, true, null).sanitize();
  assertEquals(result, "CLICK"); // a tag removed

  html = "<a href=\"javascr&amp;#09;ipt:alert(parent.csrfToken)\">CLICK</a>";
  result = new OwaspHtmlSanitizer(html, true, null).sanitize();
  assertFalse(result.contains("javascript:alert(parent.csrfToken)"));

  html = "<form id=\"test\" action=\"javascript:alert(1)\"><p>test</p>"
    + "<button form=\"test\">Test</button></form>";
  result = new OwaspHtmlSanitizer(html, true, null).sanitize();
  // action attribute removed
  assertEquals(result, "<form id=\"test\"><p>test</p><button>Test</button></form>");

  html = "<form id=\"test\" action=\"ja&amp;Tab;vascript:alert(1)\"><p>test</p>"
    + "<button form=\"test\">Test</button></form>";
  result = new OwaspHtmlSanitizer(html, true, null).sanitize();
  // action attribute removed
  assertEquals(result, "<form id=\"test\"><p>test</p><button>Test</button></form>");

  html = "<a href=\"&amp;#009;java&#00009;scr&amp;#09;i\t\tpt:alert(parent.csrfToken)\">CLICK</a>";
  result = new OwaspHtmlSanitizer(html, true, null).sanitize();
  assertFalse(result.contains("javascript:alert(parent.csrfToken)"));
 }

 /*
  * Verify that a new line in a html based signature is maintained after
  * passing through the defanger.
  *
  * @throws Exception
  */
 @Test
 void testBug105001() throws Exception {
  String html = "<html><body><div> <a href=\"javascript\n: alert('XSS')\">XSS LINK</a>"
    + "<br data-mce-bogus=\"1\"></div></div></body></html>";
  String result = new OwaspHtmlSanitizer(html, true, null).sanitize();
  assertEquals(result, "<html><body><div> XSS LINK<br /></div></body></html>");

  html = "<a href=\"vbscript\n\n:alert(parent.csrfToken)\">CLICK</a>";
  result = new OwaspHtmlSanitizer(html, true, null).sanitize();
  // a tag removed
  assertEquals(result, "CLICK");

  html = "<a href=\"Vbscr&amp;#0009;ip&#009;t\n\n:alert(parent.csrfToken)\">CLICK</a>";
  result = new OwaspHtmlSanitizer(html, true, null).sanitize();
  assertFalse(result.contains("Vbscript:alert(parent.csrfToken)"));

  html = "<a href=\"Vbscr&amp;#0009;ip&#009;t\r\n:alert(parent.csrfToken)\">CLICK</a>";
  result = new OwaspHtmlSanitizer(html, true, null).sanitize();
  assertFalse(result.contains("Vbscript:alert(parent.csrfToken)"));

  html = "<a href=\"Vbscr&amp;#0009;ip&#009;t\r&#009\n:alert(parent.csrfToken)\">CLICK</a>";
  result = new OwaspHtmlSanitizer(html, true, null).sanitize();
  assertFalse(result.contains("Vbscript:alert(parent.csrfToken)"));

  html = "<html>" + " <body>\n" + " <a href=\"j\n" + " av\n" + " ascript\n" + " :\n"
    + "alert(1)\n" + "\"\n" + ">XSS(1)</a>\n" + "</body>\n" + "</html>";
  result = new OwaspHtmlSanitizer(html, true, null).sanitize();
  assertEquals(result, "<html><body>\n XSS(1)\n</body></html>");

  html = "<a href=j&#97;v&#97;script&#x3A;&#97;lert(document.domain)>ClickMe</a>";
  result = new OwaspHtmlSanitizer(html, true, null).sanitize();
  // a tag removed
  assertEquals(result, "ClickMe");

 }

 @Test
 void testBug82303() throws Exception {
  String html = "<a href=\"http://ebobby.org/2013/05/18/"
    + "Fun-with-Javascript-and-function-tracing.html\" "
    + "style=\"color: #187AAB; text-decoration: none\" target=\"_blank\">";
  String result = new OwaspHtmlSanitizer(html, true, null).sanitize();
  assertTrue(result.contains("Fun-with-Javascript-and-function-tracing.html"));

  html = "<a href=\"javascript-and-function-tracing.html\" "
    + "style=\"color: #187AAB; text-decoration: none\" target=\"_blank\">";
  result = new OwaspHtmlSanitizer(html, true, null).sanitize();
  assertTrue(result.contains("javascript-and-function-tracing.html"));

  html = "<a href=\"javascript:myJsFunc()\">Link Text</a>";
  result = new OwaspHtmlSanitizer(html, true, null).sanitize();
  assertFalse(result.contains("javascript"));

  html = "<a href=\"javascriptlessDestination.html\" onclick=\"myJSFunc(); "
    + "return false;\">Link text</a>";
  result = new OwaspHtmlSanitizer(html, true, null).sanitize();
  assertTrue(result.contains("javascriptlessDestination.html"));

  html = "<a href=\"javascript:alert('Hello');\"></a>";
  result = new OwaspHtmlSanitizer(html, true, null).sanitize();
  assertFalse(result.contains("javascript"));

  html = "<a href=\"http://ebobby.org/2013/05/18/" + "javascript/Lessonsinjavascript.html\" "
    + "style=\"color: #187AAB; text-decoration: none\" target=\"_blank\">";
  result = new OwaspHtmlSanitizer(html, true, null).sanitize();
  assertTrue(result.contains("javascript/Lessonsinjavascript.html"));

  html = "<a href='javascript:myFunction()'> Click Me! <a/>";
  result = new OwaspHtmlSanitizer(html, true, null).sanitize();
  assertFalse(result.contains("javascript"));

  html = " <a href=\"javascript:void(0)\" onclick=\"loadProducts(<?php echo $categoryId ?>)\"> ";
  result = new OwaspHtmlSanitizer(html, true, null).sanitize();
  assertFalse(result.contains("javascript"));

  html = "<a href=\"#\" onclick=\"someFunction();\" return false;\">LINK</a>";
  result = new OwaspHtmlSanitizer(html, true, null).sanitize();
  assertEquals(result, "<a href=\"#\" rel=\"nofollow\">LINK</a>");

  html = "<a href='javascript:my_Function()'> Click Me! <a/>";
  result = new OwaspHtmlSanitizer(html, true, null).sanitize();
  assertFalse(result.contains("javascript"));

  html = "<a href='javascript:myFunction(field1, field2)'> Click Me! <a/>";
  result = new OwaspHtmlSanitizer(html, true, null).sanitize();
  assertFalse(result.contains("javascript"));

  html = "<a href='javaScript:document.f1.findString(this.t1.value)'>";
  result = new OwaspHtmlSanitizer(html, true, null).sanitize();
  assertFalse(result.contains("javascript"));

  html = "<a href='javaScript:document.f1.findString(this.t1.value)'>";
  result = new OwaspHtmlSanitizer(html, true, null).sanitize();
  assertFalse(result.contains("javascript"));

  html = "<a href=\"#\" onclick=\"findString(document.getElementById('t1').value); return false;\">Click Me</a>";
  result = new OwaspHtmlSanitizer(html, true, null).sanitize();
  assertTrue(result.contains("<a href=\"#\" rel=\"nofollow\">Click Me</a>"));

  html = "<a href=\"javascript:alert('0');\">Click Me</a>";
  result = new OwaspHtmlSanitizer(html, true, null).sanitize();
  assertFalse(result.contains("javascript"));

  html = "<a href=\"  javascript:alert('0');\">Click Me</a>";
  result = new OwaspHtmlSanitizer(html, true, null).sanitize();
  assertFalse(result.contains("javascript"));

 }

 /**
  * Checks that rgb() in style value is not removed.
  *
  * @throws Exception
   */
 @Test
 void testBug67537() throws Exception {
  String html = "<html><body><span style=\"color: rgb(255, 0, 0);\">This is RED</span></body></html>";
  String result = new OwaspHtmlSanitizer(html, true, null).sanitize();
  assertTrue(result.contains("style=\"color:rgb( 255 , 0 , 0 )\""));
 }

 @Test
 void testBug76500() throws Exception {
  String html = "<blockquote style=\"border-left:2px solid rgb(16, 16, 255);\">";
  String result = new OwaspHtmlSanitizer(html, true, null).sanitize();
  assertTrue(result.contains("rgb( 16 , 16 , 255 )"));
 }

 @Test
 void testBug97443() throws Exception {
  String html = "<html><head></head><body><table><tr><td><B>javascript-blocked test </B></td>"
    + "</tr><tr><td><a href=\"javascript:alert('Hello!');\">alert</a>"
    + "</td></tr></table></body></html>";
  String result = new OwaspHtmlSanitizer(html, true, null).sanitize();
  assertTrue(result
    .contains(
      "<body><table><tbody><tr><td><b>javascript-blocked test </b></td></tr><tr><td>alert</td></tr></tbody></table></body>"));

  html = "<html><head><base href=\"http://lbpe.wikispaces.com/\" /></head><body>"
    + "<table><tr><td><B>javascript-blocked test</B></td></tr><tr><td>"
    + "<a href=\"javascript:alert('Hello!');\">alert</a></td></tr></table>"
    + "</body></html>";
  result = new OwaspHtmlSanitizer(html, true, null).sanitize();
  assertTrue(result
    .contains(
      "<body><table><tbody><tr><td><b>javascript-blocked test</b></td></tr><tr><td>alert</td></tr></tbody></table></body>"));
 }

 @Test
 void testBug78902() throws Exception {

  String html = "<html><head></head><body><a target=\"_blank\" href=\"Neptune.gif\"></a></body></html>";
  String result = new OwaspHtmlSanitizer(html, true, null).sanitize();
  assertTrue(result
    .contains(
      "<a href=\"Neptune.gif\" target=\"_blank\" rel=\"nofollow noopener noreferrer\"></a>"));

  html =
    "<html><body>My pictures <a href=\"javascript:document.write('%3C%61%20%68%72%65%66%3D%22%6A%61%76%"
      + "61%73%63%72%69%70%74%3A%61%6C%65%72%74%28%31%29%22%20%6F%6E%4D%6F%75%73%65%4F%76%65%72%3D%61%6C%65%"
      + "72%74%28%5C%22%70%30%77%6E%5C%22%29%3E%4D%6F%75%73%65%20%6F%76%65%72%20%68%65%72%65%3C%2F%61%3E')\">here</a></body></html>";
  result = new OwaspHtmlSanitizer(html, true, null).sanitize();
  assertEquals(result,
    "<html><body>My pictures here</body></html>");

  html = "<html><head></head><body><a target=\"_blank\" href=\"Neptune.txt\"></a></body></html>";
  result = new OwaspHtmlSanitizer(html, true, null).sanitize();
  assertEquals(result,
    "<html><head></head><body><a href=\"Neptune.txt\" target=\"_blank\" rel=\"nofollow noopener noreferrer\"></a></body></html>");

  html = "<html><head></head><body><a target=\"_blank\" href=\"Neptune.pptx\"></a></body></html>";
  result = new OwaspHtmlSanitizer(html, true, null).sanitize();
  assertEquals(result,
    "<html><head></head><body><a href=\"Neptune.pptx\" target=\"_blank\" rel=\"nofollow noopener noreferrer\"></a></body></html>");

  html = "<li><a href=\"poc.zip?view=html&archseq=0\">\"/><script>alert(1);</script>AAAAAAAAAA</a></li>";
  result = new OwaspHtmlSanitizer(html, true, null).sanitize();
  assertFalse(result
    .contains("<script>"));
 }


 /**
  * Tests to make sure we properly defang images that are neither inline/internal nor external
  * images.
  *
  * @throws Exception
   */
 @Test
 void testBug64903() throws Exception {
  String fileName = "bug_60769.txt";
  InputStream htmlStream = getHtmlBody(fileName);
  String html = CharStreams.toString(new InputStreamReader(htmlStream, Charsets.UTF_8));
  String result = new OwaspHtmlSanitizer(html, true, null).sanitize();
  assertTrue(result.contains("pnsrc=\"image001.gif\""));
 }

 /**
  * Checks to make sure we actually defang external content
  *
  * @throws Exception
   */
 @Test
 void testBug64726() throws Exception {
  String fileName = "bug_64726.txt";
  InputStream htmlStream = getHtmlBody(fileName);
  assertNotNull(htmlStream);
  String html = CharStreams.toString(new InputStreamReader(htmlStream, Charsets.UTF_8));
  String result = new OwaspHtmlSanitizer(html, true, null).sanitize();
  // just make sure we made it here, as this was NPEing out..
  assertNotNull(result);
  // Make sure the input got changed
  assertTrue(
    result.contains("dfsrc=\"http://www.google.com/intl/en_com/images/srpr/logo3w.png\""));
 }

 /**
  * Checks to ensure that we're properly removing src for input tags as well.
  *
  * @throws Exception
   */
 @Test
 void testBug58889() throws Exception {
  String fileName = "bug_58889.txt";
  InputStream htmlStream = getHtmlBody(fileName);
  assertNotNull(htmlStream);
  String html = CharStreams.toString(new InputStreamReader(htmlStream, Charsets.UTF_8));
  String result = new OwaspHtmlSanitizer(html, true, null).sanitize();
  assertNotNull(result);
  assertFalse(result.contains(" src=\"https://grepular.com/email_privacy_tester/"));
  assertTrue(result.contains("dfsrc=\"https://grepular.com/email_privacy_tester/"));

  result = new OwaspHtmlSanitizer(html, false, null).sanitize();
  assertNotNull(result);
  assertTrue(result.contains(" src=\"https://grepular.com/email_privacy_tester/"));
  assertFalse(result.contains("dfsrc=\"https://grepular.com/email_privacy_tester/"));
 }

 /**
  * Checks that CDATA section in HTML is reported as a comment and removed.
  *
  * @throws Exception
   */
 @Test
 void testBug64974() throws Exception {
  String html = "<html><body><![CDATA[--><a href=\"data:text/html;base64,PHNjcmlwdD4KYWxlcnQoZG9jdW1lbnQuY29va2llKQo8L3NjcmlwdD4=\">click</a]]></body></html>";
  String result = new OwaspHtmlSanitizer(html, true, null).sanitize();
  assertEquals(result, "<html><body>click</body></html>");
 }

 /**
  * Verify that a new line in a html based signature is maintained after passing through the defanger.
  * @throws Exception
  */
 @Test
 void testBug104666() throws Exception {
  String html =
    "<div></div><div></div><div id=\"5589f382-9e9b-47cd-ab09-3ea973fd4f6a\" data-marker=\"__SIG_PRE__\">"
      + "<div>LIne 1</div>" + "</div>" + "<div>Line 2</div>" + "</div>";
  String result = new OwaspHtmlSanitizer(html, true, null).sanitize();
  assertTrue(result.contains("<div>LIne 1</div></div><div>Line 2</div>"));

  html =
    "<div>Thanks</div><div><img src=\"/home/ews01@zdev-vm002.eng.zimbra.com/Briefcase/rupali.jpeg\" "
      + "data-mce-src=\"/home/ews01@zdev-vm002.eng.zimbra.com/Briefcase/rupali.jpeg\"></div>";
  result = new OwaspHtmlSanitizer(html, true, null).sanitize();
  assertTrue(result.contains("data-mce-src=\"/home/ews01"));
 }

 /**
  * Verify that data-mce-src attributes of img tag are maintained after passing through the defanger.
  * @throws Exception
  */
 @Test
 void testBug106162() throws Exception {

  String html =
    "<div>Thanks</div><div><img src=\"/home/ews01@zdev-vm002.eng.zimbra.com/Briefcase/rupali.jpeg\" "
      + "data-mce-src=\"/home/ews01@zdev-vm002.eng.zimbra.com/Briefcase/rupali.jpeg\"></div>";
  String result = new OwaspHtmlSanitizer(html, true, null).sanitize();
  assertTrue(result.contains("data-mce-src=\"/home/ews01"));
 }

 @Test
 void testBug85478() throws Exception {
  String html = "<a href=\"data:text/html;base64,PHNjcmlwdD5hbGVydCgiSGVsbG8hIik7PC9zY3JpcHQ+\" "
    + "data-mce-href=\"data:text/html;base64,PHNjcmlwdD5hbGVydCgiSGVsbG8hIik7PC9zY3JpcHQ+\">Bug</a>";
  String result = new OwaspHtmlSanitizer(html, true, null).sanitize();
  // make sure that it removed the href link with 'data' URI
  assertEquals(result, "Bug");

  html = "<a href=\"data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAUAErkJggg==\" />Bug</a>";
  result = new OwaspHtmlSanitizer(html, true, null).sanitize();
  assertEquals(result, "Bug");

  html =
    "<a target=_blank href=\"data:text/html,<script>alert(opener.document.body.innerHTML)</script>\">"
      + " clickme in Opera/FF</a>";
  result = new OwaspHtmlSanitizer(html, true, null).sanitize();
  assertEquals(result, "<a target=\"_blank\"> clickme in Opera/FF</a>");

  html = "<a target=_blank href=\"data.html\"> Data fIle</a>";
  result = new OwaspHtmlSanitizer(html, true, null).sanitize();
  assertTrue(result.contains("data.html"));

  html = "<a href=\"data:;base64,iVBORw0KGgoAAAANSUhEUgAAAAUAErkJggg==\" />Bug</a>";
  result = new OwaspHtmlSanitizer(html, true, null).sanitize();
  assertEquals(result, "Bug");

  // make sure that it doesn't remove the img src with 'data' URI
  html = "<img src=\"data:image/jpeg;base64,/9j/4AAAAAxITGlubwIQAABtbnRyUkdCI\"><br>";
  result = new OwaspHtmlSanitizer(html, true, null).sanitize();
  assertTrue(
    result.contains("data:image/jpeg;base64,/9j/4AAAAAxITGlubwIQAABtbnRyUkdCI"));

  html = "<img src=\"DaTa:image/jpeg;base64,/9j/4AAAAAxITGlubwIQAABtbnRyUkdCI\"><br>";
  result = new OwaspHtmlSanitizer(html, true, null).sanitize();
  assertTrue(
    result.contains("DaTa:image/jpeg;base64,/9j/4AAAAAxITGlubwIQAABtbnRyUkdCI"));

  html = "<a href=\"DATA:;base64,iVBORw0KGgoAAAANSUhEUgAAAAUAErkJggg==\" />Bug</a>";
  result = new OwaspHtmlSanitizer(html, true, null).sanitize();
  assertEquals(result, "Bug");

  html = "<a href=\"data\n\n:\n\ntext/html;base64,PHNjcmlwdD5hbGVydCgiSGVsbG8hIik7PC9zY3JpcHQ+\">Bug</a>";
  result = new OwaspHtmlSanitizer(html, true, null).sanitize();
  assertEquals(result, "Bug");

  html = "<a href=\"data\r\n:text/html;base64,PHNjcmlwdD5hbGVydCgiSGVsbG8hIik7PC9zY3JpcHQ+\">Bug</a>";
  result = new OwaspHtmlSanitizer(html, true, null).sanitize();
  assertEquals(result, "Bug");

  html = "<a href=\"data:text/html;base64,PHNjcmlwdD5hbGVydCgiSGVsbG8hIik7PC9zY3JpcHQ+\">Bug</a>";
  result = new OwaspHtmlSanitizer(html, true, null).sanitize();
  assertEquals(result, "Bug");

 }

 @Test
 void testBug83999() throws IOException {

  String html = "<FORM NAME=\"buy\" ENCTYPE=\"text/plain\" " +
    "action=\"http://mail.zimbra.com:7070/service/soap/ModifyFilterRulesRequest\" METHOD=\"POST\">";

  String result = new OwaspHtmlSanitizer(html, true, "mail.zimbra.com").sanitize();

  assertTrue(result.contains("SAMEHOSTFORMPOST-BLOCKED"));

  html = "<FORM NAME=\"buy\" ENCTYPE=\"text/plain\" "
    + "action=\"http://zimbra.vmware.com:7070/service/soap/ModifyFilterRulesRequest\" METHOD=\"POST\">";

  result = new OwaspHtmlSanitizer(html, true, "mail.zimbra.com").sanitize();

  assertFalse(result.contains("SAMEHOSTFORMPOST-BLOCKED"));

  html = "<FORM NAME=\"buy\" ENCTYPE=\"text/plain\" "
    + "action=\"http://mail.zimbra.com/service/soap/ModifyFilterRulesRequest\" METHOD=\"POST\">";

  result = new OwaspHtmlSanitizer(html, true, "mail.zimbra.com").sanitize();
  assertTrue(result.contains("SAMEHOSTFORMPOST-BLOCKED"));

  html = "<FORM NAME=\"buy\" ENCTYPE=\"text/plain\" "
    + "action=\"/service/soap/ModifyFilterRulesRequest\" METHOD=\"POST\">";

  result = new OwaspHtmlSanitizer(html, true, "mail.zimbra.com").sanitize();
  assertTrue(result.contains("SAMEHOSTFORMPOST-BLOCKED"));

  ZThreadLocal.unset();
 }

 @Test
 void testBug102637() throws Exception {
  String html =
    "<html><body><div style=\"font-family: arial, helvetica, sans-serif; font-size: 12pt; color: #000000\">"
      + "<div><br></div><div><a href=\"&amp;#106&amp;#097&amp;#118&amp;#097&amp;#115&amp;#099&amp;#114&amp;#105&amp;"
      + "#112&amp;#116&amp;#058&amp;#097&amp;#108&amp;#101&amp;#114&amp;#116&amp;#040&amp;#039&amp;#088&amp;#083&amp;"
      + "#083&amp;#039&amp;#041\" data-mce-href=\"&amp;#106&amp;#097&amp;#118&amp;#097&amp;#115&amp;#099&amp;"
      + "#114&amp;#105&amp;#112&amp;#116&amp;#058&amp;#097&amp;#108&amp;#101&amp;#114&amp;#116&amp;#040&amp;"
      + "#039&amp;#088&amp;#083&amp;#083&amp;#039&amp;#041\">test</a><br data-mce-bogus=\"1\"></div><div>"
      + "<br data-mce-bogus=\"1\"></div><div>Test message<br data-mce-bogus=\"1\"></div></div></body></html>";
  String result = new OwaspHtmlSanitizer(html, true, null).sanitize();
  assertFalse(result.contains("javascript:alert('XSS')"));
  assertTrue(result.contains(
    "&amp;#106&amp;#097&amp;#118&amp;#097&amp;#115&amp;#099&amp;#114&amp;#105&amp;#112&amp;#116&amp;#058&amp;#097&amp;#108&amp;#101&amp;#114&amp;#116&amp;#040&amp;#039&amp;#088&amp;#083&amp;#083&amp;#039&amp;#041"));
 }

 @Test
 void testBug73037() throws Exception {
  String html = "<html><head></head><body><a target=\"_blank\"" +
    " href=\"smb://Aurora._smb._tcp.local/untitled/folder/03 DANDIYA MIX.mp3\"></a></body></html>";
  String hrefVal = "smb://Aurora._smb._tcp.local/untitled/folder/03%20DANDIYA%20MIX.mp3";
  String result = new OwaspHtmlSanitizer(html, true, null).sanitize();
  assertTrue(result.contains(hrefVal));

  html = "<html><head></head><body><a target=\"_blank\"" +
    " href=\"smb://Aurora._smb._tcp.local/untitled/folder/03%20DANDIYA%20MIX.mp3\"></a></body></html>";
  result = new OwaspHtmlSanitizer(html, true, null).sanitize();
  hrefVal = "smb://Aurora._smb._tcp.local/untitled/folder/03%20DANDIYA%20MIX.mp3";
  assertTrue(result.contains(hrefVal));

  html = "<html><head></head><body><a target=\"_blank\"" +
    " href=\"//Shared_srv/folder/file.txt\"></a></body></html>";
  hrefVal = "//Shared_srv/folder/file.txt";
  result = new OwaspHtmlSanitizer(html, true, null).sanitize();
  assertTrue(result.contains(hrefVal));

  html = "<html><head></head><body><a target=\"_blank\"" +
    " href=\"//Shared_srv/folder/file with spaces.txt\"></a></body></html>";
  hrefVal = "//Shared_srv/folder/file%20with%20spaces.txt";
  result = new OwaspHtmlSanitizer(html, true, null).sanitize();
  assertTrue(result.contains(hrefVal));
 }

 @Test
 void testBug73874() throws Exception {
  String fileName = "bug_73874.txt";
  InputStream htmlStream = getHtmlBody(fileName);
  String html = CharStreams.toString(new InputStreamReader(htmlStream, Charsets.UTF_8));
  String result = new OwaspHtmlSanitizer(html, true, null).sanitize();

  // and make sure we have the the complete URL for
  assertTrue(result
    .contains(
      "https://wiki.tomsawyer.com/download/thumbnails/27132023/Screen&#43;Shot&#43;2012-05-02&#43;at&#43;08.08.12&#43;"
        + "AM.png?version&#61;1&amp;modificationDate&#61;1335967057000"));

  // case where base URL does not have a trailing '/'
  html = "<html><head><base href=\"https://wiki.tomsawyer.com\"/>"
    + "</head><body>"
    + "<img  width=\"100\"  src=\"/download/thumbnails/27132023/Screen+Shot+"
    + "2012-05-02+at+08.08.12+AM.png?version=3D1&modificationDate=3D1335967057"
    + "000\"/></body></html>";
  result = new OwaspHtmlSanitizer(html, true, null).sanitize();
  assertTrue(result
    .contains(
      "https://wiki.tomsawyer.com/download/thumbnails/27132023/Screen&#43;Shot&#43;2012-05-02&#43;at&#43;08.08.12&#43;"
        + "AM.png?version&#61;3D1&amp;modificationDate&#61;3D1335967057000"));

  // case where base URL has a trailing '/'
  html = "<html><head><base href=\"https://wiki.tomsawyer.com/\" />"
    + "</head><body>"
    + "<img  width=\"100\"  src=\"download/thumbnails/27132023/Screen+Shot+"
    + "2012-05-02+at+08.08.12+AM.png?version=3D1&modificationDate=3D1335967057"
    + "000\"/></body></html>";
  result = new OwaspHtmlSanitizer(html, true, null).sanitize();
  assertTrue(result
    .contains(
      "https://wiki.tomsawyer.com/download/thumbnails/27132023/Screen&#43;Shot&#43;2012-05-02&#43;at&#43;08.08.12&#43;"
        + "AM.png?version&#61;3D1&amp;modificationDate&#61;3D1335967057000"));

  // case where base URL has a single parameter'/'
  html = "<html><head><base href=\"https://wiki.tomsawyer.com/\" />"
    + "</head><body>"
    + "<img  width=\"100\"  src=\"download/thumbnails/27132023/Screen+Shot+"
    + "2012-05-02+at+08.08.12+AM.png?version=3D1\"/></body></html>";
  result = new OwaspHtmlSanitizer(html, true, null).sanitize();
  assertTrue(result
    .contains(
      "https://wiki.tomsawyer.com/download/thumbnails/27132023/Screen&#43;Shot&#43;2012-05-02&#43;at&#43;08.08.12&#43;"
        + "AM.png?version&#61;3D1"));

  // case where base URL no parameters
  html = "<html><head><base href=\"https://wiki.tomsawyer.com/\" />"
    + "</head><body>"
    + "<img  width=\"100\"  src=\"download/thumbnails/27132023/Screen+Shot+"
    + "2012-05-02+at+08.08.12+AM.png\"/></body></html>";
  result = new OwaspHtmlSanitizer(html, true, null).sanitize();
  assertTrue(result
    .contains(
      "https://wiki.tomsawyer.com/download/thumbnails/27132023/Screen&#43;Shot&#43;2012-05-02&#43;at&#43;08.08.12&#43;"
        + "AM.png"));

  // case where relative URL is invalidsomething like.pngxxx.gif
  html = "<html><head><base href=\"https://wiki.tomsawyer.com/\" />"
    + "</head><body>"
    + "<img  width=\"100\"  src=\"download/thumbnails/27132023/Screen+Shot.pngTest.gif\"/></body></html>";
  result = new OwaspHtmlSanitizer(html, true, null).sanitize();
  assertFalse(result
    .contains(
      "https://wiki.tomsawyer.com/download/thumbnails/27132023/Screen&#43;Shot&#43;2012-05-02&#43;at&#43;08.08.12&#43;"
        + "AM.png"));
 }

 @Test
 void testBug102910() throws Exception {
  String html = "<div><img pnsrc=\"cid:1040f05975d4d4b8fcf8747be3eb9ae3c08e5cd4@zimbra\" "
    + "data-mce-src=\"cid:1040f05975d4d4b8fcf8747be3eb9ae3c08e5cd4@zimbra\" "
    + "src=\"cid:1040f05975d4d4b8fcf8747be3eb9ae3c08e5cd4@zimbra\"></div>";
  String result = new OwaspHtmlSanitizer(html, true, null).sanitize();
  assertFalse(result.contains("@zimbra"));
  assertTrue(result.contains("&#64;zimbra"));
 }

 @Test
 void testZCS7621() throws Exception {
  String html = "<div class=\"gmail\" style=\"display:none; width:0; overflow:hidden; float:left; max-height:0;\" align=\"center\">";
  String result = new OwaspHtmlSanitizer(html, true, null).sanitize();
  assertTrue(result.contains("display"));
  assertTrue(result.contains("float"));
 }

 @Test
 void testZCS7784() throws Exception {
  String html = "<img class=\"gmail\" style=\"display:none; width:0; overflow:hidden;\" src=\"https://localhost:8443/service/home/~/?auth=co&loc=en_US&id=285&part=2.2\" >";
  String result = new OwaspHtmlSanitizer(html, true, null).sanitize();
  assertTrue(result.contains("style"));
 }

 @Test
 void testZBUG1215() throws Exception {
  String html = "<div id=\"noticias\"><div class=\"bloque\">BLOQUESSS</div></div>";
  String result = new OwaspHtmlSanitizer(html, true, null).sanitize();
  // check that the id and class attributes are not removed
  assertEquals(result, html);
 }

 //Start working tests section

 @Test
 void testBug101813() throws Exception {
  String html = "<textarea><img title=\"</<!-- -->textarea><img src=x onerror=alert(1)></img>";
  String result = new OwaspHtmlSanitizer(html, true, null).sanitize();
  // make sure that the javascript content is escaped
  assertTrue(result.contains("onerror&#61;alert(1)&gt;"));

  html = "<textarea><IMG title=\"</<!-- -->textarea><img src=x onerror=alert(1)></img>";
  result = new OwaspHtmlSanitizer(html, true, null).sanitize();
  assertTrue(result.contains("onerror&#61;alert(1)&gt;"));

  html = "<textarea><   img title=\"</<!-- -->textarea><img src=x onerror=alert(1)></img>";
  result = new OwaspHtmlSanitizer(html, true, null).sanitize();
  assertTrue(result.contains("onerror&#61;alert(1)&gt;"));
 }


 /**
  * Tests to make sure target="_blank" is added to anythign with an href
  *
  * @throws Exception
   */
 @Test
 void testBug46948() throws Exception {
  String fileName = "bug_46948.txt";
  InputStream htmlStream = getHtmlBody(fileName);
  String html = CharStreams.toString(new InputStreamReader(htmlStream, Charsets.UTF_8));
  String result = new OwaspHtmlSanitizer(html, true, null).sanitize();
  // Make sure each area tag has a target
  int index = result.indexOf("<area");
  while (index >= 0) {
   int closingIndex = result.indexOf(">", index);
   int targetIndex = result.indexOf("target=", index);
   // Make sure we got a target
   assertTrue(targetIndex != -1);
   // make sure its before the closing tag
   assertTrue(targetIndex < closingIndex);
   index = result.indexOf("<area", index + 1);
  }
 }
}
