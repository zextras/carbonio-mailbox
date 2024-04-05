// SPDX-FileCopyrightText: 2024 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: AGPL-3.0-only

package com.zextras.mailbox.tracking;

import io.vavr.control.Try;

public interface Tracking {

  // curl -i "https://analytics.zextras.tools/matomo.php?
  // idsite=7 -> TODO: is idsite always the same? for now let's use a static value
  // &rec=1&send_image=0&apiv=1 -> these params are static

  // &e_c=Mail -> Category
  // &e_a=SendEmailWithSmartLinks&  -> Action
  // uid=UserA" -> user
  // -> these are dynamic

  Try<Void> sendEvent(Event event);

}
