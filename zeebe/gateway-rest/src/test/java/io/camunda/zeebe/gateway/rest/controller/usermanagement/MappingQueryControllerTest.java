/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH under
 * one or more contributor license agreements. See the NOTICE file distributed
 * with this work for additional information regarding copyright ownership.
 * Licensed under the Camunda License 1.0. You may not use this file
 * except in compliance with the Camunda License 1.0.
 */
package io.camunda.zeebe.gateway.rest.controller.usermanagement;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.camunda.search.entities.MappingEntity;
import io.camunda.search.exception.NotFoundException;
import io.camunda.search.page.SearchQueryPage;
import io.camunda.search.query.MappingQuery;
import io.camunda.search.query.SearchQueryResult;
import io.camunda.search.sort.MappingSort;
import io.camunda.security.auth.Authentication;
import io.camunda.service.MappingServices;
import io.camunda.zeebe.gateway.rest.RestControllerTest;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;

@WebMvcTest(value = MappingQueryController.class, properties = "camunda.rest.query.enabled=true")
public class MappingQueryControllerTest extends RestControllerTest {
  private static final String MAPPING_BASE_URL = "/v2/mapping-rules";

  @MockBean private MappingServices mappingServices;

  @BeforeEach
  void setup() {
    when(mappingServices.withAuthentication(any(Authentication.class))).thenReturn(mappingServices);
  }

  @Test
  void getMappingShouldReturnOk() {
    // given
    final var mapping = new MappingEntity(100L, "Claim Name", "Claim Value");
    when(mappingServices.getMapping(mapping.mappingKey())).thenReturn(mapping);

    // when
    webClient
        .get()
        .uri("%s/%s".formatted(MAPPING_BASE_URL, mapping.mappingKey()))
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody()
        .json(
            """
            {
              "mappingKey": 100,
              "claimName": "Claim Name",
              "claimValue": "Claim Value"
            }""");

    // then
    verify(mappingServices, times(1)).getMapping(mapping.mappingKey());
  }

  @Test
  void getNonExistingMappingShouldReturnNotFound() {
    // given
    final var mappingKey = 100L;
    final var path = "%s/%s".formatted(MAPPING_BASE_URL, mappingKey);
    when(mappingServices.getMapping(mappingKey))
        .thenThrow(new NotFoundException("mapping not found"));

    // when
    webClient
        .get()
        .uri(path)
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus()
        .isNotFound()
        .expectBody()
        .json(
            """
            {
              "type": "about:blank",
              "title": "NOT_FOUND",
              "status": 404,
              "detail": "mapping not found",
              "instance": "%s"
            }"""
                .formatted(path));

    // then
    verify(mappingServices, times(1)).getMapping(mappingKey);
  }

  @Test
  void shouldSearchMappingsWithEmptyQuery() {
    // given
    when(mappingServices.search(any(MappingQuery.class)))
        .thenReturn(
            new SearchQueryResult.Builder<MappingEntity>()
                .total(3)
                .sortValues(new Object[] {})
                .items(
                    List.of(
                        new MappingEntity(100L, "Claim Name1", "Claim Value1"),
                        new MappingEntity(200L, "Claim Name2", "Claim Value2"),
                        new MappingEntity(300L, "Claim Name3", "Claim Value3")))
                .build());

    // when / then
    webClient
        .post()
        .uri("%s/search".formatted(MAPPING_BASE_URL))
        .accept(MediaType.APPLICATION_JSON)
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue("{}")
        .exchange()
        .expectStatus()
        .isOk()
        .expectHeader()
        .contentType(MediaType.APPLICATION_JSON)
        .expectBody()
        .json(
            """
          {
             "items": [
               {
                 "mappingKey": 100,
                 "claimName": "Claim Name1",
                 "claimValue": "Claim Value1"
               },
               {
                 "mappingKey": 200,
                 "claimName": "Claim Name2",
                 "claimValue": "Claim Value2"
               },
               {
                 "mappingKey": 300,
                 "claimName": "Claim Name3",
                 "claimValue": "Claim Value3"
               }
             ],
             "page": {
               "totalItems": 3,
               "firstSortValues": [],
               "lastSortValues": []
             }
           }""");

    verify(mappingServices).search(new MappingQuery.Builder().build());
  }

  @Test
  void shouldSortAndPaginateSearchResult() {
    // given
    when(mappingServices.search(any(MappingQuery.class)))
        .thenReturn(
            new SearchQueryResult.Builder<MappingEntity>()
                .total(3)
                .items(
                    List.of(
                        new MappingEntity(100L, "Claim Name1", "Claim Value1"),
                        new MappingEntity(200L, "Claim Name2", "Claim Value2"),
                        new MappingEntity(300L, "Claim Name3", "Claim Value3")))
                .build());

    // when / then
    webClient
        .post()
        .uri("%s/search".formatted(MAPPING_BASE_URL))
        .accept(MediaType.APPLICATION_JSON)
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(
            """
            {
              "sort":  [{"field": "claimName", "order":  "asc"}],
              "page":  {"from":  20, "limit":  10}
            }
             """)
        .exchange()
        .expectStatus()
        .isOk();

    verify(mappingServices)
        .search(
            new MappingQuery.Builder()
                .sort(MappingSort.of(builder -> builder.claimName().asc()))
                .page(SearchQueryPage.of(builder -> builder.from(20).size(10)))
                .build());
  }
}