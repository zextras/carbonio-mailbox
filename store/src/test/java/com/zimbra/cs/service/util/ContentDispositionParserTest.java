package com.zimbra.cs.service.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ContentDispositionParserTest {

  @Test
  @DisplayName("Extended filename should be returned when present")
  void getFileNameFromContentDisposition_should_returnValidFileNameWithExtendedEncoding() {
    final String contentDisposition =
        "attachment; filename=\"Fruten.txt\";"
            + " filename*=UTF-8''%D0%A4%D1%80%D1%83%D1%82%D0%B5%D0%BD.txt";
    final String fileName =
        ContentDispositionParser.getFileNameFromContentDisposition(contentDisposition);

    assertEquals("Фрутен.txt", fileName);
  }

  @Test
  @DisplayName(
      "ASCII filename should be preferred when extended filename is missing in content-disposition")
  void getFileNameFromContentDisposition_should_returnValidFileNameWithoutExtendedEncoding() {
    final String contentDisposition = "attachment; filename=\"ang.txt\"";
    final String fileName =
        ContentDispositionParser.getFileNameFromContentDisposition(contentDisposition);

    assertEquals("ang.txt", fileName);
  }

  @Test
  @DisplayName("Null content-disposition should return empty filename")
  void getFileNameFromContentDisposition_should_returnEmptyFilenameForInvalidContentDisposition() {
    final String contentDisposition = null;
    @SuppressWarnings("ConstantConditions")
    final String fileName =
        ContentDispositionParser.getFileNameFromContentDisposition(contentDisposition);

    assertEquals("", fileName);
  }

  @Test
  @DisplayName("Invalid encoding (missing '') should return empty filename")
  void getFileNameFromContentDisposition_should_returnEmptyFilenameForInvalidEncoding() {
    final String contentDisposition = "attachment; filename*=UTF-8,%E6%96%87%E4%BB%B6.txt";
    final String fileName =
        ContentDispositionParser.getFileNameFromContentDisposition(contentDisposition);

    assertEquals("", fileName);
  }
}
