// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.html.owasp.policies;

import com.zimbra.common.localconfig.DebugConfig;
import java.util.List;
import java.util.regex.Pattern;
import org.owasp.html.ElementPolicy;

public class ImgInputElementPolicy implements ElementPolicy {

  private static final Pattern VALID_IMG_FILE = Pattern.compile(DebugConfig.defangValidImgFile);
  private static final Pattern VALID_INT_IMG =
      Pattern.compile(DebugConfig.defangValidIntImg, Pattern.CASE_INSENSITIVE);
  private static final Pattern VALID_EXT_URL =
      Pattern.compile(DebugConfig.defangValidExtUrl, Pattern.CASE_INSENSITIVE);
  // matches the file format that convertd uses so it doesn't get removed
  private static final Pattern VALID_CONVERTD_FILE =
      Pattern.compile(DebugConfig.defangValidConvertdFile);

  @Override
  public String apply(String elementName, List<String> attrs) {
    final int srcIndex = attrs.indexOf("src");
    if (srcIndex == -1) {
      return elementName;
    }
    String srcValue = attrs.get(srcIndex + 1);
    if (VALID_EXT_URL.matcher(srcValue).find()
        || (!VALID_INT_IMG.matcher(srcValue).find() && !VALID_IMG_FILE.matcher(srcValue).find())) {
      attrs.remove(srcIndex);
      attrs.remove(srcIndex); // value
      attrs.add("dfsrc");
      attrs.add(srcValue);
    } else if (!VALID_INT_IMG.matcher(srcValue).find()
        && VALID_IMG_FILE.matcher(srcValue).find()
        && !VALID_CONVERTD_FILE.matcher(srcValue).find()) {
      attrs.remove(srcIndex);
      attrs.remove(srcIndex); // value
      attrs.add("pnsrc");
      attrs.add(srcValue);
    }
    return elementName;
  }
}
