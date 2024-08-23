/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH under
 * one or more contributor license agreements. See the NOTICE file distributed
 * with this work for additional information regarding copyright ownership.
 * Licensed under the Camunda License 1.0. You may not use this file
 * except in compliance with the Camunda License 1.0.
 */
package io.camunda.search.connect.plugin;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import io.camunda.plugin.search.header.CustomHeader;
import io.camunda.plugin.search.header.DatabaseCustomHeaderSupplier;
import io.camunda.zeebe.test.util.junit.AutoCloseResources;
import io.camunda.zeebe.test.util.junit.AutoCloseResources.AutoCloseResource;
import io.camunda.zeebe.util.jar.ExternalJarClassLoader;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import net.bytebuddy.ByteBuddy;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.impl.BasicEntityDetails;
import org.apache.hc.core5.http.message.BasicHttpRequest;
import org.apache.hc.core5.http.protocol.BasicHttpContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

@AutoCloseResources
final class PluginRepositoryTest {
  @AutoCloseResource private final PluginRepository repository = new PluginRepository();

  @Test
  void shouldBeEmpty() {
    // given - when
    final var isEmpty = repository.isEmpty();

    // then
    assertThat(isEmpty).isTrue();
  }

  @Test
  void shouldNotBeEmpty() {
    // given
    final var config = new PluginConfiguration("test", TestPlugin.class.getName(), null);

    // when
    repository.load(List.of(config));
    final var isEmpty = repository.isEmpty();

    // then
    assertThat(isEmpty).isFalse();
  }

  @Test
  void shouldNotFailWhenLoadingNothing() {
    // given
    final var configs = new ArrayList<PluginConfiguration>();

    // when
    final var result = repository.load(configs);

    // then
    assertThat(result).isSameAs(repository);
    assertThat(result.isEmpty()).isTrue();
  }

  @Test
  void shouldNotFailWhenLoadingNull() {
    // when
    final var result = repository.load(null);

    // then
    assertThat(result).isSameAs(repository);
    assertThat(result.isEmpty()).isTrue();
  }

  @Test
  void shouldFailOnIncorrectClass() {
    // given
    final var config = new PluginConfiguration("test", String.class.getName(), null);

    // when - then
    assertThatCode(() -> repository.load(List.of(config))).isInstanceOf(PluginLoadException.class);
  }

  @Test
  void shouldFailOnNonExistentClass() {
    // given
    final var config = new PluginConfiguration("test", "foo", null);

    // when - then
    assertThatCode(() -> repository.load(List.of(config))).isInstanceOf(PluginLoadException.class);
  }

  @Test
  void shouldLoadFromExternalJar(final @TempDir File tmpDir) throws IOException {
    // given
    final var className = "com.acme.Foo";
    final var unloadedClass = new ByteBuddy().subclass(TestPlugin.class).name(className).make();
    final var jar = unloadedClass.toJar(new File(tmpDir, "plugin.jar"));
    final var config = new PluginConfiguration("test", className, jar.toPath());
    repository.load(List.of(config));

    // when
    final var plugins = repository.instantiate().toList();
    final var plugin = plugins.getFirst();
    final var header = plugin.getCustomHttpHeader();

    // then
    assertThat(plugins).hasSize(1);
    assertThat(header.key()).isEqualTo("foo");
    assertThat(header.value()).isEqualTo("bar");
  }

  @Test
  void shouldReturnInterceptor() throws HttpException, IOException {
    // given
    final var request = new BasicHttpRequest("GET", "path");
    final var context = new BasicHttpContext();
    final var config = new PluginConfiguration("test", TestPlugin.class.getName(), null);
    repository.load(List.of(config));

    // when
    final var interceptor = repository.asRequestInterceptor();
    interceptor.process(request, new BasicEntityDetails(0, ContentType.APPLICATION_JSON), context);

    // then
    assertThat(request.getHeader("foo").getValue()).isEqualTo("bar");
  }

  @Test
  void shouldApplyPluginInOrderOfDefinition() throws HttpException, IOException {
    // given
    final var request = new BasicHttpRequest("GET", "path");
    final var context = new BasicHttpContext();
    repository.load(
        List.of(
            new PluginConfiguration("test", TestPlugin.class.getName(), null),
            new PluginConfiguration("other", OtherPlugin.class.getName(), null)));

    // when
    final var interceptor = repository.asRequestInterceptor();
    interceptor.process(request, new BasicEntityDetails(0, ContentType.APPLICATION_JSON), context);

    // then - the last plugin will overwrite the value of the first
    assertThat(request.getHeader("foo").getValue()).isEqualTo("baz");
  }

  @Test
  void shouldInstrumentExternallyLoadedPlugin(final @TempDir File tmpDir)
      throws IOException, NoSuchFieldException, IllegalAccessException {
    // given
    final var className = "com.acme.Foo";
    final var unloadedClass = new ByteBuddy().subclass(TestPlugin.class).name(className).make();
    final var jar = unloadedClass.toJar(new File(tmpDir, "plugin.jar"));
    final var config = new PluginConfiguration("test", className, jar.toPath());
    repository.load(List.of(config));

    // when
    final var plugins = repository.instantiate().toList();
    final var externalClass = repository.getPlugins().get("test");
    final var classLoader = externalClass.getClassLoader();
    // ignore return value as we just want to trigger the code path
    plugins.getFirst().getCustomHttpHeader();

    // then - fetch the value of the threadContextLoader from the external class to get the value
    // this instance of the class (on the external class loader); we can't fetch it directly from
    // from our system class loader as they are two different class instances
    assertThat(classLoader).isInstanceOf(ExternalJarClassLoader.class);
    assertThat(externalClass.getField("threadContextLoader").get(null)).isSameAs(classLoader);
  }

  // Must be public for us to load as an external plugin
  public static class TestPlugin implements DatabaseCustomHeaderSupplier {
    public static ClassLoader threadContextLoader;

    @Override
    public CustomHeader getCustomHttpHeader() {
      threadContextLoader = Thread.currentThread().getContextClassLoader();
      return new CustomHeader("foo", "bar");
    }
  }

  // Must be public for us to load as an external plugin
  public static class OtherPlugin implements DatabaseCustomHeaderSupplier {
    @Override
    public CustomHeader getCustomHttpHeader() {
      return new CustomHeader("foo", "baz");
    }
  }
}
