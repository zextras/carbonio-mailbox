// SPDX-FileCopyrightText: 2026 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: AGPL-3.0-only

package com.zextras.mailbox.testcontainers;

public final class ContainerRuntime {

  private ContainerRuntime() {}

  /**
   * Returns {@code true} when the JVM runs inside a Kubernetes pod. The kubelet always injects
   * {@code KUBERNETES_SERVICE_HOST} into in-cluster pods, so tests use it to pick a
   * Kubernetes-backed resource in CI and fall back to Docker (testcontainers) locally.
   */
  public static boolean isKubernetes() {
    return System.getenv("KUBERNETES_SERVICE_HOST") != null;
  }
}