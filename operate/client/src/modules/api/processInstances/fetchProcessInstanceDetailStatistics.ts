/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH under
 * one or more contributor license agreements. See the NOTICE file distributed
 * with this work for additional information regarding copyright ownership.
 * Licensed under the Camunda License 1.0. You may not use this file
 * except in compliance with the Camunda License 1.0.
 */

import {requestAndParse} from 'modules/request';

type ProcessInstanceDetailStatisticsDto = {
  activityId: string;
  active: number;
  canceled: number;
  incidents: number;
  completed: number;
};

const fetchProcessInstanceDetailStatistics = async (
  processInstanceId: ProcessInstanceEntity['id'],
  options?: Parameters<typeof requestAndParse>[1],
) => {
  return requestAndParse<ProcessInstanceDetailStatisticsDto[]>(
    {
      url: `/api/process-instances/${processInstanceId}/statistics`,
    },
    options,
  );
};

export {fetchProcessInstanceDetailStatistics};
export type {ProcessInstanceDetailStatisticsDto};
