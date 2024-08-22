/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH under
 * one or more contributor license agreements. See the NOTICE file distributed
 * with this work for additional information regarding copyright ownership.
 * Licensed under the Camunda License 1.0. You may not use this file
 * except in compliance with the Camunda License 1.0.
 */

import {
  IS_LISTENERS_TAB_SUPPORTED,
  IS_VERSION_TAG_ENABLED,
} from 'modules/feature-flags';
import {mockListeners} from 'modules/mocks/mockListeners';
import {RequestHandler, rest} from 'msw';

const processVersionTagHandler = IS_VERSION_TAG_ENABLED
  ? [
      rest.get('/api/processes/:processId', async (req, res, ctx) => {
        const response = await ctx.fetch(req);
        const body = await response.json();

        return res(
          ctx.json({
            ...body,
            versionTag: 'myVersionTag',
          }),
        );
      }),
    ]
  : [];

const listenersHandler = IS_LISTENERS_TAB_SUPPORTED
  ? [
      rest.post(
        '/api/process-instances/:instanceId/listeners',
        async (req, res, ctx) => {
          const body: {pageSize: number; flowNodeId: string} = await req.json();

          if (body.flowNodeId.includes('start')) {
            return res(ctx.json(mockListeners));
          }
          return res(ctx.json([]));
        },
      ),
    ]
  : [];

const handlers: RequestHandler[] = [
  ...listenersHandler,
  ...processVersionTagHandler,
];

export {handlers};
