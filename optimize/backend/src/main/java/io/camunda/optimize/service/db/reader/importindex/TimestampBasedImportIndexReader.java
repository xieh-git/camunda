/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH under
 * one or more contributor license agreements. See the NOTICE file distributed
 * with this work for additional information regarding copyright ownership.
 * Licensed under the Camunda License 1.0. You may not use this file
 * except in compliance with the Camunda License 1.0.
 */
package io.camunda.optimize.service.db.reader.importindex;

import static io.camunda.optimize.service.db.DatabaseConstants.TIMESTAMP_BASED_IMPORT_INDEX_NAME;

import io.camunda.optimize.dto.optimize.datasource.IngestedDataSourceDto;
import io.camunda.optimize.dto.optimize.index.TimestampBasedImportIndexDto;
import io.camunda.optimize.service.db.repository.ImportRepository;
import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@AllArgsConstructor
public class TimestampBasedImportIndexReader
    implements ImportIndexReader<TimestampBasedImportIndexDto, IngestedDataSourceDto> {

  final ImportRepository importRepository;

  @Override
  public Optional<TimestampBasedImportIndexDto> getImportIndex(
      final String typeIndexComesFrom, final IngestedDataSourceDto dataSourceDto) {
    return importRepository.getImportIndex(
        TIMESTAMP_BASED_IMPORT_INDEX_NAME,
        "timestamp based",
        TimestampBasedImportIndexDto.class,
        typeIndexComesFrom,
        dataSourceDto);
  }

  public List<TimestampBasedImportIndexDto> getAllImportIndicesForTypes(
      final List<String> indexTypes) {
    return importRepository.getAllTimestampBasedImportIndicesForTypes(indexTypes);
  }
}