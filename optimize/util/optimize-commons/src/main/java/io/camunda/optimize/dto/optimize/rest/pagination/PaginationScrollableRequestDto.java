/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH under
 * one or more contributor license agreements. See the NOTICE file distributed
 * with this work for additional information regarding copyright ownership.
 * Licensed under the Camunda License 1.0. You may not use this file
 * except in compliance with the Camunda License 1.0.
 */
package io.camunda.optimize.dto.optimize.rest.pagination;

import static io.camunda.optimize.service.db.DatabaseConstants.MAX_RESPONSE_SIZE_LIMIT;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.QueryParam;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaginationScrollableRequestDto {

  public static final String QUERY_LIMIT_PARAM = "limit";
  public static final String QUERY_SCROLL_ID_PARAM = "searchRequestId";
  public static final String QUERY_SCROLL_TIMEOUT_PARAM = "paginationTimeout";

  @QueryParam(QUERY_LIMIT_PARAM)
  @Min(0)
  @DefaultValue("1000")
  @Max(MAX_RESPONSE_SIZE_LIMIT)
  protected Integer limit;

  @QueryParam(QUERY_SCROLL_ID_PARAM)
  protected String scrollId;

  @QueryParam(QUERY_SCROLL_TIMEOUT_PARAM)
  @Min(60)
  @DefaultValue("120")
  protected Integer scrollTimeout;
}