/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. Licensed under a proprietary license.
 * See the License.txt file for more information. You may not use this file
 * except in compliance with the proprietary license.
 */

import {get, put} from 'request';

export async function loadProcesses(sortBy, sortOrder) {
  const params = {};
  if (sortBy && sortOrder) {
    params.sortBy = sortBy;
    params.sortOrder = sortOrder;
  }

  const response = await get('api/process/overview', params);
  return await response.json();
}

export function updateOwner(processDefinitionKey, id) {
  return put(`api/process/${processDefinitionKey}/owner`, {id});
}

export async function loadManagementDashboard() {
  const response = await get(`api/dashboard/management`);

  return await response.json();
}
