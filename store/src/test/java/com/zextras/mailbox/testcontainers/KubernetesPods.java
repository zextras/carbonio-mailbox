// SPDX-FileCopyrightText: 2026 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: AGPL-3.0-only

package com.zextras.mailbox.testcontainers;

import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.Configuration;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.V1Container;
import io.kubernetes.client.openapi.models.V1ContainerPort;
import io.kubernetes.client.openapi.models.V1EnvVar;
import io.kubernetes.client.openapi.models.V1LocalObjectReference;
import io.kubernetes.client.openapi.models.V1ObjectMeta;
import io.kubernetes.client.openapi.models.V1Pod;
import io.kubernetes.client.openapi.models.V1PodSpec;
import io.kubernetes.client.openapi.models.V1PodStatus;
import io.kubernetes.client.util.ClientBuilder;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/**
 * Launches short-lived pods for integration tests running inside a k3s/crun cluster.
 *
 * <p>This is the CI counterpart to testcontainers: where testcontainers needs a Docker daemon
 * (absent under crun), this creates a real pod through the in-cluster Kubernetes API and waits
 * until its ports accept connections. Since the test JVM and the launched pod share the cluster
 * network there is no port mapping — callers connect straight to the pod IP on the container port.
 *
 * <p>The pod is scheduled with the {@code crun} RuntimeClass onto a worker node, matching the CI
 * cluster layout. Override the RuntimeClass with {@code K8S_RUNTIME_CLASS} if your cluster differs.
 */
public final class KubernetesPods {

  private static final Logger LOG = Logger.getLogger(KubernetesPods.class.getName());

  private static final String DEFAULT_RUNTIME_CLASS = "crun";
  private static final Duration RUNNING_TIMEOUT = Duration.ofMinutes(5);
  private static final Duration PORT_TIMEOUT = Duration.ofMinutes(5);
  private static final long POLL_INTERVAL_MS = 2_000;

  private KubernetesPods() {}

  public static LaunchedPod start(
      String namePrefix,
      String image,
      List<Integer> ports,
      Map<String, String> env,
      List<String> args) {
    CoreV1Api api = newApi();
    String namespace = readNamespace();
    String podName = namePrefix + "-" + System.nanoTime();

    try {
      api.createNamespacedPod(namespace, buildPod(podName, namespace, image, ports, env, null, args))
          .execute();
    } catch (ApiException e) {
      throw new IllegalStateException("Failed to create pod " + podName + ": " + e.getResponseBody(), e);
    }
    LOG.info("Created pod " + podName + " in namespace " + namespace);

    String podIP = waitForRunning(api, namespace, podName);
    LOG.info("Pod " + podName + " running at IP " + podIP);

    for (int port : ports) {
      waitForPort(api, namespace, podName, podIP, port);
    }
    LOG.info("Pod " + podName + " is ready");

    return new LaunchedPod(api, namespace, podName, podIP);
  }

  public static LaunchedPod startWithLogReadiness(
      String namePrefix,
      String image,
      List<Integer> ports,
      Map<String, String> env,
      List<String> command,
      List<String> args,
      String readyLogRegex,
      Duration readyTimeout) {
    CoreV1Api api = newApi();
    String namespace = readNamespace();
    String podName = namePrefix + "-" + System.nanoTime();

    try {
      api.createNamespacedPod(
              namespace, buildPod(podName, namespace, image, ports, env, command, args))
          .execute();
    } catch (ApiException e) {
      throw new IllegalStateException("Failed to create pod " + podName + ": " + e.getResponseBody(), e);
    }
    LOG.info("Created pod " + podName + " in namespace " + namespace);

    String podIP = waitForRunning(api, namespace, podName);
    LOG.info("Pod " + podName + " running at IP " + podIP);

    LaunchedPod pod = new LaunchedPod(api, namespace, podName, podIP);
    pod.waitForLog(readyLogRegex, readyTimeout);
    LOG.info("Pod " + podName + " is ready");
    return pod;
  }

  private static CoreV1Api newApi() {
    try {
      ApiClient client = ClientBuilder.cluster().build();
      Configuration.setDefaultApiClient(client);
      return new CoreV1Api();
    } catch (IOException e) {
      throw new IllegalStateException("Failed to build in-cluster Kubernetes client", e);
    }
  }

