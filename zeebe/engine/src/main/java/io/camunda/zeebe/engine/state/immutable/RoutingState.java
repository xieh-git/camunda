/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH under
 * one or more contributor license agreements. See the NOTICE file distributed
 * with this work for additional information regarding copyright ownership.
 * Licensed under the Camunda License 1.0. You may not use this file
 * except in compliance with the Camunda License 1.0.
 */
package io.camunda.zeebe.engine.state.immutable;

import java.util.Set;

public interface RoutingState {
  Set<Integer> partitions();

  MessageCorrelation messageCorrelation();

  sealed interface MessageCorrelation {
    record HashMod(int partitionCount) implements MessageCorrelation {
      public HashMod {
        if (partitionCount <= 0) {
          throw new IllegalArgumentException("Partition count must be positive");
        }
      }
    }
  }
}
