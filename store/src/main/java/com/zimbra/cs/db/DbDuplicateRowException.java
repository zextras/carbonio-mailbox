// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

/*
 * Created on 2004. 6. 1.
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package com.zimbra.cs.db;

import java.sql.SQLException;

/**
 * @author jhahm
 *     <p>TODO To change the template for this generated type comment go to Window - Preferences -
 *     Java - Code Generation - Code and Comments
 */
public class DbDuplicateRowException extends SQLException {

  /**
   * @param msg
   */
  public DbDuplicateRowException(SQLException cause) {
    super("Detected duplicate row being inserted");
    setNextException(cause);
  }
}
