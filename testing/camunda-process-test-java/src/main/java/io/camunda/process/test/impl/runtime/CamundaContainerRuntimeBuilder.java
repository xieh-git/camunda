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

import io.camunda.process.test.impl.containers.ContainerFactory;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CamundaContainerRuntimeBuilder {

  private ContainerFactory containerFactory = new ContainerFactory();

  private String camundaDockerImageName = ContainerRuntimeDefaults.CAMUNDA_DOCKER_IMAGE_NAME;
  private String camundaDockerImageVersion = ContainerRuntimeDefaults.CAMUNDA_DOCKER_IMAGE_VERSION;

  private String elasticsearchDockerImageName =
      ContainerRuntimeDefaults.ELASTICSEARCH_DOCKER_IMAGE_NAME;
  private String elasticsearchDockerImageVersion =
      ContainerRuntimeDefaults.ELASTICSEARCH_DOCKER_IMAGE_VERSION;

  private final Map<String, String> camundaEnvVars = new HashMap<>();
  private final Map<String, String> elasticsearchEnvVars = new HashMap<>();

  private final List<Integer> camundaExposedPorts = new ArrayList<>();
  private final List<Integer> elasticsearchExposedPorts = new ArrayList<>();

  private String camundaLoggerName = ContainerRuntimeDefaults.CAMUNDA_LOGGER_NAME;
  private String elasticsearchLoggerName = ContainerRuntimeDefaults.ELASTICSEARCH_LOGGER_NAME;

  // ============ For testing =================

  CamundaContainerRuntimeBuilder withContainerFactory(final ContainerFactory containerFactory) {
    this.containerFactory = containerFactory;
    return this;
  }

  // ============ Configuration options =================

  public CamundaContainerRuntimeBuilder withCamundaDockerImageName(final String dockerImageName) {
    camundaDockerImageName = dockerImageName;
    return this;
  }

  public CamundaContainerRuntimeBuilder withCamundaDockerImageVersion(
      final String dockerImageVersion) {
    camundaDockerImageVersion = dockerImageVersion;
    return this;
  }

  public CamundaContainerRuntimeBuilder withElasticsearchDockerImageName(
      final String dockerImageName) {
    elasticsearchDockerImageName = dockerImageName;
    return this;
  }

  public CamundaContainerRuntimeBuilder withElasticsearchDockerImageVersion(
      final String dockerImageVersion) {
    elasticsearchDockerImageVersion = dockerImageVersion;
    return this;
  }

  public CamundaContainerRuntimeBuilder withCamundaEnv(final Map<String, String> envVars) {
    camundaEnvVars.putAll(envVars);
    return this;
  }

  public CamundaContainerRuntimeBuilder withCamundaEnv(final String name, final String value) {
    camundaEnvVars.put(name, value);
    return this;
  }

  public CamundaContainerRuntimeBuilder withElasticsearchEnv(final Map<String, String> envVars) {
    elasticsearchEnvVars.putAll(envVars);
    return this;
  }

  public CamundaContainerRuntimeBuilder withElasticsearchEnv(
      final String name, final String value) {
    elasticsearchEnvVars.put(name, value);
    return this;
  }

  public CamundaContainerRuntimeBuilder withCamundaExposedPort(final int port) {
    camundaExposedPorts.add(port);
    return this;
  }

  public CamundaContainerRuntimeBuilder withElasticsearchExposedPort(final int port) {
    elasticsearchExposedPorts.add(port);
    return this;
  }

  public CamundaContainerRuntimeBuilder withCamundaLogger(final String loggerName) {
    camundaLoggerName = loggerName;
    return this;
  }

  public CamundaContainerRuntimeBuilder withElasticsearchLogger(final String loggerName) {
    elasticsearchLoggerName = loggerName;
    return this;
  }

  // ============ Build =================

  public CamundaContainerRuntime build() {
    return new CamundaContainerRuntime(this, containerFactory);
  }

  // ============ Getters =================

  public String getCamundaDockerImageName() {
    return camundaDockerImageName;
  }

  public String getCamundaDockerImageVersion() {
    return camundaDockerImageVersion;
  }

  public String getElasticsearchDockerImageName() {
    return elasticsearchDockerImageName;
  }

  public String getElasticsearchDockerImageVersion() {
    return elasticsearchDockerImageVersion;
  }

  public Map<String, String> getCamundaEnvVars() {
    return camundaEnvVars;
  }

  public Map<String, String> getElasticsearchEnvVars() {
    return elasticsearchEnvVars;
  }

  public List<Integer> getCamundaExposedPorts() {
    return camundaExposedPorts;
  }

  public List<Integer> getElasticsearchExposedPorts() {
    return elasticsearchExposedPorts;
  }

  public String getCamundaLoggerName() {
    return camundaLoggerName;
  }

  public String getElasticsearchLoggerName() {
    return elasticsearchLoggerName;
  }
}
