/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH under
 * one or more contributor license agreements. See the NOTICE file distributed
 * with this work for additional information regarding copyright ownership.
 * Licensed under the Camunda License 1.0. You may not use this file
 * except in compliance with the Camunda License 1.0.
 */
package io.camunda.zeebe.operate.exporter.util;

import static io.camunda.operate.schema.indices.IndexDescriptor.DEFAULT_TENANT_ID;

import org.apache.commons.lang3.StringUtils;

public class OperateExportUtil {

  public static String tenantOrDefault(final String tenantId) {
    if (StringUtils.isEmpty(tenantId)) {
      return DEFAULT_TENANT_ID;
    }
    return tenantId;
  }
}