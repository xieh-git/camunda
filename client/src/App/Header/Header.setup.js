/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. Licensed under a commercial license.
 * You may not use this file except in compliance with the commercial license.
 */

import {createInstance} from 'modules/testUtils';
import {PATHNAME} from './constants';

export const dataStore = {
  running: 0,
  active: 0,
  withIncidents: 0,
  filterCount: null,
  instancesInSelectionsCount: 0,
  selectionCount: 0
};

export const dataStoreWithCount = {
  running: 100,
  active: 80,
  withIncidents: 20,
  filterCount: null,
  instancesInSelectionsCount: 0,
  selectionCount: 0
};

export const location = {
  dashboard: {
    pathname: PATHNAME.DASHBOAD
  },
  instances: {
    pathname: PATHNAME.INSTANCES
  },
  instance: {
    pathname: PATHNAME.INSTANCE
  }
};

export const mockInstance = createInstance();
