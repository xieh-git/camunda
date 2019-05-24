/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. Licensed under a commercial license.
 * You may not use this file except in compliance with the commercial license.
 */
package org.camunda.optimize.service.es.report.command.process.processinstance.duration.groupby.none;

import org.camunda.optimize.service.es.report.command.aggregations.AggregationStrategy;
import org.camunda.optimize.service.es.schema.type.ProcessInstanceType;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.Aggregations;

public class ProcessInstanceDurationGroupByNoneCommand extends
  AbstractProcessInstanceDurationGroupByNoneCommand {

  public ProcessInstanceDurationGroupByNoneCommand(AggregationStrategy strategy) {
    aggregationStrategy = strategy;
  }

  @Override
  protected Long processAggregationOperation(Aggregations aggs) {
    return aggregationStrategy.getValue(aggs);
  }

  @Override
  protected AggregationBuilder createOperationsAggregation() {
    return aggregationStrategy.getAggregationBuilder().field(ProcessInstanceType.DURATION);
  }
}
