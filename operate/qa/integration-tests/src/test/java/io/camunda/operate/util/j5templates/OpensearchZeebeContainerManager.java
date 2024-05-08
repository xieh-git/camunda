/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH under
 * one or more contributor license agreements. See the NOTICE file distributed
 * with this work for additional information regarding copyright ownership.
 * Licensed under the Camunda License 1.0. You may not use this file
 * except in compliance with the Camunda License 1.0.
 */
package io.camunda.operate.util.j5templates;

import io.camunda.operate.conditions.OpensearchCondition;
import io.camunda.operate.property.OperateProperties;
import io.camunda.operate.qa.util.TestContainerUtil;
import io.camunda.operate.store.opensearch.client.sync.ZeebeRichOpenSearchClient;
import io.camunda.operate.util.TestUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

@Conditional(OpensearchCondition.class)
@Component
public class OpensearchZeebeContainerManager extends ZeebeContainerManager {
  private static final Logger LOGGER =
      LoggerFactory.getLogger(OpensearchZeebeContainerManager.class);

  private final ZeebeRichOpenSearchClient zeebeRichOpenSearchClient;

  public OpensearchZeebeContainerManager(
      OperateProperties operateProperties,
      TestContainerUtil testContainerUtil,
      ZeebeRichOpenSearchClient zeebeRichOpenSearchClient) {
    super(operateProperties, testContainerUtil);
    this.zeebeRichOpenSearchClient = zeebeRichOpenSearchClient;
  }

  protected void updatePrefix() {
    LOGGER.info("Starting Zeebe with OS prefix: " + prefix);
    operateProperties.getZeebeOpensearch().setPrefix(prefix);
  }

  protected void removeIndices() {
    TestUtil.removeAllIndices(
        zeebeRichOpenSearchClient.index(), zeebeRichOpenSearchClient.template(), prefix);
  }
}
