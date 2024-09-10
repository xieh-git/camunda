/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH under
 * one or more contributor license agreements. See the NOTICE file distributed
 * with this work for additional information regarding copyright ownership.
 * Licensed under the Camunda License 1.0. You may not use this file
 * except in compliance with the Camunda License 1.0.
 */
package io.camunda.optimize.service.db.es.report.service;

import static io.camunda.optimize.service.db.es.report.interpreter.util.NumberHistogramAggregationUtilES.generateHistogramWithField;
import static io.camunda.optimize.service.db.es.report.service.VariableAggregationServiceES.VARIABLE_HISTOGRAM_AGGREGATION;

import io.camunda.optimize.dto.optimize.query.variable.VariableType;
import io.camunda.optimize.service.db.es.report.context.VariableAggregationContextES;
import io.camunda.optimize.service.db.report.interpreter.service.AbstractNumberVariableAggregationService;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.histogram.HistogramAggregationBuilder;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class NumberVariableAggregationServiceES extends AbstractNumberVariableAggregationService {
  public Optional<AggregationBuilder> createNumberVariableAggregation(
      final VariableAggregationContextES context) {
    if (context.getVariableRangeMinMaxStats().isEmpty()) {
      return Optional.empty();
    }

    final Optional<Double> min = getBaselineForNumberVariableAggregation(context);
    if (min.isEmpty()) {
      // no valid baseline is set, return empty result
      return Optional.empty();
    }

    final double intervalSize = getIntervalSize(context, min.get());
    final double max = context.getMaxVariableValue();

    final String digitFormat = VariableType.DOUBLE.equals(context.getVariableType()) ? "0.00" : "0";

    final HistogramAggregationBuilder histogramAggregation =
        generateHistogramWithField(
            VARIABLE_HISTOGRAM_AGGREGATION,
            intervalSize,
            min.get(),
            max,
            context.getNestedVariableValueFieldLabel(),
            digitFormat,
            context.getSubAggregations());

    return Optional.of(histogramAggregation);
  }
}
