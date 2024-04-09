// SPDX-FileCopyrightText: 2024 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: AGPL-3.0-only

package com.zextras.mailbox.tracking;

/**
 * Tracks events/actions performed by the user to use as metrics/insights on Carbonio usages.
 * Tracking of data is anonymous and not linkable to user information/personal data.
 *
 */
public interface Tracking {

  void sendEventIgnoringFailure(Event event);

}
