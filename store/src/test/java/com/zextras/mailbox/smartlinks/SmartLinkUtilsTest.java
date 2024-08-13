package com.zextras.mailbox.smartlinks;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.mailbox.Message;
import com.zimbra.cs.mime.MPartInfo;
import com.zimbra.cs.mime.Mime;
import com.zimbra.cs.service.mail.message.parser.ParseMimeMessage;
import java.util.ArrayList;
import java.util.List;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimePart;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


class SmartLinkUtilsTest {

  private Message messageMock;
  private MimeMessage mimeMessageMock;

  @BeforeEach
  void setUp() {
    messageMock = mock(Message.class);
    mimeMessageMock = mock(MimeMessage.class);
  }

  @Test
  void testGetSmartLinkAwareMimeMessageSize_noSmartLinks() throws Exception {

    try (var mockedStatic = mockStatic(Mime.class)) {
      when(messageMock.getMimeMessage()).thenReturn(mimeMessageMock);
      List<MPartInfo> parts = new ArrayList<>();
      var partInfoMock = mock(MPartInfo.class);
      var mimePartMock = mock(MimePart.class);

      when(partInfoMock.isMultipart()).thenReturn(false);
      when(partInfoMock.getMimePart()).thenReturn(mimePartMock);
      when(mimePartMock.getHeader(ParseMimeMessage.SMART_LINK_HEADER, null)).thenReturn(null);
      when(mimePartMock.getSize()).thenReturn(500);
      parts.add(partInfoMock);

      mockedStatic.when(() -> Mime.getParts(mimeMessageMock)).thenReturn(parts);

      var size = SmartLinkUtils.getSmartLinkAwareMimeMessageSize(messageMock);
      assertEquals(500L, size);
    }
  }

  @Test
  void testGetSmartLinkAwareMimeMessageSize_withSmartLinks() throws Exception {
    try (var mockedStatic = mockStatic(Mime.class)) {
      when(messageMock.getMimeMessage()).thenReturn(mimeMessageMock);

      var partInfoMock = mock(MPartInfo.class);
      var mimePartMock = mock(MimePart.class);

      when(partInfoMock.isMultipart()).thenReturn(false);
      when(partInfoMock.getMimePart()).thenReturn(mimePartMock);
      when(mimePartMock.getHeader(ParseMimeMessage.SMART_LINK_HEADER, null)).thenReturn("true");

      List<MPartInfo> parts = new ArrayList<>();
      parts.add(partInfoMock);

      mockedStatic.when(() -> Mime.getParts(mimeMessageMock)).thenReturn(parts);

      var size = SmartLinkUtils.getSmartLinkAwareMimeMessageSize(messageMock);

      assertEquals(250L, size);
    }
  }


  @Test
  void testGetSmartLinkAwareMimeMessageSize_mixedParts() throws Exception {
    try (var mockedStatic = mockStatic(Mime.class)) {
      when(messageMock.getMimeMessage()).thenReturn(mimeMessageMock);
      List<MPartInfo> parts = new ArrayList<>();

      // Part with smart link
      var smartLinkPartInfo = mock(MPartInfo.class);
      var smartLinkPart = mock(MimePart.class);
      when(smartLinkPartInfo.isMultipart()).thenReturn(false);
      when(smartLinkPartInfo.getMimePart()).thenReturn(smartLinkPart);
      when(smartLinkPart.getHeader(ParseMimeMessage.SMART_LINK_HEADER, null)).thenReturn("true");
      parts.add(smartLinkPartInfo);

      // Regular part
      var regularPartInfo = mock(MPartInfo.class);
      var regularPart = mock(MimePart.class);
      when(regularPartInfo.isMultipart()).thenReturn(false);
      when(regularPartInfo.getMimePart()).thenReturn(regularPart);
      when(regularPart.getHeader(ParseMimeMessage.SMART_LINK_HEADER, null)).thenReturn(null);
      when(regularPart.getSize()).thenReturn(500);
      parts.add(regularPartInfo);

      mockedStatic.when(() -> Mime.getParts(mimeMessageMock)).thenReturn(parts);

      var size = SmartLinkUtils.getSmartLinkAwareMimeMessageSize(messageMock);
      assertEquals(750L, size); // 500 bytes regular part + 250 bytes smart link
    }
  }

  @Test
  void testGetSmartLinkAwareMimeMessageSize_nullMimeMessage() throws Exception {
    when(messageMock.getMimeMessage()).thenReturn(null);

    var size = SmartLinkUtils.getSmartLinkAwareMimeMessageSize(messageMock);
    assertEquals(0L, size);
  }

  @Test
  void testGetSmartLinkAwareMimeMessageSize_messagingException() throws Exception {
    when(messageMock.getMimeMessage()).thenReturn(mimeMessageMock);
    when(Mime.getParts(mimeMessageMock)).thenThrow(new MessagingException());

    assertThrows(ServiceException.class, () -> SmartLinkUtils.getSmartLinkAwareMimeMessageSize(messageMock));
  }
}