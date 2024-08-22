/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH under
 * one or more contributor license agreements. See the NOTICE file distributed
 * with this work for additional information regarding copyright ownership.
 * Licensed under the Camunda License 1.0. You may not use this file
 * except in compliance with the Camunda License 1.0.
 */
package io.camunda.zeebe.engine.processing.usertask;

import io.camunda.zeebe.engine.processing.bpmn.behavior.BpmnBehaviors;
import io.camunda.zeebe.engine.processing.common.EventHandle;
import io.camunda.zeebe.engine.processing.streamprocessor.TypedRecordProcessors;
import io.camunda.zeebe.engine.processing.streamprocessor.writers.Writers;
import io.camunda.zeebe.engine.state.mutable.MutableProcessingState;
import io.camunda.zeebe.protocol.record.ValueType;
import io.camunda.zeebe.protocol.record.intent.UserTaskIntent;

public final class UserTaskEventProcessors {

  public static void addUserTaskProcessors(
      final TypedRecordProcessors typedRecordProcessors,
      final MutableProcessingState processingState,
      final BpmnBehaviors bpmnBehaviors,
      final Writers writers) {

    final var keyGenerator = processingState.getKeyGenerator();

    final EventHandle eventHandle =
        new EventHandle(
            keyGenerator,
            processingState.getEventScopeInstanceState(),
            writers,
            processingState.getProcessState(),
            bpmnBehaviors.eventTriggerBehavior(),
            bpmnBehaviors.stateBehavior());

    typedRecordProcessors
        .onCommand(
            ValueType.USER_TASK,
            UserTaskIntent.COMPLETE,
            new UserTaskCompleteProcessor(
                processingState, eventHandle, writers, bpmnBehaviors.jobBehavior()))
        .onCommand(
            ValueType.USER_TASK,
            UserTaskIntent.COMPLETE_TASK_LISTENER,
            new UserTaskListenerCompleteProcessor(
                processingState, eventHandle, writers, bpmnBehaviors.jobBehavior()))
        .onCommand(
            ValueType.USER_TASK,
            UserTaskIntent.ASSIGN,
            new UserTaskAssignProcessor(processingState, writers))
        .onCommand(
            ValueType.USER_TASK,
            UserTaskIntent.CLAIM,
            new UserTaskClaimProcessor(processingState, writers))
        .onCommand(
            ValueType.USER_TASK,
            UserTaskIntent.UPDATE,
            new UserTaskUpdateProcessor(processingState, writers));
  }
}
