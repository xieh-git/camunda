/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH under
 * one or more contributor license agreements. See the NOTICE file distributed
 * with this work for additional information regarding copyright ownership.
 * Licensed under the Camunda License 1.0. You may not use this file
 * except in compliance with the Camunda License 1.0.
 */
package io.camunda.search.connect.plugin;

import io.camunda.plugin.search.header.DatabaseCustomHeaderSupplier;
import java.util.SequencedCollection;
import org.apache.hc.core5.http.EntityDetails;
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.protocol.HttpContext;

final class PluginRepositoryInterceptor implements CompatHttpRequestInterceptor {
  private final SequencedCollection<DatabaseCustomHeaderSupplier> plugins;

  PluginRepositoryInterceptor(final SequencedCollection<DatabaseCustomHeaderSupplier> plugins) {
    this.plugins = plugins;
  }

  static PluginRepositoryInterceptor ofRepository(final PluginRepository repository) {
    final var interceptors = repository.instantiate().toList();
    return new PluginRepositoryInterceptor(interceptors);
  }

  @Override
  public void process(
      final HttpRequest request, final EntityDetails entity, final HttpContext context) {
    for (final var plugin : plugins) {
      setHeader(plugin, request::setHeader);
    }
  }

  @Override
  public void process(
      final org.apache.http.HttpRequest request,
      final org.apache.http.protocol.HttpContext context) {
    for (final var plugin : plugins) {
      setHeader(plugin, request::setHeader);
    }
  }

  private void setHeader(
      final DatabaseCustomHeaderSupplier plugin, final HeaderConsumer headerConsumer) {
    final var header = plugin.getCustomHttpHeader();
    headerConsumer.accept(header.key(), header.value());
  }

  private interface HeaderConsumer {
    void accept(final String key, String value);
  }
}
