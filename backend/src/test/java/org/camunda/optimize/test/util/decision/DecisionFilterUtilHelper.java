/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. Licensed under a commercial license.
 * You may not use this file except in compliance with the commercial license.
 */
package org.camunda.optimize.test.util.decision;

import org.camunda.optimize.dto.optimize.query.report.single.decision.filter.EvaluationDateFilterDto;
import org.camunda.optimize.dto.optimize.query.report.single.decision.filter.InputVariableFilterDto;
import org.camunda.optimize.dto.optimize.query.report.single.decision.filter.OutputVariableFilterDto;
import org.camunda.optimize.dto.optimize.query.report.single.filter.data.date.DateFilterDataDto;
import org.camunda.optimize.dto.optimize.query.report.single.filter.data.date.DateFilterUnit;
import org.camunda.optimize.dto.optimize.query.report.single.filter.data.date.FixedDateFilterDataDto;
import org.camunda.optimize.dto.optimize.query.report.single.filter.data.date.RelativeDateFilterDataDto;
import org.camunda.optimize.dto.optimize.query.report.single.filter.data.date.RelativeDateFilterStartDto;
import org.camunda.optimize.dto.optimize.query.report.single.filter.data.date.RollingDateFilterDataDto;
import org.camunda.optimize.dto.optimize.query.report.single.filter.data.date.RollingDateFilterStartDto;
import org.camunda.optimize.dto.optimize.query.report.single.filter.data.variable.BooleanVariableFilterDataDto;
import org.camunda.optimize.dto.optimize.query.report.single.filter.data.variable.DateVariableFilterDataDto;
import org.camunda.optimize.dto.optimize.query.report.single.filter.data.variable.DoubleVariableFilterDataDto;
import org.camunda.optimize.dto.optimize.query.report.single.filter.data.variable.StringVariableFilterDataDto;
import org.camunda.optimize.dto.optimize.query.report.single.filter.data.variable.VariableFilterDataDto;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Collections;

public class DecisionFilterUtilHelper {

  public static EvaluationDateFilterDto createFixedEvaluationDateFilter(OffsetDateTime startDate,
                                                                        OffsetDateTime endDate) {
    final FixedDateFilterDataDto fixedDateFilterDataDto = new FixedDateFilterDataDto(startDate, endDate);
    EvaluationDateFilterDto filter = new EvaluationDateFilterDto();
    filter.setData(fixedDateFilterDataDto);
    return filter;
  }

  public static EvaluationDateFilterDto createRelativeEvaluationDateFilter(Long value, DateFilterUnit unit) {
    RelativeDateFilterDataDto filterData = new RelativeDateFilterDataDto(new RelativeDateFilterStartDto(value, unit));
    EvaluationDateFilterDto filter = new EvaluationDateFilterDto();
    filter.setData(filterData);
    return filter;
  }

  public static EvaluationDateFilterDto createRollingEvaluationDateFilter(Long value, DateFilterUnit unit) {
    RollingDateFilterStartDto evaluationDate = new RollingDateFilterStartDto(value, unit);
    RollingDateFilterDataDto filterData = new RollingDateFilterDataDto(evaluationDate);
    EvaluationDateFilterDto filter = new EvaluationDateFilterDto();
    filter.setData(filterData);
    return filter;
  }

  public static InputVariableFilterDto createStringInputVariableFilter(String variableName, String operator,
                                                                       String... variableValues) {
    StringVariableFilterDataDto data = new StringVariableFilterDataDto(
      variableName, operator, Arrays.asList(variableValues)
    );
    InputVariableFilterDto variableFilterDto = new InputVariableFilterDto();
    variableFilterDto.setData(data);

    return variableFilterDto;
  }

  public static InputVariableFilterDto createNumericInputVariableFilter(String variableName, String operator,
                                                                        String... variableValues) {
    DoubleVariableFilterDataDto data = new DoubleVariableFilterDataDto(
      variableName,
      operator,
      Arrays.asList(variableValues)
    );
    InputVariableFilterDto variableFilterDto = new InputVariableFilterDto();
    variableFilterDto.setData(data);

    return variableFilterDto;
  }

  public static InputVariableFilterDto createDateInputVariableFilter(final String variableName,
                                                                     final DateFilterDataDto<?> dateFilterDataDto) {
    final DateVariableFilterDataDto data = new DateVariableFilterDataDto(variableName, dateFilterDataDto);
    InputVariableFilterDto variableFilterDto = new InputVariableFilterDto();
    variableFilterDto.setData(data);

    return variableFilterDto;
  }

  public static InputVariableFilterDto createFixedDateInputVariableFilter(final String variableName,
                                                                          final OffsetDateTime startDate,
                                                                          final OffsetDateTime endDate) {
    return createDateInputVariableFilter(variableName, new FixedDateFilterDataDto(startDate, endDate));
  }

  public static InputVariableFilterDto createRelativeDateInputVariableFilter(final String variableName,
                                                                             final Long value,
                                                                             final DateFilterUnit unit) {
    return createDateInputVariableFilter(
      variableName, new RelativeDateFilterDataDto(new RelativeDateFilterStartDto(value, unit))
    );
  }

  public static InputVariableFilterDto createRollingDateInputVariableFilter(final String variableName,
                                                                            final Long value,
                                                                            final DateFilterUnit unit) {
    return createDateInputVariableFilter(
      variableName, new RollingDateFilterDataDto(new RollingDateFilterStartDto(value, unit))
    );
  }

  public static OutputVariableFilterDto createBooleanOutputVariableFilter(String variableName, Boolean variableValue) {
    BooleanVariableFilterDataDto data = new BooleanVariableFilterDataDto(variableName, variableValue);
    OutputVariableFilterDto variableFilterDto = new OutputVariableFilterDto();
    variableFilterDto.setData(data);

    return variableFilterDto;
  }

  public static VariableFilterDataDto createUndefinedVariableFilterData(String variableName) {
    VariableFilterDataDto data = new StringVariableFilterDataDto(variableName, "whatever", Collections.emptyList());
    data.setFilterForUndefined(true);
    return data;
  }

  public static VariableFilterDataDto createExcludeUndefinedVariableFilterData(String variableName) {
    VariableFilterDataDto data = new StringVariableFilterDataDto(variableName, "whatever", Collections.emptyList());
    data.setExcludeUndefined(true);
    return data;
  }

}
