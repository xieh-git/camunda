/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. Licensed under a commercial license.
 * You may not use this file except in compliance with the commercial license.
 */

import React from 'react';
import {
  render,
  screen,
  within,
  waitForElementToBeRemoved,
} from '@testing-library/react';

import {CollapsablePanelProvider} from 'modules/contexts/CollapsablePanelContext';
import {ThemeProvider} from 'modules/theme/ThemeProvider';
import OperationsPanel from './index';
import * as CONSTANTS from './constants';
import {mockOperationFinished, mockOperationRunning} from './index.setup';
import {rest} from 'msw';
import {mockServer} from 'modules/mockServer';
import {operationsStore} from 'modules/stores/operations';

type Props = {
  children?: React.ReactNode;
};

const Wrapper = ({children}: Props) => {
  return (
    <ThemeProvider>
      <CollapsablePanelProvider>{children}</CollapsablePanelProvider>
    </ThemeProvider>
  );
};

describe('OperationsPanel', () => {
  afterEach(() => {
    operationsStore.reset();
  });

  it('should display empty panel on mount', async () => {
    mockServer.use(
      rest.post('/api/batch-operations', (_, res, ctx) =>
        res.once(ctx.json([]))
      )
    );

    render(<OperationsPanel />, {wrapper: Wrapper});

    expect(
      await screen.findByText(CONSTANTS.EMPTY_MESSAGE)
    ).toBeInTheDocument();
  });

  it('should render skeleton when loading', async () => {
    mockServer.use(
      rest.post('/api/batch-operations', (_, res, ctx) =>
        res.once(ctx.json([]))
      )
    );
    render(<OperationsPanel />, {wrapper: Wrapper});

    expect(screen.getByTestId('skeleton')).toBeInTheDocument();
    await waitForElementToBeRemoved(screen.getByTestId('skeleton'));
  });

  it('should render operation entries', async () => {
    mockServer.use(
      rest.post('/api/batch-operations', (_, res, ctx) =>
        res.once(
          ctx.status(200),
          ctx.json([mockOperationRunning, mockOperationFinished])
        )
      )
    );
    render(<OperationsPanel />, {wrapper: Wrapper});

    await waitForElementToBeRemoved(screen.getByTestId('skeleton'));

    const [firstOperation, secondOperation] = screen.getAllByTestId(
      'operations-entry'
    );

    expect(
      within(firstOperation).getByText(mockOperationRunning.id)
    ).toBeInTheDocument();
    expect(within(firstOperation).getByText('Retry')).toBeInTheDocument();
    expect(
      within(firstOperation).getByTestId('operation-retry-icon')
    ).toBeInTheDocument();

    expect(
      within(secondOperation).getByText(mockOperationFinished.id)
    ).toBeInTheDocument();
    expect(within(secondOperation).getByText('Cancel')).toBeInTheDocument();
    expect(
      within(secondOperation).getByTestId('operation-cancel-icon')
    ).toBeInTheDocument();
  });

  it('should show an error message', async () => {
    mockServer.use(
      rest.post('/api/batch-operations', (_, res, ctx) =>
        res.once(ctx.json([]), ctx.status(500))
      )
    );

    const {unmount} = render(<OperationsPanel />, {wrapper: Wrapper});

    expect(
      await screen.findByText('Operations could not be fetched')
    ).toBeInTheDocument();

    unmount();

    mockServer.use(
      rest.post('/api/batch-operations', (_, res) =>
        res.networkError('A network error')
      )
    );

    render(<OperationsPanel />, {wrapper: Wrapper});

    expect(
      await screen.findByText('Operations could not be fetched')
    ).toBeInTheDocument();
  });
});
