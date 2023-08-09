// SPDX-FileCopyrightText: 2023 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: AGPL-3.0-only

package com.zextras.mailbox.resource.preview;

import com.google.common.base.Strings;
import com.zextras.carbonio.preview.queries.Query;
import com.zextras.carbonio.preview.queries.Query.QueryBuilder;
import com.zextras.carbonio.preview.queries.enums.Quality;
import com.zextras.carbonio.preview.queries.enums.Shape;
import java.util.Hashtable;
import java.util.Objects;
import javax.servlet.http.HttpUtils;
import org.apache.commons.lang.BooleanUtils;

/**
 * Utility class to manipulate {@link com.zextras.carbonio.preview.PreviewClient} query from an
 * input
 */
public class PreviewQueryUtil {

  /**
   * Builds a {@link QueryBuilder} from received query String and area. quality and shape values are
   * case-insensitive but must match. crop will return false for anything other than "true"
   *
   * @param queryString a query string without question mark and slash on beginning. e.g.:
   *     first_page=1&last_page=2 is ok ?first_page=1 is not ok /?first_page=1 is not ok too.
   * @param area comes from the url path and is optional
   * @return
   */
  public static Query getPreviewQuery(String queryString, String area) {
    if (Objects.isNull(queryString)) {
      queryString = "";
    }
    final Hashtable<String, String[]> parseQueryString = HttpUtils.parseQueryString(queryString);
    final QueryBuilder queryBuilder = new QueryBuilder();
    if (!Strings.isNullOrEmpty(area)) {
      queryBuilder.setPreviewArea(area);
    }
    final String[] shapes = parseQueryString.get("shape");
    if (!Objects.isNull(shapes) && shapes.length > 0) {
      final String shapeString = shapes[0].toUpperCase();
      final Shape shape = Shape.valueOf(shapeString);
      queryBuilder.setShape(shape);
    }
    final String[] firstPages = parseQueryString.get("first_page");
    if (!Objects.isNull(firstPages) && firstPages.length > 0) {
      final int firstPage = Integer.parseInt(firstPages[0]);
      queryBuilder.setFirstPage(firstPage);
    }
    final String[] lastPages = parseQueryString.get("last_page");
    if (!Objects.isNull(lastPages) && lastPages.length > 0) {
      final int lastPage = Integer.parseInt(lastPages[0]);
      queryBuilder.setLastPage(lastPage);
    }
    final String[] crops = parseQueryString.get("crop");
    if (!Objects.isNull(crops) && crops.length > 0) {
      queryBuilder.setCrop(BooleanUtils.toBoolean(crops[0]));
    }
    final String[] qualities = parseQueryString.get("quality");
    if (!Objects.isNull(qualities) && qualities.length > 0) {
      final String qualityString = qualities[0].toUpperCase();
      final Quality quality = Quality.valueOf(qualityString);
      queryBuilder.setQuality(quality);
    }
    return queryBuilder.build();
  }
}
