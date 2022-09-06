// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.extension;

import com.zimbra.common.service.ServiceException;

/**
 * Zimbra extension. An extension to the Zimbra server is packaged as a jar file with its manifest
 * containing the header:
 *
 * <p><code>
 *   Zimbra-Extension-Class: <i>name of implementation class of this interface</i>
 * </code>
 *
 * <p>The extension is deployed by dropping the jar file into the
 * <i>zimbra_home</i>/lib/ext/<i>ext</i> directory. It is loaded upon server startup.
 */
public interface ZimbraExtension {

  /**
   * Defines a name for the extension. It must be an identifier.
   *
   * @return extension name
   */
  String getName();

  /**
   * Initializes the extension. Called when the extension is loaded.
   *
   * @throws ExtnsionException voluntarily resign from the registration
   * @throws ServiceException error
   */
  void init() throws ExtensionException, ServiceException;

  /**
   * Terminates the extension. Called when the server is shut down or this extension is
   * unregistered.
   */
  void destroy();
}
