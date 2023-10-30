// SPDX-FileCopyrightText: 2023 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: AGPL-3.0-only

package com.zextras.mailbox.client;

import com.zextras.mailbox.client.requests.Request;

public interface Client<Service> {
  <Res> Res send(Request<Service, Res> request);
}
