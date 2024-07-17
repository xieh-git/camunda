/*
 * Copyright © 2017 camunda services GmbH (info@camunda.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.camunda.client;

import io.camunda.client.CamundaClientCloudBuilderStep1.CamundaClientCloudBuilderStep2;
import io.camunda.client.CamundaClientCloudBuilderStep1.CamundaClientCloudBuilderStep2.CamundaClientCloudBuilderStep3;
import io.camunda.client.CamundaClientCloudBuilderStep1.CamundaClientCloudBuilderStep2.CamundaClientCloudBuilderStep3.CamundaClientCloudBuilderStep4;
import java.net.URI;
import java.time.Duration;
import java.util.List;

public final class ClientProperties {

  /**
   * @see CamundaClientBuilder#applyEnvironmentVariableOverrides(boolean)
   */
  public static final String APPLY_ENVIRONMENT_VARIABLES_OVERRIDES =
      "client.zeebe.applyEnvironmentVariableOverrides";

  /**
   * @deprecated since 8.5 for removal with 8.8, replaced by {@link ClientProperties#GRPC_ADDRESS}
   * @see CamundaClientBuilder#gatewayAddress(String)
   */
  @Deprecated public static final String GATEWAY_ADDRESS = "client.zeebe.gateway.address";

  /**
   * @deprecated since 8.5 for removal with 8.8, where toggling between both will not be possible
   * @see CamundaClientBuilder#preferRestOverGrpc(boolean)
   */
  @Deprecated
  public static final String PREFER_REST_OVER_GRPC = "client.zeebe.gateway.preferRestOverGrpc";

  /**
   * @see CamundaClientBuilder#restAddress(URI)
   */
  public static final String REST_ADDRESS = "client.zeebe.gateway.rest.address";

  /**
   * @see CamundaClientBuilder#grpcAddress(URI)
   */
  public static final String GRPC_ADDRESS = "client.zeebe.gateway.grpc.address";

  /**
   * @see CamundaClientBuilder#defaultTenantId(String)
   */
  public static final String DEFAULT_TENANT_ID = "client.zeebe.tenantId";

  /**
   * @see CamundaClientBuilder#defaultJobWorkerTenantIds(List)
   */
  public static final String DEFAULT_JOB_WORKER_TENANT_IDS = "client.zeebe.worker.tenantIds";

  /**
   * @see CamundaClientBuilder#numJobWorkerExecutionThreads(int)
   */
  public static final String JOB_WORKER_EXECUTION_THREADS = "client.zeebe.worker.threads";

  /**
   * @see CamundaClientBuilder#defaultJobWorkerMaxJobsActive(int)
   */
  public static final String JOB_WORKER_MAX_JOBS_ACTIVE = "client.zeebe.worker.maxJobsActive";

  /**
   * @see CamundaClientBuilder#defaultJobWorkerName(String)
   */
  public static final String DEFAULT_JOB_WORKER_NAME = "client.zeebe.worker.name";

  /**
   * @see CamundaClientBuilder#defaultJobTimeout(Duration)
   */
  public static final String DEFAULT_JOB_TIMEOUT = "client.zeebe.job.timeout";

  /**
   * @see CamundaClientBuilder#defaultJobPollInterval(Duration)
   */
  public static final String DEFAULT_JOB_POLL_INTERVAL = "client.zeebe.job.pollinterval";

  /**
   * @see CamundaClientBuilder#defaultMessageTimeToLive(Duration)
   */
  public static final String DEFAULT_MESSAGE_TIME_TO_LIVE = "client.zeebe.message.timeToLive";

  /**
   * @see CamundaClientBuilder#defaultRequestTimeout(Duration)
   */
  public static final String DEFAULT_REQUEST_TIMEOUT = "client.zeebe.requestTimeout";

  /**
   * @see CamundaClientBuilder#usePlaintext()
   */
  public static final String USE_PLAINTEXT_CONNECTION = "client.zeebe.security.plaintext";

  /**
   * @see CamundaClientBuilder#caCertificatePath(String)
   */
  public static final String CA_CERTIFICATE_PATH = "client.zeebe.security.certpath";

  /**
   * @see CamundaClientBuilder#keepAlive(Duration)
   */
  public static final String KEEP_ALIVE = "client.zeebe.keepalive";

  /**
   * @see CamundaClientBuilder#overrideAuthority(String)
   */
  public static final String OVERRIDE_AUTHORITY = "client.zeebe.overrideauthority";

  /**
   * @see CamundaClientBuilder#maxMessageSize(int) (String)
   */
  public static final String MAX_MESSAGE_SIZE = "client.zeebe.maxMessageSize";

  /**
   * @see CamundaClientBuilder#maxMetadataSize(int)
   */
  public static final String MAX_METADATA_SIZE = "client.zeebe.maxMetadataSize";

  /**
   * @see CamundaClientCloudBuilderStep1#withClusterId(String)
   */
  public static final String CLOUD_CLUSTER_ID = "client.zeebe.cloud.clusterId";

  /**
   * @see CamundaClientCloudBuilderStep2#withClientId(String)
   */
  public static final String CLOUD_CLIENT_ID = "client.zeebe.cloud.clientId";

  /**
   * @see CamundaClientCloudBuilderStep3#withClientSecret( String)
   */
  public static final String CLOUD_CLIENT_SECRET = "client.zeebe.cloud.secret";

  /**
   * @see CamundaClientCloudBuilderStep4#withRegion(String)
   */
  public static final String CLOUD_REGION = "client.zeebe.cloud.region";

  /**
   * @see CamundaClientBuilder#defaultJobWorkerStreamEnabled(boolean)
   */
  public static final String STREAM_ENABLED = "client.zeebe.worker.stream.enabled";

  /**
   * @see CamundaClientBuilder#useDefaultRetryPolicy(boolean)
   */
  public static final String USE_DEFAULT_RETRY_POLICY = "client.zeebe.useDefaultRetryPolicy";

  private ClientProperties() {}
}
