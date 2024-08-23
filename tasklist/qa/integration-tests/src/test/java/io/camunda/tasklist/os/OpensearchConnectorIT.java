/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH under
 * one or more contributor license agreements. See the NOTICE file distributed
 * with this work for additional information regarding copyright ownership.
 * Licensed under the Camunda License 1.0. You may not use this file
 * except in compliance with the Camunda License 1.0.
 */
package io.camunda.tasklist.os;

import static org.junit.jupiter.api.Assumptions.assumeTrue;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.CountMatchingStrategy;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import io.camunda.search.connect.plugin.PluginConfiguration;
import io.camunda.tasklist.os.OpensearchConnectorIT.TestContext;
import io.camunda.tasklist.property.TasklistProperties;
import io.camunda.tasklist.qa.util.TestUtil;
import io.camunda.tasklist.util.TasklistIntegrationTest;
import io.camunda.tasklist.util.TestPlugin;
import io.camunda.zeebe.util.FileUtil;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import net.bytebuddy.ByteBuddy;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.opensearch.client.Request;
import org.opensearch.client.RestClient;
import org.opensearch.client.opensearch.cluster.HealthRequest;
import org.opensearch.testcontainers.OpensearchContainer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@Testcontainers
@SpringBootTest(
    classes = {OpenSearchConnector.class, TasklistProperties.class, TestContext.class},
    properties = TasklistProperties.PREFIX + ".database = opensearch")
@EnableConfigurationProperties(TasklistProperties.class)
final class OpensearchConnectorIT extends TasklistIntegrationTest {
  @Container
  static OpensearchContainer<?> opensearch =
      new OpensearchContainer<>(
              DockerImageName.parse("opensearchproject/opensearch")
                  .withTag(RestClient.class.getPackage().getImplementationVersion()))
          .withEnv("OPENSEARCH_PASSWORD", "changeme")
          .withEnv("plugins.security.allow_unsafe_democertificates", "true");

  // We can't use field injections from the WireMock or TempDir extensions, as those would run after
  // the DynamicPropertySource method used by SpringBootTest; so we need to manually manage their
  // lifecycle here instead
  private static final WireMockServer wireMockServer =
      new WireMockServer(WireMockConfiguration.options().dynamicPort());
  private static final Path tempDir = createTempDir();

  @Autowired private OpenSearchConnector connector;

  @BeforeAll
  static void beforeAll() {
    assumeTrue(TestUtil.isOpenSearch());
    wireMockServer.start();
  }

  @AfterAll
  static void afterAll() throws IOException {
    FileUtil.deleteFolderIfExists(tempDir);
    wireMockServer.stop();
  }

  @Test
  void shouldSetCustomHeaderOnAllOsClientRequests() throws IOException {
    // given
    final var client = connector.tasklistOsClient();

    // when
    client.cluster().health();

    // then
    wireMockServer.verify(
        new CountMatchingStrategy(CountMatchingStrategy.GREATER_THAN, 0),
        WireMock.anyRequestedFor(WireMock.anyUrl()).withHeader("foo", WireMock.equalTo("bar")));
  }

  @Test
  void shouldSetCustomHeaderOnAllOsAsyncClientRequests() throws IOException {
    // given
    final var client = connector.tasklistOsAsyncClient();

    // when
    client.cluster().health().join();

    // then
    wireMockServer.verify(
        new CountMatchingStrategy(CountMatchingStrategy.GREATER_THAN, 0),
        WireMock.anyRequestedFor(WireMock.anyUrl()).withHeader("foo", WireMock.equalTo("bar")));
  }

  @Test
  void shouldSetCustomHeaderOnAllZeebeOsClientRequests() throws IOException {
    // given
    final var client = connector.tasklistZeebeOsClient();

    // when
    client.cluster().health();

    // then
    wireMockServer.verify(
        new CountMatchingStrategy(CountMatchingStrategy.GREATER_THAN, 0),
        WireMock.anyRequestedFor(WireMock.anyUrl()).withHeader("foo", WireMock.equalTo("bar")));
  }

  @Test
  void shouldSetCustomHeaderOnAllOsRestClientRequests() throws IOException {
    // given
    final var client = connector.tasklistOsRestClient();
    final var request = new HealthRequest.Builder().build();

    // when
    client.performRequest(
        new Request(
            HealthRequest._ENDPOINT.method(request), HealthRequest._ENDPOINT.requestUrl(request)));

    // then
    wireMockServer.verify(
        new CountMatchingStrategy(CountMatchingStrategy.GREATER_THAN, 0),
        WireMock.anyRequestedFor(WireMock.anyUrl()).withHeader("foo", WireMock.equalTo("bar")));
  }

  @DynamicPropertySource
  public static void setSearchPluginProperties(final DynamicPropertyRegistry registry)
      throws IOException {
    // we need to use a temporary directory here unfortunately, and not junit's TempDir, because
    // this is called very early in the lifecycle due to the SpringBootTest annotation; not as
    // robust, but good enough
    final var jar =
        new ByteBuddy()
            .subclass(TestPlugin.class)
            .name("com.acme.Foo")
            .make()
            .toJar(tempDir.resolve("plugin.jar").toFile())
            .toPath();
    final var plugin = new PluginConfiguration("test", "com.acme.Foo", jar);

    // need to start server here since this is called before any other extensions
    wireMockServer.start();
    wireMockServer.stubFor(
        WireMock.any(WireMock.anyUrl())
            .willReturn(WireMock.aResponse().proxiedFrom(opensearch.getHttpHostAddress())));

    setPluginConfig(registry, TasklistProperties.PREFIX + ".opensearch", plugin);
    setPluginConfig(registry, TasklistProperties.PREFIX + ".zeebe.opensearch", plugin);
    registry.add(TasklistProperties.PREFIX + ".opensearch.url", wireMockServer::baseUrl);
    registry.add(TasklistProperties.PREFIX + ".zeebeOpensearch.url", wireMockServer::baseUrl);
  }

  private static void setPluginConfig(
      final DynamicPropertyRegistry registry,
      final String prefix,
      final PluginConfiguration plugin) {
    registry.add(prefix + ".plugins[0].id", plugin::id);
    registry.add(prefix + ".plugins[0].className", plugin::className);
    registry.add(prefix + ".plugins[0].jarPath", plugin::jarPath);
  }

  private static Path createTempDir() {
    try {
      return Files.createTempDirectory("plugin");
    } catch (final IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  public static final class TestContext {
    @Bean
    @Qualifier("tasklistObjectMapper")
    public ObjectMapper objectMapper() {
      return new ObjectMapper();
    }
  }
}
