// SPDX-FileCopyrightText: 2024 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: AGPL-3.0-only

package com.zimbra.cs.service.mail;

import com.zextras.mailbox.tracking.Event;
import com.zextras.mailbox.tracking.Tracking;
import io.vavr.control.Try;

/**
 * Mail Service registration API that uses a fake tracking to avoid sending data to Matomo.
 *
 */
public class MailServiceWithoutTracking extends MailService {

  @Override
  protected Tracking getTracking() {
    return new FakeTracking();
  }

  static class FakeTracking implements Tracking {

    @Override
    public void sendEventIgnoringFailure(Event event) {
    }
  }
}
