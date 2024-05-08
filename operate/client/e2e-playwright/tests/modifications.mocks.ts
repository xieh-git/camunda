/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH under
 * one or more contributor license agreements. See the NOTICE file distributed
 * with this work for additional information regarding copyright ownership.
 * Licensed under the Camunda License 1.0. You may not use this file
 * except in compliance with the Camunda License 1.0.
 */

import {deployProcess, createSingleInstance} from '../setup-utils';

export async function setup() {
  await deployProcess(['withoutIncidentsProcess_v_1.bpmn']);

  const instanceWithoutAnIncident = await createSingleInstance(
    'withoutIncidentsProcess',
    1,
    {
      test: 123,
      foo: 'bar',
    },
  );

  return {
    instanceWithoutAnIncident,
  };
}
