/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH under
 * one or more contributor license agreements. See the NOTICE file distributed
 * with this work for additional information regarding copyright ownership.
 * Licensed under the Camunda License 1.0. You may not use this file
 * except in compliance with the Camunda License 1.0.
 */
package io.camunda.search.transformers.filter;

import static org.assertj.core.api.Assertions.assertThat;

import io.camunda.search.clients.query.SearchRangeQuery;
import io.camunda.search.clients.query.SearchTermQuery;
import io.camunda.search.entities.IncidentEntity.ErrorType;
import io.camunda.search.entities.IncidentEntity.IncidentState;
import io.camunda.search.filter.DateValueFilter;
import io.camunda.search.filter.FilterBuilders;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import org.junit.jupiter.api.Test;

public final class IncidentQueryTransformerTest extends AbstractTransformerTest {

  @Test
  public void shouldQueryByIncidentKey() {
    final var filter = FilterBuilders.incident(f -> f.keys(1L));

    // when
    final var searchRequest = transformQuery(filter);

    // then
    final var queryVariant = searchRequest.queryOption();
    assertThat(queryVariant)
        .isInstanceOfSatisfying(
            SearchTermQuery.class,
            t -> {
              assertThat(t.field()).isEqualTo("key");
              assertThat(t.value().longValue()).isEqualTo(1L);
            });
  }

  @Test
  public void shouldQueryByProcessDefinitionKey() {
    final var filter = FilterBuilders.incident(f -> f.processDefinitionKeys(5432L));

    // when
    final var searchRequest = transformQuery(filter);

    // then
    final var queryVariant = searchRequest.queryOption();
    assertThat(queryVariant)
        .isInstanceOfSatisfying(
            SearchTermQuery.class,
            t -> {
              assertThat(t.field()).isEqualTo("processDefinitionKey");
              assertThat(t.value().longValue()).isEqualTo(5432L);
            });
  }

  @Test
  public void shouldQueryByBpmnProcessId() {
    final var filter = FilterBuilders.incident(f -> f.bpmnProcessIds("complexProcess"));

    // when
    final var searchRequest = transformQuery(filter);

    // then
    final var queryVariant = searchRequest.queryOption();
    assertThat(queryVariant)
        .isInstanceOfSatisfying(
            SearchTermQuery.class,
            t -> {
              assertThat(t.field()).isEqualTo("bpmnProcessId");
              assertThat(t.value().stringValue()).isEqualTo("complexProcess");
            });
  }

  @Test
  public void shouldQueryByProcessInstanceKey() {
    final var filter = FilterBuilders.incident(f -> f.processInstanceKeys(42L));

    // when
    final var searchRequest = transformQuery(filter);

    // then
    final var queryVariant = searchRequest.queryOption();
    assertThat(queryVariant)
        .isInstanceOfSatisfying(
            SearchTermQuery.class,
            t -> {
              assertThat(t.field()).isEqualTo("processInstanceKey");
              assertThat(t.value().longValue()).isEqualTo(42L);
            });
  }

  @Test
  public void shouldQueryByErrorType() {
    final var filter = FilterBuilders.incident(f -> f.errorTypes(ErrorType.JOB_NO_RETRIES));

    // when
    final var searchRequest = transformQuery(filter);

    // then
    final var queryVariant = searchRequest.queryOption();
    assertThat(queryVariant)
        .isInstanceOfSatisfying(
            SearchTermQuery.class,
            t -> {
              assertThat(t.field()).isEqualTo("errorType");
              assertThat(t.value().stringValue()).isEqualTo("JOB_NO_RETRIES");
            });
  }

  @Test
  public void shouldQueryByErrorMessage() {
    final var filter = FilterBuilders.incident(f -> f.errorMessages("No retries left."));

    // when
    final var searchRequest = transformQuery(filter);

    // then
    final var queryVariant = searchRequest.queryOption();
    assertThat(queryVariant)
        .isInstanceOfSatisfying(
            SearchTermQuery.class,
            t -> {
              assertThat(t.field()).isEqualTo("errorMessage");
              assertThat(t.value().stringValue()).isEqualTo("No retries left.");
            });
  }

