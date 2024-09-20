/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH under
 * one or more contributor license agreements. See the NOTICE file distributed
 * with this work for additional information regarding copyright ownership.
 * Licensed under the Camunda License 1.0. You may not use this file
 * except in compliance with the Camunda License 1.0.
 */
package io.camunda.service.search.sort;

import io.camunda.util.ObjectBuilder;
import java.util.List;

public record ProcessDefinitionSort(List<FieldSorting> orderings) implements SortOption {

  @Override
  public List<FieldSorting> getFieldSortings() {
    return orderings;
  }

  public static final class Builder
      extends SortOption.AbstractBuilder<ProcessDefinitionSort.Builder>
      implements ObjectBuilder<ProcessDefinitionSort> {

    public Builder processDefinitionKey() {
      currentOrdering = new FieldSorting("key", null);
      return this;
    }

    public Builder processName() {
      currentOrdering = new FieldSorting("name", null);
      return this;
    }

    public Builder processVersion() {
      currentOrdering = new FieldSorting("version", null);
      return this;
    }

    public Builder processVersionTag() {
      currentOrdering = new FieldSorting("versionTag", null);
      return this;
    }

    public Builder bpmnProcessId() {
      currentOrdering = new FieldSorting("bpmnProcessId", null);
      return this;
    }

    public Builder tenantId() {
      currentOrdering = new FieldSorting("tenantId", null);
      return this;
    }

    public Builder formKey() {
      currentOrdering = new FieldSorting("formKey", null);
      return this;
    }

    @Override
    protected ProcessDefinitionSort.Builder self() {
      return this;
    }

    @Override
    public ProcessDefinitionSort build() {
      return new ProcessDefinitionSort(orderings);
    }
  }
}
