/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH under
 * one or more contributor license agreements. See the NOTICE file distributed
 * with this work for additional information regarding copyright ownership.
 * Licensed under the Camunda License 1.0. You may not use this file
 * except in compliance with the Camunda License 1.0.
 */
package io.camunda.it.auth;

import static io.camunda.zeebe.client.protocol.rest.PermissionTypeEnum.CREATE;
import static io.camunda.zeebe.client.protocol.rest.PermissionTypeEnum.READ;
import static io.camunda.zeebe.client.protocol.rest.ResourceTypeEnum.DECISION_DEFINITION;
import static io.camunda.zeebe.client.protocol.rest.ResourceTypeEnum.DECISION_REQUIREMENTS_DEFINITION;
import static io.camunda.zeebe.client.protocol.rest.ResourceTypeEnum.DEPLOYMENT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.camunda.application.Profile;
import io.camunda.it.utils.BrokerWithCamundaExporterITInvocationProvider;
import io.camunda.it.utils.ZeebeClientTestFactory.Authenticated;
import io.camunda.it.utils.ZeebeClientTestFactory.Permissions;
import io.camunda.it.utils.ZeebeClientTestFactory.User;
import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.command.ProblemException;
import io.camunda.zeebe.client.api.response.DeploymentEvent;
import java.time.Duration;
import java.util.List;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.api.function.Executable;

@TestInstance(Lifecycle.PER_CLASS)
class DecisionAuthorizationIT {

  private static final String DECISION_DEFINITION_ID_1 = "decision_1";
  private static final String DECISION_REQUIREMENTS_ID_1 = "definitions_1";
  private static final String DECISION_DEFINITION_ID_2 = "test_qa";
  private static final String DECISION_REQUIREMENTS_ID_2 = "definitions_test";
  private static final String ADMIN = "admin";
  private static final String RESTRICTED = "restricted-user";
  private static final User ADMIN_USER =
      new User(
          ADMIN,
          "password",
          List.of(
              new Permissions(DEPLOYMENT, CREATE, List.of("*")),
              new Permissions(DECISION_DEFINITION, READ, List.of("*")),
              new Permissions(DECISION_REQUIREMENTS_DEFINITION, READ, List.of("*"))));
  private static final User RESTRICTED_USER =
      new User(
          RESTRICTED,
          "password",
          List.of(
              new Permissions(DECISION_DEFINITION, READ, List.of(DECISION_DEFINITION_ID_1)),
              new Permissions(
                  DECISION_REQUIREMENTS_DEFINITION, READ, List.of(DECISION_REQUIREMENTS_ID_1))));

  @RegisterExtension
  static final BrokerWithCamundaExporterITInvocationProvider PROVIDER =
      new BrokerWithCamundaExporterITInvocationProvider()
          .withAdditionalProfiles(Profile.AUTH_BASIC)
          .withAuthorizationsEnabled()
          .withUsers(ADMIN_USER, RESTRICTED_USER);

  private boolean initialized;

  @BeforeEach
  void setUp(@Authenticated(ADMIN) final ZeebeClient adminClient) {
    if (!initialized) {
      final List<String> decisions = List.of("decision_model.dmn", "decision_model_1.dmn");
      decisions.forEach(
          decision -> deployResource(adminClient, String.format("decisions/%s", decision)));
      waitForDecisionDefinitionsToBeDeployed(adminClient, decisions.size());
      waitForDecisionRequirementsToBeDeployed(adminClient, decisions.size());
      initialized = true;
    }
  }

  @TestTemplate
  void shouldReturnAuthorizedDecisionDefinitions(
      @Authenticated(RESTRICTED) final ZeebeClient userClient) {
    // when
    final var decisionDefinitions = userClient.newDecisionDefinitionQuery().send().join().items();

    // then
    assertThat(decisionDefinitions).hasSize(1);
    assertThat(decisionDefinitions.stream().map(p -> p.getDmnDecisionId()).toList())
        .containsOnly(DECISION_DEFINITION_ID_1);
  }

  @TestTemplate
  void shouldReturnForbiddenForUnauthorizedDecisionDefinition(
      @Authenticated(ADMIN) final ZeebeClient adminClient,
      @Authenticated(RESTRICTED) final ZeebeClient userClient) {
    // given
    final var decisionDefinitionKey =
        getDecisionDefinitionKey(adminClient, DECISION_DEFINITION_ID_2);

    // when
    final Executable executeGet =
        () -> userClient.newDecisionDefinitionGetRequest(decisionDefinitionKey).send().join();

    // then
    final var problemException = assertThrows(ProblemException.class, executeGet);
    assertThat(problemException.code()).isEqualTo(403);
    assertThat(problemException.details().getDetail())
        .isEqualTo("Unauthorized to perform operation 'READ' on resource 'DECISION_DEFINITION'");
  }

