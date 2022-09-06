// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.common.soap;

public final class ZimletConstants {

  /* top level */
  public static final String ZIMLET_TAG_ZIMLET = "zimlet";

  /* first level */
  public static final String ZIMLET_ATTR_VERSION = "version";
  public static final String ZIMLET_ATTR_DESCRIPTION = "description";
  public static final String ZIMLET_ATTR_ZIMBRAX_SEMVER = "zimbraXZimletCompatibleSemVer";
  public static final String ZIMLET_ATTR_NAME = "name";
  public static final String ZIMLET_ATTR_EXTENSION = "extension";

  public static final String ZIMLET_TAG_SCRIPT = "include";
  public static final String ZIMLET_TAG_CSS = "includeCSS";
  public static final String ZIMLET_TAG_CONTENT_OBJECT = "contentObject";
  /* value was "panelItem" - believe this was in error */
  public static final String ZIMLET_TAG_PANEL_ITEM = "zimletPanelItem";

  /* for serverExtension branch */
  public static final String ZIMLET_TAG_SERVER_EXTENSION = "serverExtension";
  public static final String ZIMLET_ATTR_HAS_KEYWORD = "hasKeyword";
  public static final String ZIMLET_ATTR_MATCH_ON = "matchOn";
  public static final String ZIMLET_ATTR_EXTENSION_CLASS = "extensionClass";
  public static final String ZIMLET_ATTR_REGEX = "regex";

  /* config description file */
  public static final String ZIMLET_TAG_CONFIG = "zimletConfig";

  public static final String ZIMLET_TAG_GLOBAL = "global";
  public static final String ZIMLET_TAG_HOST = "host";
  public static final String ZIMLET_TAG_PROPERTY = "property";

  public static final String ZIMLET_TAG_TARGET = "target";
  public static final String ZIMLET_TAG_LABEL = "label";
  public static final String ZIMLET_DISABLE_UI_UNDEPLOY = "disableUIUndeploy";
}
