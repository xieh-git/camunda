/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. Licensed under a proprietary license.
 * See the License.txt file for more information. You may not use this file
 * except in compliance with the proprietary license.
 */
package io.camunda.operate.rest;

import io.camunda.operate.OperateProfileService;
import io.camunda.operate.connect.ElasticsearchConnector;
import io.camunda.operate.management.IndicesHealthIndicator;
import io.camunda.operate.property.OperateProperties;
import io.camunda.operate.rest.HealthCheckTest.AddManagementPropertiesInitializer;
import io.camunda.operate.schema.indices.OperateWebSessionIndex;
import io.camunda.operate.store.TaskStore;
import io.camunda.operate.store.elasticsearch.ElasticsearchTaskStore;
import io.camunda.operate.store.elasticsearch.RetryElasticsearchClient;
import io.camunda.operate.util.apps.nobeans.TestApplicationWithNoBeans;
import io.camunda.operate.webapp.security.SessionService;
import io.camunda.operate.webapp.security.WebSecurityConfig;
import io.camunda.operate.webapp.security.oauth2.CCSaaSJwtAuthenticationTokenValidator;
import io.camunda.operate.webapp.security.oauth2.Jwt2AuthenticationTokenConverter;
import io.camunda.operate.webapp.security.oauth2.OAuth2WebConfigurer;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.BDDMockito.given;

/**
 * Tests the health check with enabled authentication.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(
  classes = {
      OperateProperties.class,
      TestApplicationWithNoBeans.class,
      IndicesHealthIndicator.class,
      OAuth2WebConfigurer.class,
      Jwt2AuthenticationTokenConverter.class,
      CCSaaSJwtAuthenticationTokenValidator.class,
      WebSecurityConfig.class,
      ElasticsearchTaskStore.class,
      SessionService.class,
      RetryElasticsearchClient.class,
      OperateWebSessionIndex.class,
      OperateProfileService.class,
      ElasticsearchConnector.class
  },
  webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@ContextConfiguration(initializers = AddManagementPropertiesInitializer.class)
@ActiveProfiles(OperateProfileService.AUTH_PROFILE)
public class HealthCheckAuthenticationTest {

  @Autowired
  private TestRestTemplate testRestTemplate;

  @MockBean
  private IndicesHealthIndicator probes;

  @Autowired
  private TaskStore taskStore;

  @Test
  public void testHealthStateEndpointIsNotSecured() {
    given(probes.getHealth(anyBoolean())).willReturn(Health.up().build());

    final ResponseEntity<String> response = testRestTemplate.getForEntity("/actuator/health/liveness", String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
  }


  @Ignore // unless you have a reindex task in ELS for mentioned indices
  @Test
  public void testAccessElasticsearchTaskStatusFields() throws IOException {
    assertThat(taskStore.getRunningReindexTasksIdsFor("operate-flownode-instances-1.3.0_*", "operate-flownode-instance-8.2.0_")).isEmpty();
    assertThat(taskStore.getRunningReindexTasksIdsFor("operate-flownode-instance-1.3.0_*", "operate-flownode-instance-8.2.0_")).hasSize(1);
  }

}
