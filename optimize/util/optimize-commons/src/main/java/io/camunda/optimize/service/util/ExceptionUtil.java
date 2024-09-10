/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH under
 * one or more contributor license agreements. See the NOTICE file distributed
 * with this work for additional information regarding copyright ownership.
 * Licensed under the Camunda License 1.0. You may not use this file
 * except in compliance with the Camunda License 1.0.
 */
package io.camunda.optimize.service.util;

import static io.camunda.optimize.service.db.DatabaseConstants.DECISION_INSTANCE_INDEX_PREFIX;
import static io.camunda.optimize.service.db.DatabaseConstants.DECISION_INSTANCE_MULTI_ALIAS;
import static io.camunda.optimize.service.db.DatabaseConstants.INDEX_NOT_FOUND_EXCEPTION_TYPE;
import static io.camunda.optimize.service.db.DatabaseConstants.PROCESS_INSTANCE_INDEX_PREFIX;
import static io.camunda.optimize.service.db.DatabaseConstants.PROCESS_INSTANCE_MULTI_ALIAS;
import static io.camunda.optimize.service.db.DatabaseConstants.TOO_MANY_BUCKETS_EXCEPTION_TYPE;

import io.camunda.optimize.dto.optimize.DefinitionType;
import io.camunda.optimize.service.exceptions.OptimizeRuntimeException;
import java.util.Arrays;
import java.util.function.Function;
import org.elasticsearch.ElasticsearchStatusException;
import org.opensearch.client.opensearch._types.OpenSearchException;

public class ExceptionUtil {
  public static boolean isTooManyBucketsException(final RuntimeException e) {
    return isDbExceptionWithMessage(e, msg -> msg.contains(TOO_MANY_BUCKETS_EXCEPTION_TYPE));
  }

  public static boolean isInstanceIndexNotFoundException(final RuntimeException e) {
    return isDbExceptionWithMessage(e, msg -> msg.contains(INDEX_NOT_FOUND_EXCEPTION_TYPE));
  }

  public static boolean isInstanceIndexNotFoundException(
      final DefinitionType type, final RuntimeException e) {
    return isDbExceptionWithMessage(
        e,
        msg ->
            msg.contains(INDEX_NOT_FOUND_EXCEPTION_TYPE)
                && containsInstanceIndexAliasOrPrefix(type, e.getMessage()));
  }

  private static boolean isDbExceptionWithMessage(
      final RuntimeException e, Function<String, Boolean> messageFilter) {
    if (e instanceof ElasticsearchStatusException) {
      return Arrays.stream(e.getSuppressed())
          .map(Throwable::getMessage)
          .anyMatch(msg -> messageFilter.apply(msg));
    } else if (e instanceof OpenSearchException) {
      return messageFilter.apply(e.getMessage());
    } else {
      return false;
    }
  }

  private static boolean containsInstanceIndexAliasOrPrefix(
      final DefinitionType type, final String message) {
    switch (type) {
      case PROCESS:
        return message.contains(PROCESS_INSTANCE_INDEX_PREFIX)
            || message.contains(PROCESS_INSTANCE_MULTI_ALIAS);
      case DECISION:
        return message.contains(DECISION_INSTANCE_INDEX_PREFIX)
            || message.contains(DECISION_INSTANCE_MULTI_ALIAS);
      default:
        throw new OptimizeRuntimeException("Unsupported definition type:" + type);
    }
  }
}