  @TestTemplate
  void shouldReturnAuthorizedDecisionDefinition(
      @Authenticated(ADMIN) final ZeebeClient adminClient,
      @Authenticated(RESTRICTED) final ZeebeClient userClient) {
    // given
    final var decisionDefinitionKey =
        getDecisionDefinitionKey(adminClient, DECISION_DEFINITION_ID_1);

    // when
    final var decisionDefinition =
        userClient.newDecisionDefinitionGetRequest(decisionDefinitionKey).send().join();

    // then
    assertThat(decisionDefinition).isNotNull();
    assertThat(decisionDefinition.getDmnDecisionId()).isEqualTo(DECISION_DEFINITION_ID_1);
  }

  @TestTemplate
  void shouldReturnAuthorizedDecisionRequirements(
      @Authenticated(RESTRICTED) final ZeebeClient userClient) {
    // when
    final var decisionRequirements =
        userClient.newDecisionRequirementsQuery().send().join().items();

    // then
    assertThat(decisionRequirements).hasSize(1);
    assertThat(decisionRequirements.stream().map(p -> p.getDmnDecisionRequirementsId()).toList())
        .containsOnly(DECISION_REQUIREMENTS_ID_1);
  }

  @TestTemplate
  void shouldReturnForbiddenForUnauthorizedDecisionRequirements(
      @Authenticated(ADMIN) final ZeebeClient adminClient,
      @Authenticated(RESTRICTED) final ZeebeClient userClient) {
    // given
    final var decisionRequirementsKey =
        getDecisionRequirementsKey(adminClient, DECISION_REQUIREMENTS_ID_2);

    // when
    final Executable executeGet =
        () -> userClient.newDecisionRequirementsGetRequest(decisionRequirementsKey).send().join();

    // then
    final var problemException = assertThrows(ProblemException.class, executeGet);
    assertThat(problemException.code()).isEqualTo(403);
    assertThat(problemException.details().getDetail())
        .isEqualTo(
            "Unauthorized to perform operation 'READ' on resource 'DECISION_REQUIREMENTS_DEFINITION'");
  }

  @TestTemplate
  void shouldReturnAuthorizedDecisionRequirements(
      @Authenticated(ADMIN) final ZeebeClient adminClient,
      @Authenticated(RESTRICTED) final ZeebeClient userClient) {
    // given
    final var decisionRequirementsKey =
        getDecisionRequirementsKey(adminClient, DECISION_REQUIREMENTS_ID_1);

    // when
    final var decisionRequirements =
        userClient.newDecisionRequirementsGetRequest(decisionRequirementsKey).send().join();

    // then
    assertThat(decisionRequirements).isNotNull();
    assertThat(decisionRequirements.getDmnDecisionRequirementsId())
        .isEqualTo(DECISION_REQUIREMENTS_ID_1);
  }

  private long getDecisionDefinitionKey(
      final ZeebeClient client, final String decisionDefinitionId) {
    return client
        .newDecisionDefinitionQuery()
        .filter(f -> f.decisionDefinitionId(decisionDefinitionId))
        .send()
        .join()
        .items()
        .getFirst()
        .getDecisionKey();
  }

  private long getDecisionRequirementsKey(
      final ZeebeClient client, final String decisionRequirementsId) {
    return client
        .newDecisionRequirementsQuery()
        .filter(f -> f.decisionRequirementsId(decisionRequirementsId))
        .send()
        .join()
        .items()
        .getFirst()
        .getDecisionRequirementsKey();
  }

  private DeploymentEvent deployResource(final ZeebeClient zeebeClient, final String resourceName) {
    return zeebeClient
        .newDeployResourceCommand()
        .addResourceFromClasspath(resourceName)
        .send()
        .join();
  }

  private void waitForDecisionDefinitionsToBeDeployed(
      final ZeebeClient zeebeClient, final int expectedCount) {
    Awaitility.await("should deploy decision definitions and import in Operate")
        .atMost(Duration.ofSeconds(15))
        .ignoreExceptions() // Ignore exceptions and continue retrying
        .untilAsserted(
            () -> {
              final var result = zeebeClient.newDecisionDefinitionQuery().send().join();
              assertThat(result.items().size()).isEqualTo(expectedCount);
            });
  }

  private void waitForDecisionRequirementsToBeDeployed(
      final ZeebeClient zeebeClient, final int expectedCount) {
    Awaitility.await("should deploy decision requirements and import in Operate")
        .atMost(Duration.ofSeconds(15))
        .ignoreExceptions() // Ignore exceptions and continue retrying
        .untilAsserted(
            () -> {
              final var result = zeebeClient.newDecisionRequirementsQuery().send().join();
              assertThat(result.items().size()).isEqualTo(expectedCount);
            });
  }
}
