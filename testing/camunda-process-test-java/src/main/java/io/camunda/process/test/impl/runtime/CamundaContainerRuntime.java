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
package io.camunda.process.test.impl.runtime;

import io.camunda.process.test.impl.containers.CamundaContainer;
import io.camunda.process.test.impl.containers.ContainerFactory;
import java.time.Duration;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.elasticsearch.ElasticsearchContainer;

public class CamundaContainerRuntime implements AutoCloseable {

  private static final Logger LOGGER = LoggerFactory.getLogger(CamundaContainerRuntime.class);

  private static final String NETWORK_ALIAS_ELASTICSEARCH = "elasticsearch";
  private static final String NETWORK_ALIAS_CAMUNDA = "camunda";

  private static final String ELASTICSEARCH_URL =
      "http://" + NETWORK_ALIAS_ELASTICSEARCH + ":" + ContainerRuntimePorts.ELASTICSEARCH_REST_API;

  private final ContainerFactory containerFactory;

  private final Network network;
  private final ElasticsearchContainer elasticsearchContainer;
  private final CamundaContainer camundaContainer;

  CamundaContainerRuntime(
      final CamundaContainerRuntimeBuilder builder, final ContainerFactory containerFactory) {
    this.containerFactory = containerFactory;
    network = Network.newNetwork();

    elasticsearchContainer = createElasticsearchContainer(network, builder);
    camundaContainer = createCamundaContainer(network, builder);
  }

  private ElasticsearchContainer createElasticsearchContainer(
      final Network network, final CamundaContainerRuntimeBuilder builder) {
    final ElasticsearchContainer container =
        containerFactory
            .createElasticsearchContainer(
                builder.getElasticsearchDockerImageName(),
                builder.getElasticsearchDockerImageVersion())
            .withLogConsumer(createContainerLogger(builder.getElasticsearchLoggerName()))
            .withNetwork(network)
            .withNetworkAliases(NETWORK_ALIAS_ELASTICSEARCH)
            .withEnv(ContainerRuntimeEnvs.ELASTICSEARCH_ENV_XPACK_SECURITY_ENABLED, "false")
            .withEnv(builder.getElasticsearchEnvVars());

    builder.getElasticsearchExposedPorts().forEach(container::addExposedPort);

    return container;
  }

  private CamundaContainer createCamundaContainer(
      final Network network, final CamundaContainerRuntimeBuilder builder) {
    final CamundaContainer container =
        containerFactory
            .createCamundaContainer(
                builder.getCamundaDockerImageName(), builder.getCamundaDockerImageVersion())
            .withLogConsumer(createContainerLogger(builder.getCamundaLoggerName()))
            .withNetwork(network)
            .withNetworkAliases(NETWORK_ALIAS_CAMUNDA)
            .withZeebeApi(NETWORK_ALIAS_CAMUNDA)
            .withElasticsearchUrl(ELASTICSEARCH_URL)
            .withEnv(builder.getCamundaEnvVars());

    builder.getCamundaExposedPorts().forEach(container::addExposedPort);

    return container;
  }

  public void start() {
    LOGGER.info("Starting Camunda container runtime");
    final Instant startTime = Instant.now();

    elasticsearchContainer.start();
    camundaContainer.start();

    final Instant endTime = Instant.now();
    final Duration startupTime = Duration.between(startTime, endTime);
    LOGGER.info("Camunda container runtime started in {}", startupTime);
  }

  public CamundaContainer getCamundaContainer() {
    return camundaContainer;
  }

  public ElasticsearchContainer getElasticsearchContainer() {
    return elasticsearchContainer;
  }

  @Override
  public void close() throws Exception {
    LOGGER.info("Stopping Camunda container runtime");
    final Instant startTime = Instant.now();

    camundaContainer.stop();
    elasticsearchContainer.stop();
    network.close();

    final Instant endTime = Instant.now();
    final Duration shutdownTime = Duration.between(startTime, endTime);
    LOGGER.info("Camunda container runtime stopped in {}", shutdownTime);
  }

  private static Slf4jLogConsumer createContainerLogger(final String name) {
    final Logger logger = LoggerFactory.getLogger(name);
    return new Slf4jLogConsumer(logger);
  }

  public static CamundaContainerRuntimeBuilder newBuilder() {
    return new CamundaContainerRuntimeBuilder();
  }

  public static CamundaContainerRuntime newDefaultRuntime() {
    return newBuilder().build();
  }
}
