// SPDX-FileCopyrightText: 2023 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: AGPL-3.0-only

package com.zextras.mailbox.resource.preview;

import static org.junit.jupiter.api.Assertions.*;

import com.zextras.carbonio.preview.queries.Query;
import com.zextras.carbonio.preview.queries.Query.QueryBuilder;
import com.zextras.carbonio.preview.queries.enums.Format;
import com.zextras.carbonio.preview.queries.enums.Quality;
import com.zextras.carbonio.preview.queries.enums.Shape;
import io.vavr.control.Try;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

class PreviewQueryUtilTest {

  private static Stream<Arguments> getCorrectQuery() {
    return Stream.of(
        Arguments.of(
            "first_page=1&last_page=2",
            "9x9",
            new QueryBuilder().setFirstPage(1).setLastPage(2).setPreviewArea("9x9").build()),
        Arguments.of(
            "first_page=1&first_page=20&output_format=jpeg",
            "9x9",
            new QueryBuilder()
                .setFirstPage(1)
                .setPreviewArea("9x9")
                .setOutputFormat(Format.JPEG)
                .build()),
        Arguments.of(
            "first_page=1&first_page=20&output_format=png",
            "9x9",
            new QueryBuilder()
                .setFirstPage(1)
                .setPreviewArea("9x9")
                .setOutputFormat(Format.PNG)
                .build()),
        Arguments.of(
            "first_page=1&first_page=20&output_format=gif",
            "9x9",
            new QueryBuilder()
                .setFirstPage(1)
                .setPreviewArea("9x9")
                .setOutputFormat(Format.GIF)
                .build()),
        Arguments.of(
            "first_page=1&last_page=2&quality=HIGH&crop=false&shape=RECTANGULAR",
            "9x9",
            new QueryBuilder()
                .setFirstPage(1)
                .setLastPage(2)
                .setQuality(Quality.HIGH)
                .setCrop(false)
                .setShape(Shape.RECTANGULAR)
                .setPreviewArea("9x9")
                .build()),
        Arguments.of("quality=high", "", new QueryBuilder().setQuality(Quality.HIGH).build()),
        Arguments.of("crop=unknown", "", new QueryBuilder().setCrop(false).build()),
        Arguments.of("crop=TRUE", "", new QueryBuilder().setCrop(true).build()),
        Arguments.of("unknownKey=1", "", new QueryBuilder().build()),
        Arguments.of(null, "", new QueryBuilder().build()));
  }

  @ParameterizedTest
  @MethodSource("getCorrectQuery")
  void shouldHandleCorrectQueryValues(String query, String area, Query expected) {
    final Query previewQuery = PreviewQueryUtil.getPreviewQuery(query, area);
    assertEquals(expected.toString(), previewQuery.toString());
  }

  @ParameterizedTest
  @ValueSource(
      strings = {"first_page=string", "last_page=string", "quality=UNKNOWN", "shape=UNKNOWN"})
  void shouldExplodeOnIncorrectQueryValues(String query) {
    assertTrue(Try.of(() -> PreviewQueryUtil.getPreviewQuery(query, "")).isFailure());
  }
}
