/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH under
 * one or more contributor license agreements. See the NOTICE file distributed
 * with this work for additional information regarding copyright ownership.
 * Licensed under the Camunda License 1.0. You may not use this file
 * except in compliance with the Camunda License 1.0.
 */
package io.camunda.tasklist.schema.indices;

import io.camunda.tasklist.schema.backup.Prio4Backup;
import org.springframework.stereotype.Component;

@Component
public class TasklistWebSessionIndex extends AbstractIndexDescriptor implements Prio4Backup {

  public static final String ID = "id";
  public static final String CREATION_TIME = "creationTime";
  public static final String LAST_ACCESSED_TIME = "lastAccessedTime";
  public static final String MAX_INACTIVE_INTERVAL_IN_SECONDS = "maxInactiveIntervalInSeconds";
  public static final String ATTRIBUTES = "attributes";

  public static final String INDEX_NAME = "web-session";
  public static final String INDEX_VERSION = "1.1.0";

  @Override
  public String getIndexName() {
    return INDEX_NAME;
  }

  @Override
  public String getVersion() {
    return INDEX_VERSION;
  }
}
