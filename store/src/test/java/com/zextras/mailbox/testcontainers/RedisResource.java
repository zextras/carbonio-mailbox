// SPDX-FileCopyrightText: 2026 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: AGPL-3.0-only

package com.zextras.mailbox.testcontainers;

/**
 * A running Redis instance, backed either by a Docker container (testcontainers) or a Kubernetes
 * pod. Tests only need the connection details, so they are agnostic to the backend.
 */
public interface RedisResource {

  String getHost();

  int getPort();

  void close();
}
