/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH under one or more contributor license agreements.
 * Licensed under a proprietary license. See the License.txt file for more information.
 * You may not use this file except in compliance with the proprietary license.
 */
package org.camunda.optimize.service.db.es.report;

import org.camunda.optimize.dto.optimize.RoleType;
import org.camunda.optimize.dto.optimize.query.report.ReportDefinitionDto;
import org.camunda.optimize.service.DefinitionService;
import org.camunda.optimize.service.report.ReportService;
import org.camunda.optimize.service.security.ReportAuthorizationService;
import org.camunda.optimize.service.variable.ProcessVariableService;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class AuthorizationCheckReportEvaluationHandler extends ReportEvaluationHandler {

  private final ReportAuthorizationService authorizationService;

  public AuthorizationCheckReportEvaluationHandler(final ReportService reportService,
                                                   final SingleReportEvaluator singleReportEvaluator,
                                                   final CombinedReportEvaluator combinedReportEvaluator,
                                                   final ReportAuthorizationService authorizationService,
                                                   final ProcessVariableService processVariableService,
                                                   final DefinitionService definitionService) {
    super(reportService, singleReportEvaluator, combinedReportEvaluator, processVariableService, definitionService);
    this.authorizationService = authorizationService;
  }

  @Override
  protected Optional<RoleType> getAuthorizedRole(final String userId,
                                                 final ReportDefinitionDto report) {
    return authorizationService.getAuthorizedRole(userId, report);
  }
}