  private static V1Pod buildPod(
      String podName,
      String namespace,
      String image,
      List<Integer> ports,
      Map<String, String> env,
      List<String> command,
      List<String> args) {
    List<V1ContainerPort> containerPorts = new ArrayList<>();
    for (int port : ports) {
      containerPorts.add(new V1ContainerPort().containerPort(port));
    }
    List<V1EnvVar> envVars = new ArrayList<>();
    env.forEach((k, v) -> envVars.add(new V1EnvVar().name(k).value(v)));

    V1Container container =
        new V1Container().name("main").image(image).ports(containerPorts).env(envVars);
    if (command != null && !command.isEmpty()) {
      container.command(command);
    }
    if (args != null && !args.isEmpty()) {
      container.args(args);
    }

    V1PodSpec spec =
        new V1PodSpec()
            .containers(List.of(container))
            .restartPolicy("Never")
            .runtimeClassName(runtimeClass())
            .nodeSelector(Map.of("node-role.kubernetes.io/worker", "true"));
    // client-java initializes overhead to an empty HashMap, which the admission webhook rejects
    // when the crun RuntimeClass defines no overhead. Null it so the field is omitted entirely.
    spec.setOverhead(null);

    String pullSecret = System.getenv("K8S_IMAGE_PULL_SECRET");
    if (pullSecret != null && !pullSecret.isBlank()) {
      spec.imagePullSecrets(List.of(new V1LocalObjectReference().name(pullSecret)));
    }

    return new V1Pod()
        .apiVersion("v1")
        .kind("Pod")
        .metadata(
            new V1ObjectMeta()
                .name(podName)
                .namespace(namespace)
                .labels(Map.of("app", podName, "managed-by", "carbonio-mailbox-tests")))
        .spec(spec);
  }

  private static String runtimeClass() {
    String override = System.getenv("K8S_RUNTIME_CLASS");
    return (override != null && !override.isBlank()) ? override : DEFAULT_RUNTIME_CLASS;
  }

  private static String waitForRunning(CoreV1Api api, String namespace, String podName) {
    Instant deadline = Instant.now().plus(RUNNING_TIMEOUT);
    while (Instant.now().isBefore(deadline)) {
      try {
        V1PodStatus status = api.readNamespacedPod(podName, namespace).execute().getStatus();
        String phase = status != null ? status.getPhase() : null;
        if ("Running".equals(phase) && status.getPodIP() != null && !status.getPodIP().isBlank()) {
          return status.getPodIP();
        }
        if ("Failed".equals(phase) || "Succeeded".equals(phase)) {
          deleteSilently(api, namespace, podName);
          throw new IllegalStateException("Pod " + podName + " entered terminal phase: " + phase);
        }
        LOG.info("Waiting for pod " + podName + " (phase: " + phase + ")");
        sleep();
      } catch (ApiException e) {
        throw new IllegalStateException("Error reading pod " + podName + ": " + e.getResponseBody(), e);
      }
    }
    deleteSilently(api, namespace, podName);
    throw new IllegalStateException("Timeout waiting for pod " + podName + " to be Running");
  }

  private static void waitForPort(
      CoreV1Api api, String namespace, String podName, String podIP, int port) {
    Instant deadline = Instant.now().plus(PORT_TIMEOUT);
    while (Instant.now().isBefore(deadline)) {
      try (Socket socket = new Socket()) {
        socket.connect(new InetSocketAddress(podIP, port), 2_000);
        return;
      } catch (IOException notReady) {
        sleep();
      }
    }
    deleteSilently(api, namespace, podName);
    throw new IllegalStateException("Timeout waiting for port " + port + " on pod " + podName);
  }

  private static void deleteSilently(CoreV1Api api, String namespace, String podName) {
    try {
      api.deleteNamespacedPod(podName, namespace).execute();
    } catch (ApiException ignored) {
      // best-effort cleanup
    }
  }

  private static String readNamespace() {
    try {
      return Files.readString(Paths.get("/var/run/secrets/kubernetes.io/serviceaccount/namespace"))
          .strip();
    } catch (IOException e) {
      throw new IllegalStateException(
          "Failed to read namespace from serviceaccount — is this running inside a pod?", e);
    }
  }

  private static void sleep() {
    try {
      Thread.sleep(POLL_INTERVAL_MS);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new IllegalStateException("Interrupted while waiting for pod", e);
    }
  }

  /** Handle to a running pod: exposes its cluster IP and deletes it on {@link #delete()}. */
  public static final class LaunchedPod {

    private final CoreV1Api api;
    private final String namespace;
    private final String podName;
    private final String podIP;

    private LaunchedPod(CoreV1Api api, String namespace, String podName, String podIP) {
      this.api = api;
      this.namespace = namespace;
      this.podName = podName;
      this.podIP = podIP;
    }

    public String ip() {
      return podIP;
    }

    public void waitForLog(String regex, Duration timeout) {
      Pattern pattern = Pattern.compile(regex, Pattern.DOTALL);
      Instant deadline = Instant.now().plus(timeout);
      while (Instant.now().isBefore(deadline)) {
        try {
          String log = api.readNamespacedPodLog(podName, namespace).container("main").execute();
          if (log != null && pattern.matcher(log).find()) {
            return;
          }
        } catch (ApiException notReady) {
          // log endpoint not ready yet
        }
        sleep();
      }
      deleteSilently(api, namespace, podName);
      throw new IllegalStateException("Timeout waiting for log /" + regex + "/ on pod " + podName);
    }

    public void delete() {
      try {
        api.deleteNamespacedPod(podName, namespace).execute();
        LOG.info("Deleted pod " + podName);
      } catch (ApiException e) {
        LOG.warning("Failed to delete pod " + podName + ": " + e.getResponseBody());
      }
    }
  }
}
