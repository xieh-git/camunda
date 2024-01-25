/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. Licensed under a proprietary license.
 * See the License.txt file for more information. You may not use this file
 * except in compliance with the proprietary license.
 */

import {render, screen, fireEvent, waitFor} from 'modules/testing-library';
import {MemoryRouter} from 'react-router-dom';
import {MockThemeProvider} from 'modules/theme/MockProvider';
import {generateTask} from 'modules/mock-schema/mocks/tasks';
import {Component} from './index';
import {http, HttpResponse} from 'msw';
import {nodeMockServer} from 'modules/mockServer/nodeMockServer';
import * as userMocks from 'modules/mock-schema/mocks/current-user';
import {QueryClientProvider} from '@tanstack/react-query';
import {getMockQueryClient} from 'modules/react-query/getMockQueryClient';

const FIRST_PAGE = Array.from({length: 50}).map((_, index) =>
  generateTask(`${index}`),
);
const SECOND_PAGE = Array.from({length: 50}).map((_, index) =>
  generateTask(`${index + 50}`),
);

function getWrapper(
  initialEntries: React.ComponentProps<
    typeof MemoryRouter
  >['initialEntries'] = ['/'],
) {
  const mockClient = getMockQueryClient();

  type Props = {
    children?: React.ReactNode;
  };

  const Wrapper: React.FC<Props> = ({children}) => {
    return (
      <QueryClientProvider client={mockClient}>
        <MemoryRouter initialEntries={initialEntries}>
          <MockThemeProvider>{children}</MockThemeProvider>
        </MemoryRouter>
      </QueryClientProvider>
    );
  };

  return Wrapper;
}

describe('<Tasks />', () => {
  it('should load more tasks', async () => {
    nodeMockServer.use(
      http.get(
        '/v1/internal/users/current',
        () => {
          return HttpResponse.json(userMocks.currentUser);
        },
        {once: true},
      ),
      http.post<never, {searchAfter: [string, string]}>(
        '/v1/tasks/search',
        async ({request}) => {
          const {searchAfter} = await request.json();
          if (searchAfter === undefined) {
            return HttpResponse.json(FIRST_PAGE);
          }

          return HttpResponse.json(SECOND_PAGE);
        },
      ),
    );

    render(<Component />, {
      wrapper: getWrapper(),
    });

    await waitFor(() => expect(screen.getByTitle('All open')).toBeDisabled());

    expect(await screen.findByText('TASK 0')).toBeInTheDocument();
    expect(screen.getByText('TASK 49')).toBeInTheDocument();
    expect(screen.getAllByRole('article')).toHaveLength(50);

    fireEvent.scroll(screen.getByTestId('scrollable-list'), {
      target: {scrollY: 100},
    });

    expect(screen.getByText('TASK 0')).toBeInTheDocument();
    expect(screen.getByText('TASK 49')).toBeInTheDocument();
    expect(await screen.findByText('TASK 50')).toBeInTheDocument();
    expect(screen.getByText('TASK 99')).toBeInTheDocument();
    expect(screen.getAllByRole('article')).toHaveLength(100);
  });

  it('should use tasklist api raw filters', async () => {
    nodeMockServer.use(
      http.get('/v1/internal/users/current', () => {
        return HttpResponse.json(userMocks.currentUser);
      }),
      http.post<never, {candidateUser: string; foo: unknown}>(
        '/v1/tasks/search',
        async ({request}) => {
          const {candidateUser, foo} = await request.json();

          if (candidateUser === 'demo' && foo === undefined) {
            return HttpResponse.json(FIRST_PAGE);
          }

          return HttpResponse.error();
        },
      ),
    );

    render(<Component />, {
      wrapper: getWrapper(['/?candidateUser=demo&foo=bar']),
    });

    expect(await screen.findByText('TASK 0')).toBeInTheDocument();
    expect(screen.queryByText('No tasks found')).not.toBeInTheDocument();
  });
});
