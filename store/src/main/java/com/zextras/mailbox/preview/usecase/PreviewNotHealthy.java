// SPDX-FileCopyrightText: 2023 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: AGPL-3.0-only

package com.zextras.mailbox.preview.usecase;

public class PreviewNotHealthy extends RuntimeException {

  private static final long serialVersionUID = -1169999965696039740L;

  public PreviewNotHealthy() {
    super("Preview Service is not healthy");
  }
}
