/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. Licensed under a commercial license.
 * You may not use this file except in compliance with the commercial license.
 */

import React from 'react';
import {shallow} from 'enzyme';

import {updateEntity, createEntity, evaluateReport} from 'services';
import {nowDirty, nowPristine} from 'saveGuard';
import {EntityNameForm, InstanceCount, ReportRenderer} from 'components';

import {ReportEdit} from './ReportEdit';
import ReportControlPanel from './controlPanels/ReportControlPanel';
import ReportSelect from './controlPanels/ReportSelect';

jest.mock('services', () => {
  const rest = jest.requireActual('services');
  return {
    ...rest,
    evaluateReport: jest.fn(),
    updateEntity: jest.fn(),
    createEntity: jest.fn(),
    incompatibleFilters: jest.fn(),
  };
});

jest.mock('notifications', () => ({addNotification: jest.fn()}));
jest.mock('saveGuard', () => ({nowDirty: jest.fn(), nowPristine: jest.fn()}));

const report = {
  id: '1',
  name: 'name',
  lastModifier: 'lastModifier',
  lastModified: '2017-11-11T11:11:11.1111+0200',
  reportType: 'process',
  combined: false,
  data: {
    processDefinitionKey: 'aKey',
    processDefinitionVersions: ['aVersion'],
    tenantIds: [],
    configuration: {},
    view: {proeprty: 'rawData', entity: null},
    groupBy: {type: 'none', value: null},
    visualization: 'table',
  },
  result: {data: [1, 2, 3], instanceCount: 37},
};

const props = {
  report,
  mightFail: (promise, cb) => cb(promise),
  updateOverview: jest.fn(),
  location: {pathname: '/report/1'},
};

it('should show the instance count in the header if it is available', () => {
  const node = shallow(<ReportEdit {...props} />);

  expect(node.find(InstanceCount)).toExist();
});

it('should not contain a Control Panel in edit mode for a combined report', () => {
  const combinedReport = {
    combined: true,
    result: {
      data: {
        test: {
          data: {
            visualization: 'test',
          },
        },
      },
    },
  };

  const node = shallow(<ReportEdit {...props} report={{...report, ...combinedReport}} />);

  expect(node).not.toIncludeText('ControlPanel');
});

it('should contain a Control Panel in edit mode for a single report', () => {
  const node = shallow(<ReportEdit {...props} />);

  expect(node).toIncludeText('ControlPanel');
});

it('should contain a decision control panel in edit mode for decision reports', () => {
  const node = shallow(<ReportEdit {...props} report={{...report, reportType: 'decision'}} />);

  expect(node).toIncludeText('DecisionControlPanel');
});

it('should update the report', async () => {
  const node = shallow(<ReportEdit {...props} />);

  await node.instance().updateReport({visualization: {$set: 'customTestVis'}});

  expect(node.state().report.data.visualization).toBe('customTestVis');
});

it('should evaluate the report on mount if the config is complete, but the result is missing', async () => {
  evaluateReport.mockClear();
  evaluateReport.mockReturnValue(report);

  await shallow(<ReportEdit {...props} report={{...report, result: null}} />);

  expect(evaluateReport).toHaveBeenCalled();
});

it('should evaluate the report after updating', async () => {
  const node = shallow(<ReportEdit {...props} />);

  evaluateReport.mockReturnValue(report);
  await node.instance().updateReport({visualization: {$set: 'customTestVis'}}, true);

  expect(evaluateReport).toHaveBeenCalled();
});

it('should not evaluate the report if the view/groupBy/visualization setting is incomplete', async () => {
  const node = shallow(<ReportEdit {...props} />);

  evaluateReport.mockClear();
  await node.instance().updateReport({$unset: ['groupBy', 'visualization']}, true);

  expect(evaluateReport).not.toHaveBeenCalled();
});

it('should reset the report data to its original state after canceling', async () => {
  const node = shallow(<ReportEdit {...props} />);

  const dataBefore = node.state().report;

  await node.instance().updateReport({visualization: {$set: 'customTestVis'}});
  node.instance().cancel();

  expect(node.state().report).toEqual(dataBefore);
});

it('should save a changed report', async () => {
  const node = shallow(<ReportEdit {...props} />);

  await node.instance().save();

  expect(updateEntity).toHaveBeenCalled();
});

it('should reset name on cancel', async () => {
  const node = shallow(<ReportEdit {...props} />);

  node.setState({report: {...report, name: 'new Name'}});

  node.instance().cancel();

  expect(node.state().report.name).toBe('name');
});

it('should use original data as result data if report cant be evaluated on cancel', async () => {
  const node = shallow(<ReportEdit {...props} />);

  node.setState({
    originalData: {
      ...report,
      data: {
        processDefinitionKey: '123',
        configuration: {},
      },
    },
  });

  evaluateReport.mockReturnValueOnce(null);
  node.instance().cancel();

  expect(node.state().report.data.processDefinitionKey).toEqual('123');
});