  @Test
  public void shouldQueryByFlowNodeId() {
    final var filter = FilterBuilders.incident(f -> f.flowNodeIds("flowNodeId-17"));

    // when
    final var searchRequest = transformQuery(filter);

    // then
    final var queryVariant = searchRequest.queryOption();
    assertThat(queryVariant)
        .isInstanceOfSatisfying(
            SearchTermQuery.class,
            t -> {
              assertThat(t.field()).isEqualTo("flowNodeId");
              assertThat(t.value().stringValue()).isEqualTo("flowNodeId-17");
            });
  }

  @Test
  public void shouldQueryByFlowNodeInstanceKey() {
    final var filter = FilterBuilders.incident(f -> f.flowNodeInstanceKeys(42L));

    // when
    final var searchRequest = transformQuery(filter);

    // then
    final var queryVariant = searchRequest.queryOption();
    assertThat(queryVariant)
        .isInstanceOfSatisfying(
            SearchTermQuery.class,
            t -> {
              assertThat(t.field()).isEqualTo("flowNodeInstanceKey");
              assertThat(t.value().longValue()).isEqualTo(42L);
            });
  }

  @Test
  public void shouldQueryByCreationTime() {
    final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
    final var date = OffsetDateTime.ofInstant(Instant.now(), ZoneId.systemDefault());
    final var dateFilter = new DateValueFilter.Builder().before(date).after(date).build();
    final var filter = FilterBuilders.incident(f -> f.creationTime(dateFilter));

    // when
    final var searchRequest = transformQuery(filter);

    // then
    final var queryVariant = searchRequest.queryOption();
    assertThat(queryVariant)
        .isInstanceOfSatisfying(
            SearchRangeQuery.class,
            t -> {
              assertThat(t.field()).isEqualTo("creationTime");
              assertThat(t.gte().toString()).isEqualTo(date.format(formatter));
            });
  }

  @Test
  public void shouldQueryByState() {
    final var filter = FilterBuilders.incident(f -> f.states(IncidentState.ACTIVE));

    // when
    final var searchRequest = transformQuery(filter);

    // then
    final var queryVariant = searchRequest.queryOption();
    assertThat(queryVariant)
        .isInstanceOfSatisfying(
            SearchTermQuery.class,
            t -> {
              assertThat(t.field()).isEqualTo("state");
              assertThat(t.value().stringValue()).isEqualTo("ACTIVE");
            });
  }

  @Test
  public void shouldQueryByJobKey() {
    final var filter = FilterBuilders.incident(f -> f.jobKeys(23L));

    // when
    final var searchRequest = transformQuery(filter);

    // then
    final var queryVariant = searchRequest.queryOption();
    assertThat(queryVariant)
        .isInstanceOfSatisfying(
            SearchTermQuery.class,
            t -> {
              assertThat(t.field()).isEqualTo("jobKey");
              assertThat(t.value().longValue()).isEqualTo(23L);
            });
  }

  @Test
  public void shouldQueryByTreePath() {
    final var filter = FilterBuilders.incident(f -> f.treePaths("/"));

    // when
    final var searchRequest = transformQuery(filter);

    // then
    final var queryVariant = searchRequest.queryOption();
    assertThat(queryVariant)
        .isInstanceOfSatisfying(
            SearchTermQuery.class,
            t -> {
              assertThat(t.field()).isEqualTo("treePath");
              assertThat(t.value().stringValue()).isEqualTo("/");
            });
  }

  @Test
  public void shouldQueryByTenantId() {
    final var filter = FilterBuilders.incident(f -> f.tenantIds("Homer"));

    // when
    final var searchRequest = transformQuery(filter);

    // then
    final var queryVariant = searchRequest.queryOption();
    assertThat(queryVariant)
        .isInstanceOfSatisfying(
            SearchTermQuery.class,
            t -> {
              assertThat(t.field()).isEqualTo("tenantId");
              assertThat(t.value().stringValue()).isEqualTo("Homer");
            });
  }
}