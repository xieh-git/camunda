/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH under
 * one or more contributor license agreements. See the NOTICE file distributed
 * with this work for additional information regarding copyright ownership.
 * Licensed under the Camunda License 1.0. You may not use this file
 * except in compliance with the Camunda License 1.0.
 */
package io.camunda.zeebe.logstreams.impl.log;

import com.netflix.concurrency.limits.Limit;
import io.camunda.zeebe.logstreams.impl.LogStreamMetrics;
import io.camunda.zeebe.logstreams.impl.Loggers;
import io.camunda.zeebe.logstreams.impl.flowcontrol.FlowControl;
import io.camunda.zeebe.logstreams.impl.flowcontrol.RateLimit;
import io.camunda.zeebe.logstreams.log.LogRecordAwaiter;
import io.camunda.zeebe.logstreams.log.LogStream;
import io.camunda.zeebe.logstreams.log.LogStreamReader;
import io.camunda.zeebe.logstreams.log.LogStreamWriter;
import io.camunda.zeebe.logstreams.storage.LogStorage;
import io.camunda.zeebe.logstreams.storage.LogStorage.CommitListener;
import java.util.Collection;
import java.util.concurrent.CopyOnWriteArrayList;
import org.slf4j.Logger;

public final class LogStreamImpl implements LogStream, CommitListener {

  private static final Logger LOG = Loggers.LOGSTREAMS_LOGGER;

  private final Collection<LogStreamReader> readers = new CopyOnWriteArrayList<>();
  private final Collection<LogRecordAwaiter> recordAwaiters = new CopyOnWriteArrayList<>();

  private final String logName;
  private final int partitionId;
  private final LogStorage logStorage;
  private final LogStreamMetrics logStreamMetrics;
  private final FlowControl flowControl;
  private final Sequencer sequencer;

  LogStreamImpl(
      final String logName,
      final int partitionId,
      final int maxFragmentSize,
      final LogStorage logStorage,
      final Limit requestLimit,
      final RateLimit writeRateLimit) {
    this.logName = logName;
    this.partitionId = partitionId;
    this.logStorage = logStorage;
    logStreamMetrics = new LogStreamMetrics(partitionId);
    flowControl = new FlowControl(logStreamMetrics, requestLimit, writeRateLimit);
    sequencer =
        new Sequencer(
            logStorage,
            getWriteBuffersInitialPosition(),
            maxFragmentSize,
            new SequencerMetrics(partitionId),
            flowControl);
    logStorage.addCommitListener(this);
  }

  @Override
  public void close() {
    LOG.info("Closing {} with {} readers", logName, readers.size());
    readers.forEach(LogStreamReader::close);
    logStorage.removeCommitListener(this);
    logStreamMetrics.remove();
  }

  @Override
  public int getPartitionId() {
    return partitionId;
  }

  @Override
  public String getLogName() {
    return logName;
  }

  @Override
  public LogStreamReader newLogStreamReader() {
    return createLogStreamReader();
  }

  @Override
  public LogStreamWriter newLogStreamWriter() {
    return sequencer;
  }

  @Override
  public FlowControl getFlowControl() {
    return flowControl;
  }

  @Override
  public void registerRecordAvailableListener(final LogRecordAwaiter recordAwaiter) {
    recordAwaiters.add(recordAwaiter);
  }

  @Override
  public void removeRecordAvailableListener(final LogRecordAwaiter recordAwaiter) {
    recordAwaiters.remove(recordAwaiter);
  }

  private void notifyRecordAwaiters() {
    recordAwaiters.forEach(LogRecordAwaiter::onRecordAvailable);
  }

  @Override
  public void onCommit() {
    notifyRecordAwaiters();
  }

  private LogStreamReader createLogStreamReader() {
    final var newReader = new LogStreamReaderImpl(logStorage.newReader());
    readers.add(newReader);
    return newReader;
  }

  private long getWriteBuffersInitialPosition() {
    final long initialPosition;
    final long lastPosition = getLastCommittedPosition();
    if (lastPosition > 0) {
      initialPosition = lastPosition + 1;
    } else {
      initialPosition = 1;
    }

    return initialPosition;
  }

  private long getLastCommittedPosition() {
    try (final var storageReader = logStorage.newReader();
        final var logStreamReader = new LogStreamReaderImpl(storageReader)) {
      return logStreamReader.seekToEnd();
    }
  }
}
