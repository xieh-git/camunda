/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH under
 * one or more contributor license agreements. See the NOTICE file distributed
 * with this work for additional information regarding copyright ownership.
 * Licensed under the Camunda License 1.0. You may not use this file
 * except in compliance with the Camunda License 1.0.
 */
package io.camunda.webapps.schema.entities.tasklist;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.camunda.webapps.schema.entities.operate.VariableEntity;

public class TaskVariableEntity extends TasklistEntity<TaskVariableEntity> {

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private String name;

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private String value;

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private String fullValue;

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private Boolean isTruncated;

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private Long scopeKey;

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private Long processInstanceId;

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private TaskJoinRelationship join;

  public TaskVariableEntity() {}

  public TaskVariableEntity(final VariableEntity entity) {
    setKey(entity.getKey());
    setId(entity.getId());
    setPartitionId(entity.getPartitionId());
    setTenantId(entity.getTenantId());
    name = entity.getName();
    value = entity.getValue();
    fullValue = entity.getFullValue();
    isTruncated = entity.getIsPreview();
    scopeKey = entity.getScopeKey();
    processInstanceId = entity.getProcessInstanceKey();
    join = new TaskJoinRelationship();
  }

  public String getName() {
    return name;
  }

  public TaskVariableEntity setName(final String name) {
    this.name = name;
    return this;
  }

  public String getValue() {
    return value;
  }

  public TaskVariableEntity setValue(final String value) {
    this.value = value;
    return this;
  }

  public String getFullValue() {
    return fullValue;
  }

  public TaskVariableEntity setFullValue(final String fullValue) {
    this.fullValue = fullValue;
    return this;
  }

  public Boolean getIsTruncated() {
    return isTruncated;
  }

  public TaskVariableEntity setIsTruncated(final Boolean truncated) {
    isTruncated = truncated;
    return this;
  }

  public Long getScopeKey() {
    return scopeKey;
  }

  public TaskVariableEntity setScopeKey(final Long scopeKey) {
    this.scopeKey = scopeKey;
    return this;
  }

  public Long getProcessInstanceId() {
    return processInstanceId;
  }

  public TaskVariableEntity setProcessInstanceId(final Long processInstanceId) {
    this.processInstanceId = processInstanceId;
    return this;
  }

  public TaskJoinRelationship getJoin() {
    return join;
  }

  public TaskVariableEntity setJoin(final TaskJoinRelationship join) {
    this.join = join;
    return this;
  }

  public static TaskVariableEntity createFrom(
      final String tenantId,
      final String id,
      final String name,
      final String value,
      final Long scopeKey,
      final int variableSizeThreshold,
      final TaskJoinRelationship joinRelationship) {
    final TaskVariableEntity entity = new TaskVariableEntity().setId(id).setName(name);
    if (value.length() > variableSizeThreshold) {
      entity.setValue(value.substring(0, variableSizeThreshold));
      entity.setFullValue(value);
      entity.setIsTruncated(true);
    } else {
      entity.setIsTruncated(false);
      entity.setValue(value);
    }
    entity.setScopeKey(scopeKey);
    entity.setTenantId(tenantId);
    entity.setJoin(joinRelationship);
    return entity;
  }
}