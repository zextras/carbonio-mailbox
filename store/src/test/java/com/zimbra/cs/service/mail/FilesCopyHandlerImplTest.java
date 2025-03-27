package com.zimbra.cs.service.mail;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import javax.mail.MessagingException;
import javax.mail.internet.MimePart;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class FilesCopyHandlerImplTest {

  private MimePart mimePart;

  @BeforeEach
  void setUp() {
    mimePart = mock(MimePart.class);
  }

  @Test
  void shouldReturnFileNameFromContentDispositionHeader() throws MessagingException {
    String contentDispositionHeader = "attachment; filename=\"имя_файла.txt\"";
    when(mimePart.getHeader("Content-Disposition", null)).thenReturn(contentDispositionHeader);

    String filename = FilesCopyHandlerImpl.getRawFileName(mimePart);

    assertEquals("имя_файла.txt", filename);
  }

  @Test
  void shouldReturnFileNameFromContentTypeHeader() throws MessagingException {
    when(mimePart.getHeader("Content-Disposition", null)).thenReturn(null);

    String contentTypeHeader = "application/octet-stream; name=\"имя_файла.txt\"";
    when(mimePart.getHeader("Content-Type", null)).thenReturn(contentTypeHeader);

    String filename = FilesCopyHandlerImpl.getRawFileName(mimePart);

    assertEquals("имя_файла.txt", filename);
  }

  @Test
  void shouldReturnEmptyFileNameForContentDispositionInvalidEncoding()
      throws MessagingException {
    String contentDispositionHeader = "attachment; filename*=UTF-8,%E6%96%87%E4%BB%B6.txt";
    when(mimePart.getHeader("Content-Disposition", null)).thenReturn(contentDispositionHeader);

    String filename = FilesCopyHandlerImpl.getRawFileName(mimePart);

    assertEquals("", filename);
  }

  @Test
  void shouldReturnEmptyFileNameForContentTypeInvalidEncoding()
      throws MessagingException {
    String contentTypeHeader = "application/octet-stream; name=UTF-8,%E6%96%87%E4%BB%B6.txt";
    when(mimePart.getHeader("Content-Type", null)).thenReturn(contentTypeHeader);

    String filename = FilesCopyHandlerImpl.getRawFileName(mimePart);

    assertEquals("", filename);
  }
}

