/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH under
 * one or more contributor license agreements. See the NOTICE file distributed
 * with this work for additional information regarding copyright ownership.
 * Licensed under the Camunda License 1.0. You may not use this file
 * except in compliance with the Camunda License 1.0.
 */
package io.camunda.operate.schema.indices;

import io.camunda.operate.conditions.DatabaseInfoProvider;
import io.camunda.operate.schema.backup.Prio4Backup;

public class ProcessIndex extends AbstractIndexDescriptor implements Prio4Backup {

  public static final String INDEX_NAME = "process";
  public static final String ID = "id";
  public static final String KEY = "key";
  public static final String BPMN_PROCESS_ID = "bpmnProcessId";
  public static final String NAME = "name";
  public static final String VERSION = "version";
  public static final String BPMN_XML = "bpmnXml";
  public static final String RESOURCE_NAME = "resourceName";
  public static final String FLOWNODES = "flowNodes";
  public static final String FLOWNODE_ID = "id";
  public static final String FLOWNODE_NAME = "name";

  public ProcessIndex(final String indexPrefix, final DatabaseInfoProvider databaseInfoProvider) {
    super(indexPrefix, databaseInfoProvider);
  }

  @Override
  public String getIndexName() {
    return INDEX_NAME;
  }

  @Override
  public String getVersion() {
    return "8.3.0";
  }
}