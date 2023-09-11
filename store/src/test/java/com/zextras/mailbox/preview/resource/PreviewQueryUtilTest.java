// SPDX-FileCopyrightText: 2023 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: AGPL-3.0-only

package com.zextras.mailbox.preview.resource;

import static org.junit.jupiter.api.Assertions.*;

import com.zextras.carbonio.preview.queries.Query;
import com.zextras.carbonio.preview.queries.Query.QueryBuilder;
import com.zextras.carbonio.preview.queries.enums.Format;
import com.zextras.carbonio.preview.queries.enums.Quality;
import com.zextras.carbonio.preview.queries.enums.Shape;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class PreviewQueryUtilTest {

  private static Stream<Arguments> getQueryParamsAndExpected() {
    return Stream.of(
        Arguments.of(
            "9x9",
            null,
            null,
            null,
            null,
            null,
            null,
            new QueryBuilder().setPreviewArea("9x9").build()),
        Arguments.of(
            "9x9",
            "jpeg",
            "1",
            "2",
            null,
            null,
            null,
            new QueryBuilder()
                .setFirstPage(1)
                .setLastPage(2)
                .setPreviewArea("9x9")
                .setOutputFormat(Format.JPEG)
                .build()),
        Arguments.of(
            "9x9",
            "png",
            "1",
            "2",
            null,
            null,
            null,
            new QueryBuilder()
                .setFirstPage(1)
                .setLastPage(2)
                .setPreviewArea("9x9")
                .setOutputFormat(Format.PNG)
                .build()),
        Arguments.of(
            "9x9",
            "gif",
            "17",
            null,
            null,
            null,
            null,
            new QueryBuilder()
                .setFirstPage(17)
                .setPreviewArea("9x9")
                .setOutputFormat(Format.GIF)
                .build()),
        Arguments.of(
            "9x9",
            "png",
            "1",
            "2",
            true,
            "highest",
            "rectangular",
            new QueryBuilder()
                .setFirstPage(1)
                .setLastPage(2)
                .setPreviewArea("9x9")
                .setCrop(true)
                .setQuality(Quality.HIGHEST)
                .setOutputFormat(Format.PNG)
                .setShape(Shape.RECTANGULAR)
                .build()),
        Arguments.of(
            "9x9",
            "png",
            "1",
            "2",
            true,
            "highest",
            "rounded",
            new QueryBuilder()
                .setFirstPage(1)
                .setLastPage(2)
                .setPreviewArea("9x9")
                .setCrop(true)
                .setQuality(Quality.HIGHEST)
                .setOutputFormat(Format.PNG)
                .setShape(Shape.ROUNDED)
                .build()),
        Arguments.of(null, null, null, null, null, null, null, new QueryBuilder().build()));
  }

  @ParameterizedTest
  @MethodSource("getQueryParamsAndExpected")
  void shouldHandleCorrectQueryValues(
      String area,
      String outputFormat,
      String firstPage,
      String lastPage,
      Boolean crop,
      String quality,
      String shape,
      Query expected) {
    final Query previewQuery =
        PreviewQueryUtil.getPreviewQuery(
            area, outputFormat, firstPage, lastPage, crop, quality, shape);
    assertEquals(expected.toString(), previewQuery.toString());
  }
}
