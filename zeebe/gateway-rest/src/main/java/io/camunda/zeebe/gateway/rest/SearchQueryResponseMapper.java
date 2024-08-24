/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH under
 * one or more contributor license agreements. See the NOTICE file distributed
 * with this work for additional information regarding copyright ownership.
 * Licensed under the Camunda License 1.0. You may not use this file
 * except in compliance with the Camunda License 1.0.
 */
package io.camunda.zeebe.gateway.rest;

import static java.util.Optional.ofNullable;

import io.camunda.service.entities.*;
import io.camunda.service.search.query.SearchQueryResult;
import io.camunda.zeebe.gateway.protocol.rest.*;
import io.camunda.zeebe.util.Either;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public final class SearchQueryResponseMapper {

  private SearchQueryResponseMapper() {}

  public static ProcessInstanceSearchQueryResponse toProcessInstanceSearchQueryResponse(
      final SearchQueryResult<ProcessInstanceEntity> result) {
    final var page = toSearchQueryPageResponse(result);
    return new ProcessInstanceSearchQueryResponse()
        .page(page)
        .items(
            ofNullable(result.items())
                .map(SearchQueryResponseMapper::toProcessInstances)
                .orElseGet(Collections::emptyList));
  }

  public static DecisionDefinitionSearchQueryResponse toDecisionDefinitionSearchQueryResponse(
      final SearchQueryResult<DecisionDefinitionEntity> result) {
    final var page = toSearchQueryPageResponse(result);
    return new DecisionDefinitionSearchQueryResponse()
        .page(page)
        .items(
            ofNullable(result.items())
                .map(SearchQueryResponseMapper::toDecisionDefinitions)
                .orElseGet(Collections::emptyList));
  }

  public static DecisionRequirementsSearchQueryResponse toDecisionRequirementsSearchQueryResponse(
      final SearchQueryResult<DecisionRequirementsEntity> result) {
    final var page = toSearchQueryPageResponse(result);
    return new DecisionRequirementsSearchQueryResponse()
        .page(page)
        .items(
            ofNullable(result.items())
                .map(SearchQueryResponseMapper::toDecisionRequirements)
                .orElseGet(Collections::emptyList));
  }

  public static UserTaskSearchQueryResponse toUserTaskSearchQueryResponse(
      final SearchQueryResult<UserTaskEntity> result) {
    final var page = toSearchQueryPageResponse(result);
    return new UserTaskSearchQueryResponse()
        .page(page)
        .items(
            ofNullable(result.items())
                .map(SearchQueryResponseMapper::toUserTasks)
                .orElseGet(Collections::emptyList));
  }

  public static UserSearchResponse toUserSearchQueryResponse(
      final SearchQueryResult<UserEntity> result) {
    final var response = new UserSearchResponse();
    final var total = result.total();
    final var sortValues = result.sortValues();
    final var items = result.items();

    final var page = new SearchQueryPageResponse();
    page.setTotalItems(total);
    response.setPage(page);

    if (sortValues != null) {
      page.setLastSortValues(Arrays.asList(sortValues));
    }

    if (items != null) {
      response.setItems(toUsers(items).get());
    }

    return response;
  }

  private static SearchQueryPageResponse toSearchQueryPageResponse(
      final SearchQueryResult<?> result) {
    return new SearchQueryPageResponse()
        .totalItems(result.total())
        .lastSortValues(
            ofNullable(result.sortValues()).map(Arrays::asList).orElseGet(Collections::emptyList));
  }

  private static List<ProcessInstanceItem> toProcessInstances(
      final List<ProcessInstanceEntity> instances) {
    return instances.stream().map(SearchQueryResponseMapper::toProcessInstance).toList();
  }

  private static ProcessInstanceItem toProcessInstance(final ProcessInstanceEntity p) {
    return new ProcessInstanceItem()
        .key(p.key())
        .processName(p.processName())
        .processVersion(p.processVersion())
        .bpmnProcessId(p.bpmnProcessId())
        .parentKey(p.parentProcessInstanceKey())
        .parentFlowNodeInstanceKey(p.parentFlowNodeInstanceKey())
        .startDate(p.startDate())
        .endDate(p.endDate())
        .state((p.state() == null) ? null : ProcessInstanceItem.StateEnum.fromValue(p.state()))
        .incident(p.incident())
        .hasActiveOperation(p.hasActiveOperation())
        .processDefinitionKey(p.processDefinitionKey())
        .tenantId(p.tenantId())
        .rootInstanceId(p.rootInstanceId())
        .operations(toOperations(p.operations()))
        .callHierarchy(toCallHierarchy(p.callHierarchy()));
  }

  private static List<OperationItem> toOperations(final List<OperationEntity> instances) {
    if (instances == null) {
      return null;
    }
    return instances.stream().map(SearchQueryResponseMapper::toOperation).toList();
  }

  private static OperationItem toOperation(final OperationEntity o) {
    return new OperationItem()
        .id(o.id())
        .batchOperationId(o.batchOperationId())
        .type((o.type() == null) ? null : (OperationItem.TypeEnum.fromValue(o.type())))
        .state((o.state() == null) ? null : (OperationItem.StateEnum.fromValue(o.state())))
        .errorMessage(o.errorMessage())
        .completedDate(o.completedDate());
  }

  private static List<CallHierarchyItem> toCallHierarchy(
      final List<ProcessInstanceReference> instances) {
    if (instances == null) {
      return null;
    }
    return instances.stream().map(SearchQueryResponseMapper::toCallHierarchy).toList();
  }

  private static CallHierarchyItem toCallHierarchy(final ProcessInstanceReference p) {
    return new CallHierarchyItem()
        .instanceId(p.instanceId())
        .processDefinitionId(p.processDefinitionId())
        .processDefinitionName(p.processDefinitionName());
  }

  private static List<DecisionDefinitionItem> toDecisionDefinitions(
      final List<DecisionDefinitionEntity> instances) {
    return instances.stream().map(SearchQueryResponseMapper::toDecisionDefinition).toList();
  }

  private static List<DecisionRequirementsItem> toDecisionRequirements(
      final List<DecisionRequirementsEntity> instances) {
    return instances.stream().map(SearchQueryResponseMapper::toDecisionRequirements).toList();
  }

  private static DecisionDefinitionItem toDecisionDefinition(final DecisionDefinitionEntity d) {
    return new DecisionDefinitionItem()
        .tenantId(d.tenantId())
        .decisionKey(d.key())
        .dmnDecisionName(d.name())
        .version(d.version())
        .dmnDecisionId(d.decisionId())
        .decisionRequirementsKey(d.decisionRequirementsKey())
        .dmnDecisionRequirementsId(d.decisionRequirementsId());
  }

  private static DecisionRequirementsItem toDecisionRequirements(
      final DecisionRequirementsEntity d) {
    return new DecisionRequirementsItem()
        .tenantId(d.tenantId())
        .decisionRequirementsKey(d.key())
        .dmnDecisionRequirementsName(d.name())
        .version(d.version())
        .resourceName(d.resourceName())
        .dmnDecisionRequirementsId(d.decisionRequirementsId());
  }

  private static List<UserTaskItem> toUserTasks(final List<UserTaskEntity> tasks) {
    return tasks.stream().map(SearchQueryResponseMapper::toUserTask).toList();
  }

  private static UserTaskItem toUserTask(final UserTaskEntity t) {
    return new UserTaskItem()
        .tenantIds(t.tenantId())
        .key(t.key())
        .processInstanceKey(t.processInstanceId())
        .processDefinitionKey(t.processDefinitionId())
        .elementInstanceKey(t.flowNodeInstanceId())
        .bpmnProcessId(t.bpmnProcessId())
        .state(t.state())
        .assignee(t.assignee())
        .candidateUser(t.candidateUsers())
        .candidateGroup(t.candidateGroups())
        .formKey(t.formKey())
        .elementId(t.flowNodeBpmnId())
        .creationDate(t.creationTime())
        .completionDate(t.completionTime())
        .dueDate(t.dueDate())
        .followUpDate(t.followUpDate())
        .externalFormReference(t.externalFormReference())
        .processDefinitionVersion(t.processDefinitionVersion())
        .customHeaders(t.customHeaders())
        .priority(t.priority());
  }

  public static Either<ProblemDetail, List<UserResponse>> toUsers(final List<UserEntity> users) {
    return Either.right(
        users.stream().map(SearchQueryResponseMapper::toUser).map(Either::get).toList());
  }

  public static Either<ProblemDetail, UserResponse> toUser(final UserEntity user) {
    return Either.right(
        new UserResponse()
            .username(user.value().username())
            .email(user.value().email())
            .name(user.value().name()));
  }
}