it('should set conflict state when conflict happens on save button click', async () => {
  const conflictedItems = [{id: '1', name: 'alert', type: 'alert'}];

  const mightFail = (promise, cb, err) => err({status: 409, json: () => ({conflictedItems})});

  const node = shallow(<ReportEdit {...props} mightFail={mightFail} />);

  try {
    node.instance().save();
  } catch (e) {
    expect(node.state().conflict.type).toEqual('save');
    expect(node.state().conflict.items).toEqual(conflictedItems);
  }
});

it('should create a new report if the report is new', () => {
  const node = shallow(<ReportEdit {...props} isNew />);

  node.instance().save();

  expect(createEntity).toHaveBeenCalledWith('report/process/single', {
    collectionId: null,
    data: report.data,
    name: report.name,
  });
});

it('should create a new report in a collection', async () => {
  const node = await shallow(
    <ReportEdit
      {...props}
      location={{pathname: '/collection/123/report/new/edit'}}
      match={{params: {id: 'new'}}}
      isNew
    />
  );

  node.instance().save();

  expect(createEntity).toHaveBeenCalledWith('report/process/single', {
    collectionId: '123',
    data: report.data,
    name: report.name,
  });
});

it('should invoke updateOverview when saving the report', async () => {
  updateEntity.mockClear();
  updateEntity.mockReturnValue({});
  const spy = jest.fn();
  const node = shallow(<ReportEdit {...props} updateOverview={spy} />);

  await node.find(EntityNameForm).prop('onSave')();

  expect(spy).toHaveBeenCalled();
});

it('should notify the saveGuard of changes', () => {
  const node = shallow(<ReportEdit {...props} />);

  node.find(ReportControlPanel).prop('updateReport')({processDefinitionKey: {$set: 'b'}});

  expect(nowDirty).toHaveBeenCalled();

  node.find(ReportControlPanel).prop('updateReport')({processDefinitionKey: {$set: null}});

  expect(nowPristine).toHaveBeenCalled();
});

it('should only resolve the save promise if a decision for conflicts has been made', async () => {
  const mightFail = jest.fn().mockImplementation((promise, cb) => cb(promise));
  nowDirty.mockClear();
  const node = shallow(<ReportEdit {...props} mightFail={mightFail} />);

  mightFail.mockImplementationOnce((promise, cb, err) =>
    err({status: 409, json: () => ({conflictedItems: [{id: '1', name: 'alert', type: 'alert'}]})})
  );

  let promiseResolved = false;
  node
    .instance()
    .save()
    .then(() => (promiseResolved = true));

  await flushPromises();

  expect(promiseResolved).toBe(false);
  expect(node.state().conflict).not.toBe(null);

  node.find('ConflictModal').simulate('confirm');

  await flushPromises();

  expect(promiseResolved).toBe(true);
});

it('should disable the visualization Select if view or groupBy is not selected', () => {
  const node = shallow(
    <ReportEdit {...props} report={{...report, data: {...report.data, groupBy: null}}} />
  );

  expect(node.find(ReportSelect)).toBeDisabled();
});

it('should go back to a custom route after saving if provided as URL Search Param', async () => {
  const node = shallow(
    <ReportEdit
      {...props}
      location={{pathname: '/report/1', search: '?returnTo=/dashboard/1/edit'}}
    />
  );

  await node.find(EntityNameForm).prop('onSave')();

  expect(node.find('Redirect')).toExist();
  expect(node.find('Redirect').prop('to')).toBe('/dashboard/1/edit');
});

it('should go back to a custom route after canceling if provided as URL Search Param', async () => {
  const node = shallow(
    <ReportEdit
      {...props}
      location={{pathname: '/report/1', search: '?returnTo=/dashboard/1/edit'}}
    />
  );

  node.find(EntityNameForm).prop('onCancel')({preventDefault: jest.fn()});

  expect(node.find('Redirect')).toExist();
  expect(node.find('Redirect').prop('to')).toBe('/dashboard/1/edit');
});

it('should show loading indicator if specified by children components', () => {
  const node = shallow(<ReportEdit {...props} />);

  node.find(ReportControlPanel).prop('setLoading')(true);

  expect(node.find('LoadingIndicator')).toExist();

  node.find(ReportControlPanel).prop('setLoading')(false);

  expect(node.find('LoadingIndicator')).not.toExist();
});

it('should pass the error to reportRenderer if evaluation fails', async () => {
  const testError = {message: 'testError', reportDefinition: report};
  const mightFail = (promise, cb, err) => err({status: 400, json: () => testError});

  const node = shallow(<ReportEdit {...props} mightFail={mightFail} />);
  await node.instance().loadReport(undefined, report);

  expect(node.find(ReportRenderer).prop('error')).toEqual({status: 400, ...testError});
});
