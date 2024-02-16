/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH under
 * one or more contributor license agreements. See the NOTICE file distributed
 * with this work for additional information regarding copyright ownership.
 * Licensed under the Zeebe Community License 1.1. You may not use this file
 * except in compliance with the Zeebe Community License 1.1.
 */
package io.camunda.zeebe.broker.exporter.metrics;

import io.camunda.zeebe.broker.system.configuration.ExporterCfg;
import io.camunda.zeebe.exporter.api.Exporter;
import io.camunda.zeebe.exporter.api.context.Context;
import io.camunda.zeebe.exporter.api.context.Context.RecordFilter;
import io.camunda.zeebe.exporter.api.context.Controller;
import io.camunda.zeebe.protocol.record.Record;
import io.camunda.zeebe.protocol.record.RecordType;
import io.camunda.zeebe.protocol.record.ValueType;
import io.camunda.zeebe.protocol.record.intent.JobBatchIntent;
import io.camunda.zeebe.protocol.record.intent.JobIntent;
import io.camunda.zeebe.protocol.record.intent.ProcessInstanceIntent;
import io.camunda.zeebe.protocol.record.value.BpmnElementType;
import io.camunda.zeebe.protocol.record.value.JobBatchRecordValue;
import io.camunda.zeebe.protocol.record.value.ProcessInstanceRecordValue;
import io.camunda.zeebe.scheduler.clock.ActorClock;
import io.camunda.zeebe.util.VisibleForTesting;
import java.time.Duration;
import java.util.Set;

public class MetricsExporter implements Exporter {

  public static final Duration TIME_TO_LIVE = Duration.ofSeconds(10);
  private final ExecutionLatencyMetrics executionLatencyMetrics;
  private final TtlKeyCache processInstanceCache;
  private final TtlKeyCache jobCache;

  private Controller controller;

  public MetricsExporter() {
    this(new ExecutionLatencyMetrics());
  }

  public MetricsExporter(final ExecutionLatencyMetrics executionLatencyMetrics) {
    this(executionLatencyMetrics, new TtlKeyCache(), new TtlKeyCache());
  }

  @VisibleForTesting
  MetricsExporter(
      final ExecutionLatencyMetrics executionLatencyMetrics,
      final TtlKeyCache processInstanceCache,
      final TtlKeyCache jobCache) {
    this.executionLatencyMetrics = executionLatencyMetrics;
    this.processInstanceCache = processInstanceCache;
    this.jobCache = jobCache;
  }

  @Override
  public void configure(final Context context) throws Exception {
    context.setFilter(
        new RecordFilter() {
          private static final Set<ValueType> ACCEPTED_VALUE_TYPES =
              Set.of(ValueType.JOB, ValueType.JOB_BATCH, ValueType.PROCESS_INSTANCE);

          @Override
          public boolean acceptType(final RecordType recordType) {
            return recordType == RecordType.EVENT;
          }

          @Override
          public boolean acceptValue(final ValueType valueType) {
            return ACCEPTED_VALUE_TYPES.contains(valueType);
          }
        });
  }

  @Override
  public void open(final Controller controller) {
    this.controller = controller;

    controller.scheduleCancellableTask(TIME_TO_LIVE, this::cleanUp);
  }

  @Override
  public void close() {
    processInstanceCache.clear();
    jobCache.clear();
  }

  @Override
  public void export(final Record<?> record) {
    if (record.getRecordType() != RecordType.EVENT) {
      controller.updateLastExportedRecordPosition(record.getPosition());
      return;
    }

    final var partitionId = record.getPartitionId();
    final var recordKey = record.getKey();

    final var currentValueType = record.getValueType();
    if (currentValueType == ValueType.JOB) {
      handleJobRecord(record, partitionId, recordKey);
    } else if (currentValueType == ValueType.JOB_BATCH) {
      handleJobBatchRecord(record, partitionId);
    } else if (currentValueType == ValueType.PROCESS_INSTANCE) {
      handleProcessInstanceRecord(record, partitionId, recordKey);
    }

    controller.updateLastExportedRecordPosition(record.getPosition());
  }

  private void handleProcessInstanceRecord(
      final Record<?> record, final int partitionId, final long recordKey) {
    final var currentIntent = record.getIntent();

    if (currentIntent == ProcessInstanceIntent.ELEMENT_ACTIVATING
        && isProcessInstanceRecord(record)) {
      processInstanceCache.store(recordKey, record.getTimestamp());
    } else if (currentIntent == ProcessInstanceIntent.ELEMENT_COMPLETED
        && isProcessInstanceRecord(record)) {
      final var creationTime = processInstanceCache.remove(recordKey);
      executionLatencyMetrics.observeProcessInstanceExecutionTime(
          partitionId, creationTime, record.getTimestamp());
    }
  }

  private void handleJobRecord(
      final Record<?> record, final int partitionId, final long recordKey) {
    final var currentIntent = record.getIntent();

    if (currentIntent == JobIntent.CREATED) {
      jobCache.store(recordKey, record.getTimestamp());
    } else if (currentIntent == JobIntent.COMPLETED) {
      final var creationTime = jobCache.remove(recordKey);
      executionLatencyMetrics.observeJobLifeTime(partitionId, creationTime, record.getTimestamp());
    }
  }

  private void handleJobBatchRecord(final Record<?> record, final int partitionId) {
    final var currentIntent = record.getIntent();

    if (currentIntent == JobBatchIntent.ACTIVATED) {
      final var value = (JobBatchRecordValue) record.getValue();
      for (final long jobKey : value.getJobKeys()) {
        final var creationTime = jobCache.remove(jobKey);
        executionLatencyMetrics.observeJobActivationTime(
            partitionId, creationTime, record.getTimestamp());
      }
    }
  }

  private void cleanUp() {
    final var currentTimeMillis = ActorClock.currentTimeMillis();
    final var deadTime = currentTimeMillis - TIME_TO_LIVE.toMillis();
    processInstanceCache.cleanup(deadTime);
    jobCache.cleanup(deadTime);
    controller.scheduleCancellableTask(TIME_TO_LIVE, this::cleanUp);
  }

  public static ExporterCfg defaultConfig() {
    final ExporterCfg exporterCfg = new ExporterCfg();
    exporterCfg.setClassName(MetricsExporter.class.getName());
    return exporterCfg;
  }

  public static String defaultExporterId() {
    return MetricsExporter.class.getSimpleName();
  }

  private static boolean isProcessInstanceRecord(final Record<?> record) {
    final var recordValue = (ProcessInstanceRecordValue) record.getValue();
    return BpmnElementType.PROCESS == recordValue.getBpmnElementType();
  }
}
