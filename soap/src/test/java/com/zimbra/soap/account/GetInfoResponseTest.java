// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.account;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.zimbra.common.soap.Element;
import com.zimbra.soap.JaxbUtil;
import com.zimbra.soap.account.message.GetInfoResponse;
import com.zimbra.soap.account.type.Identity;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Unit test for {@link GetInfoResponse}.
 *
 * @author ysasaki
 */
public class GetInfoResponseTest {

  private static Unmarshaller unmarshaller;

  @BeforeAll
  public static void init() throws Exception {
    JAXBContext jaxb = JAXBContext.newInstance(GetInfoResponse.class);
    unmarshaller = jaxb.createUnmarshaller();
  }

  private void checkAsserts(GetInfoResponse result) {
    List<Identity> identities = result.getIdentities();
    assertEquals(1, identities.size());
    assertEquals(
        "Identity{a=[Attr{name=zimbraPrefIdentityId, value=91e6d036-5d5e-4788-9bc2-5b65e8c2480c},"
            + " Attr{name=zimbraPrefSaveToSent, value=TRUE},"
            + " Attr{name=zimbraPrefForwardReplyPrefixChar, value=>},"
            + " Attr{name=zimbraPrefSentMailFolder, value=sent}, Attr{name=zimbraPrefFromDisplay,"
            + " value=Demo User One}, Attr{name=zimbraPrefForwardIncludeOriginalText,"
            + " value=includeBody}, Attr{name=zimbraPrefForwardReplyFormat, value=same},"
            + " Attr{name=zimbraPrefMailSignatureStyle, value=outlook},"
            + " Attr{name=zimbraPrefIdentityName, value=DEFAULT}, Attr{name=zimbraCreateTimestamp,"
            + " value=20120528071949Z}, Attr{name=zimbraPrefReplyIncludeOriginalText,"
            + " value=includeBody}, Attr{name=zimbraPrefFromAddress, value=user1@tarka.local},"
            + " Attr{name=zimbraPrefDefaultSignatureId,"
            + " value=28fa4fec-a5fb-4dc8-acf9-df930bb13546}], name=DEFAULT,"
            + " id=91e6d036-5d5e-4788-9bc2-5b65e8c2480c}",
        identities.get(0).toString());
    Collection<String> sigHtml = result.getPrefsMultimap().get("zimbraPrefMailSignatureHTML");
    assertNotNull(sigHtml);
    String sig = sigHtml.iterator().next();
    // Full comparison failing on Jenkins system due to environmental charset issues
    // Re-enabled stricter test.  Assuming use of Unicode escapes \u00F3 (twice) gets around this
    // issue:
    //     Assert.assertTrue("Signature", sig.endsWith("signature test"));
    assertEquals(
        "\u003Cstrong\u003Ef\u00F3\u00F3 utf8\u003C/strong\u003E signature test", sig);
    assertTrue(result.getIsTrackingIMAP(), "isTrackingIMAP should be 'TRUE'");
  }

  @Test
  void unmarshall() throws Exception {
    checkAsserts(
        (GetInfoResponse)
            unmarshaller.unmarshal(getClass().getResourceAsStream("GetInfoResponse.xml")));
  }

  @Test
  void jaxbUtilUnmarshall() throws Exception {
    // same as unmarshall but use JaxbUtil; this provokes/tests issues with utf8 conversion
    checkAsserts(
        Objects.requireNonNull(JaxbUtil.elementToJaxb(
            Element.parseXML(getClass().getResourceAsStream("GetInfoResponse.xml")))));
  }
}
