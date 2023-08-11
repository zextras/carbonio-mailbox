// SPDX-FileCopyrightText: 2023 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: AGPL-3.0-only

package com.zextras.mailbox.resource.preview;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.zextras.carbonio.preview.PreviewClient;
import com.zextras.carbonio.preview.queries.Query;
import com.zextras.carbonio.preview.queries.Query.QueryBuilder;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class PreviewTypeTest {

  private static Stream<Arguments> getPreviewTypeFunctions() {
    return Stream.of(
        Arguments.of(PreviewType.PDF, "postPreviewOfPdf"),
        Arguments.of(PreviewType.PDF_THUMBNAIL, "postThumbnailOfPdf"),
        Arguments.of(PreviewType.DOC, "postPreviewOfDocument"),
        Arguments.of(PreviewType.DOC_THUMBNAIL, "postThumbnailOfDocument"),
        Arguments.of(PreviewType.IMAGE, "postPreviewOfImage"),
        Arguments.of(PreviewType.IMAGE_THUMBNAIL, "postThumbnailOfImage"));
  }

  @ParameterizedTest
  @MethodSource("getPreviewTypeFunctions")
  void shouldInvokeCorrectPreviewMethodForPreviewType(PreviewType type, String expectedMethodName)
      throws Exception {
    final PreviewClient previewClient = mock(PreviewClient.class);
    final InputStream inputStream =
        new ByteArrayInputStream("Hello".getBytes(StandardCharsets.UTF_8));
    final Query query = new QueryBuilder().build();
    final String fileName = "test";
    type.getFunction().apply(previewClient, inputStream, query, fileName);
    final Method testMethod =
        previewClient
            .getClass()
            .getMethod(expectedMethodName, InputStream.class, Query.class, String.class);
    assertDoesNotThrow(
        () -> testMethod.invoke(verify(previewClient, times(1)), inputStream, query, fileName));
  }
}
