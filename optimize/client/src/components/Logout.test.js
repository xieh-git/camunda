/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. Licensed under a proprietary license.
 * See the License.txt file for more information. You may not use this file
 * except in compliance with the proprietary license.
 */

import React, {runLastEffect} from 'react';

import {shallow} from 'enzyme';

import {Logout} from './Logout';
import {get} from 'request';
import {addNotification} from 'notifications';

jest.mock('request', () => ({get: jest.fn()}));
jest.mock('notifications', () => ({addNotification: jest.fn()}));

const props = {
  mightFail: jest.fn(),
  history: {replace: jest.fn()},
};

it('should logout from server', () => {
  shallow(<Logout {...props} />);
  runLastEffect();

  expect(get).toHaveBeenCalledWith('api/authentication/logout');
});

it('should redirect to the index page', async () => {
  props.history.replace.mockClear();
  shallow(<Logout {...props} mightFail={(_, cb) => cb()} />);
  runLastEffect();

  await flushPromises();

  expect(props.history.replace).toHaveBeenCalledWith('/');
});

it('should show an error if the logout fails', async () => {
  props.history.replace.mockClear();
  addNotification.mockClear();
  shallow(<Logout {...props} mightFail={(_, cb, fail) => fail()} />);
  runLastEffect();

  await flushPromises();

  expect(props.history.replace).toHaveBeenCalledWith('/');
  expect(addNotification).toHaveBeenCalled();
  expect(addNotification.mock.calls[0][0].type).toBe('error');
});