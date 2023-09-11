// SPDX-FileCopyrightText: 2023 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: AGPL-3.0-only

package com.zextras.mailbox.preview.resource;

import com.google.common.base.Strings;
import com.zextras.carbonio.preview.queries.Query;
import com.zextras.carbonio.preview.queries.Query.QueryBuilder;
import com.zextras.carbonio.preview.queries.enums.Format;
import com.zextras.carbonio.preview.queries.enums.Quality;
import com.zextras.carbonio.preview.queries.enums.Shape;
import java.util.Objects;

/** Utility class to map {@link PreviewController} query to a preview service {@link Query} */
public class PreviewQueryUtil {

  /**
   * Creates a PreviewQuery from input parameters.
   *
   * @param area
   * @param outputFormat
   * @param firstPage
   * @param lastPage
   * @param crop
   * @param quality
   * @param shape
   * @return {@link Query} fro preview
   */
  public static Query getPreviewQuery(
      String area,
      String outputFormat,
      String firstPage,
      String lastPage,
      Boolean crop,
      String quality,
      String shape) {
    final QueryBuilder queryBuilder = new QueryBuilder();
    if (!Strings.isNullOrEmpty(area)) {
      queryBuilder.setPreviewArea(area);
    }
    if (!Strings.isNullOrEmpty(outputFormat)) {
      queryBuilder.setOutputFormat(Format.valueOf(outputFormat.toUpperCase()));
    }

    if (!Strings.isNullOrEmpty(shape)) {
      queryBuilder.setShape(Shape.valueOf(shape.toUpperCase()));
    }

    if (!Strings.isNullOrEmpty(firstPage)) {
      queryBuilder.setFirstPage(Integer.parseInt(firstPage));
    }

    if (!Strings.isNullOrEmpty(lastPage)) {
      queryBuilder.setLastPage(Integer.parseInt(lastPage));
    }
    if (!Objects.isNull(crop)) {
      queryBuilder.setCrop(crop);
    }

    if (!Strings.isNullOrEmpty(quality)) {
      queryBuilder.setQuality(Quality.valueOf(quality.toUpperCase()));
    }
    return queryBuilder.build();
  }
}
