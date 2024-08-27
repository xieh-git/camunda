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
import java.util.function.Function;

public record FlownodeInstanceSort(List<FieldSorting> orderings) implements SortOption {

  @Override
  public List<FieldSorting> getFieldSortings() {
    return orderings;
  }

  public static FlownodeInstanceSort of(
      final Function<FlownodeInstanceSort.Builder, ObjectBuilder<FlownodeInstanceSort>> fn) {
    return SortOptionBuilders.flownodeInstance(fn);
  }

  public static final class Builder extends AbstractBuilder<Builder>
      implements ObjectBuilder<FlownodeInstanceSort> {

    public Builder key() {
      currentOrdering = new FieldSorting("key", null);
      return this;
    }

    public Builder processInstanceKey() {
      currentOrdering = new FieldSorting("processInstanceKey", null);
      return this;
    }

    public Builder processDefinitionKey() {
      currentOrdering = new FieldSorting("processDefinitionKey", null);
      return this;
    }

    public Builder startDate() {
      currentOrdering = new FieldSorting("startDate", null);
      return this;
    }

    public Builder endDate() {
      currentOrdering = new FieldSorting("endDate", null);
      return this;
    }

    public Builder flowNodeId() {
      currentOrdering = new FieldSorting("flowNodeId", null);
      return this;
    }

    public Builder flowNodeName() {
      currentOrdering = new FieldSorting("flowNodeName", null);
      return this;
    }

    public Builder type() {
      currentOrdering = new FieldSorting("type", null);
      return this;
    }

    public Builder state() {
      currentOrdering = new FieldSorting("state", null);
      return this;
    }

    public Builder incidentKey() {
      currentOrdering = new FieldSorting("incidentKey", null);
      return this;
    }

    public Builder tenantId() {
      currentOrdering = new FieldSorting("tenantId", null);
      return this;
    }

    @Override
    public FlownodeInstanceSort build() {
      return new FlownodeInstanceSort(orderings);
    }

    @Override
    protected Builder self() {
      return this;
    }
  }
}
