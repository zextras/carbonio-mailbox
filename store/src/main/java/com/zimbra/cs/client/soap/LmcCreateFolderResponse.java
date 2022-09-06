// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.client.soap;

import com.zimbra.cs.client.*;

public class LmcCreateFolderResponse extends LmcSoapResponse {

  private LmcFolder mFolder;

  public LmcFolder getFolder() {
    return mFolder;
  }

  public void setFolder(LmcFolder f) {
    mFolder = f;
  }
}
