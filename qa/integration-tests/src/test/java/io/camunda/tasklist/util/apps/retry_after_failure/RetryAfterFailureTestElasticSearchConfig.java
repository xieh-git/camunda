/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. Licensed under a proprietary license.
 * See the License.txt file for more information. You may not use this file
 * except in compliance with the proprietary license.
 */
package io.camunda.tasklist.util.apps.retry_after_failure;

import io.camunda.tasklist.exceptions.PersistenceException;
import io.camunda.tasklist.zeebe.ImportValueType;
import io.camunda.tasklist.zeebeimport.ImportBatch;
import io.camunda.tasklist.zeebeimport.v840.processors.es.BulkProcessorElasticSearch;
import java.util.HashSet;
import java.util.Set;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * Let's mock ElasticsearchBulkProcessor, so that it throw an exception with the 2st run and persist
 * the data only with the second run.
 */
@Configuration
public class RetryAfterFailureTestElasticSearchConfig {

  @Bean("io.camunda.tasklist.zeebeimport.v830.processors.es.ElasticsearchBulkProcessor")
  @Primary
  public CustomElasticsearchBulkProcessor elasticsearchBulkProcessor() {
    return new CustomElasticsearchBulkProcessor();
  }

  public static class CustomElasticsearchBulkProcessor extends BulkProcessorElasticSearch {

    private Set<ImportValueType> alreadyFailedTypes = new HashSet<>();

    @Override
    public void performImport(ImportBatch importBatchElasticSearch) throws PersistenceException {
      final ImportValueType importValueType = importBatchElasticSearch.getImportValueType();
      if (!alreadyFailedTypes.contains(importValueType)) {
        alreadyFailedTypes.add(importValueType);
        throw new PersistenceException(
            String.format(
                "Fake exception when saving data of type %s to Elasticsearch", importValueType));
      } else {
        super.performImport(importBatchElasticSearch);
      }
    }

    public void cancelAttempts() {
      alreadyFailedTypes.clear();
    }
  }
}
