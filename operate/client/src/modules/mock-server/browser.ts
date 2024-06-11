/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH under
 * one or more contributor license agreements. See the NOTICE file distributed
 * with this work for additional information regarding copyright ownership.
 * Licensed under the Camunda License 1.0. You may not use this file
 * except in compliance with the Camunda License 1.0.
 */

import {setupWorker} from 'msw';
import {handlers} from './handlers';

function startMocking() {
  const worker = setupWorker(...handlers);

  worker.stop();

  if (handlers.length > 0) {
    worker.start({onUnhandledRequest: 'bypass'});
  }
}

export {startMocking};