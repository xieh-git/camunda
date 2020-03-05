/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. Licensed under a commercial license.
 * You may not use this file except in compliance with the commercial license.
 */
package org.camunda.optimize.service.security;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.camunda.optimize.dto.optimize.IdentityDto;
import org.camunda.optimize.dto.optimize.IdentityType;
import org.camunda.optimize.dto.optimize.query.event.EventProcessRoleDto;
import org.camunda.optimize.service.EventProcessRoleService;
import org.camunda.optimize.service.IdentityService;
import org.camunda.optimize.service.util.configuration.ConfigurationService;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@AllArgsConstructor
@Component
public class EventProcessAuthorizationService {
  private final ConfigurationService configurationService;
  private final EventProcessRoleService eventProcessRoleService;
  private final IdentityService identityService;

  public boolean hasEventProcessManagementAccess(@NonNull final String userId) {
    return configurationService.getEventBasedProcessAccessUserIds().contains(userId);
  }

  public boolean isAuthorizedToEventProcess(@NonNull final String userId, @NonNull final String eventProcessMappingId) {
    boolean isAuthorized = false;

    final List<EventProcessRoleDto<IdentityDto>> roles = eventProcessRoleService.getRoles(eventProcessMappingId);
    final Map<IdentityType, Set<String>> groupAndUserRoleIdentityIds = roles.stream()
      .map(EventProcessRoleDto::getIdentity)
      .collect(Collectors.groupingBy(
        IdentityDto::getType,
        Collectors.mapping(IdentityDto::getId, Collectors.toSet())
      ));
    final Set<String> roleGroupIds = groupAndUserRoleIdentityIds
      .getOrDefault(IdentityType.GROUP, Collections.emptySet());
    final Set<String> roleUserIds = groupAndUserRoleIdentityIds
      .getOrDefault(IdentityType.USER, Collections.emptySet());

    if (!roleGroupIds.isEmpty()) {
      // if there are groups check if the user is member of those
      final Set<String> allGroupIdsOfUser = identityService.getAllGroupsOfUser(userId)
        .stream()
        .map(IdentityDto::getId)
        .collect(Collectors.toSet());
      isAuthorized = allGroupIdsOfUser.stream().anyMatch(roleGroupIds::contains);
    }

    if (!isAuthorized) {
      // if not authorized yet check the user roles
      isAuthorized = roleUserIds.stream().anyMatch(userId::equals);
    }

    return isAuthorized;
  }

}
