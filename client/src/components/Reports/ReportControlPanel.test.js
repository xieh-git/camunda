import React from 'react';
import {shallow} from 'enzyme';

import ReportControlPanel from './ReportControlPanel';
import {extractProcessDefinitionName, reportConfig, getFlowNodeNames} from 'services';

import {loadVariables} from './service';

const flushPromises = () => new Promise(resolve => setImmediate(resolve));

jest.mock('services', () => {
  return {
    reportConfig: {
      getLabelFor: () => 'foo',
      view: {foo: {data: 'foo', label: 'viewfoo'}},
      groupBy: {
        foo: {data: 'foo', label: 'groupbyfoo'},
        variable: {data: {value: []}, label: 'Variables'}
      },
      visualization: {foo: {data: 'foo', label: 'visualizationfoo'}},
      isAllowed: jest.fn().mockReturnValue(true),
      getNext: jest.fn()
    },
    extractProcessDefinitionName: jest.fn(),
    formatters: {
      getHighlightedText: text => text
    },
    getFlowNodeNames: jest.fn().mockReturnValue({
      a: 'foo',
      b: 'bar'
    })
  };
});

jest.mock('./service', () => {
  return {
    loadVariables: jest.fn().mockReturnValue([])
  };
});

const data = {
  processDefinitionKey: 'aKey',
  processDefinitionVersion: 'aVersion',
  view: {operation: 'count', entity: 'processInstance'},
  groupBy: {type: 'none', unit: null},
  visualization: 'number',
  filter: null,
  configuration: {xml: 'fooXml'}
};

extractProcessDefinitionName.mockReturnValue('foo');
const spy = jest.fn();

it('should call the provided updateReport property function when a setting changes', () => {
  const node = shallow(<ReportControlPanel {...data} updateReport={spy} />);

  node.instance().update('visualization', 'someTestVis');

  expect(spy).toHaveBeenCalled();
  expect(spy.mock.calls[0][0].visualization).toBe('someTestVis');
});

it('should toggle target value view mode off when a setting changes', () => {
  const node = shallow(<ReportControlPanel {...data} updateReport={spy} />);

  node.instance().update('visualization', 'someTestVis');

  expect(spy.mock.calls[0][0].configuration.targetValue).toBe(null);
});

it('should disable the groupBy and visualization Selects if view is not selected', () => {
  const node = shallow(<ReportControlPanel {...data} view="" />);

  expect(node.find('.configDropdown').at(1)).toBeDisabled();
  expect(node.find('.configDropdown').at(2)).toBeDisabled();
});

it('should not disable the groupBy and visualization Selects if view is selected', () => {
  const node = shallow(<ReportControlPanel {...data} />);

  expect(node.find('.configDropdown').at(1)).not.toBeDisabled();
  expect(node.find('.configDropdown').at(2)).not.toBeDisabled();
});

it('should set or reset following selects according to the getNext function', () => {
  const node = shallow(<ReportControlPanel {...data} updateReport={spy} />);

  reportConfig.getNext.mockReturnValueOnce('next');
  node.instance().update('view', 'foo');

  expect(spy).toHaveBeenCalledWith({
    configuration: {targetValue: null, xml: 'fooXml'},
    view: 'foo',
    groupBy: 'next'
  });
});

it('should disable options, which would create wrong combination', () => {
  reportConfig.isAllowed.mockReturnValue(false);
  const node = shallow(<ReportControlPanel {...data} onChange={spy} />);
  node.setProps({view: 'baz'});

  expect(
    node
      .find('Dropdown')
      .at(1)
      .find('DropdownOption')
  ).toBeDisabled();
});

it('should show process definition name', async () => {
  extractProcessDefinitionName.mockReturnValue('aName');

  const node = await shallow(<ReportControlPanel {...data} />);

  expect(node.find('.processDefinitionPopover').prop('title')).toContain('aName');
});

it('should change process definition name if process definition is updated', async () => {
  const node = await shallow(<ReportControlPanel {...data} />);

  extractProcessDefinitionName.mockReturnValue('aName');
  node.setProps({processDefinitionKey: 'bar'});

  expect(node.find('.processDefinitionPopover').prop('title')).toContain('aName');
});

it('should load the variables of the process', () => {
  const node = shallow(<ReportControlPanel {...data} />);

  node.setProps({processDefinitionKey: 'bar', processDefinitionVersion: 'ALL'});

  expect(loadVariables).toHaveBeenCalledWith('bar', 'ALL');
});

it('should include variables in the groupby options', () => {
  const node = shallow(<ReportControlPanel {...data} />);

  node.setState({variables: [{name: 'Var1'}, {name: 'Var2'}]});

  const varDropdown = node.find('[label="Group by"] Submenu DropdownOption');

  expect(varDropdown.at(0).prop('children')).toBe('Var1');
  expect(varDropdown.at(1).prop('children')).toBe('Var2');
});

it('should only include variables that match the typeahead', () => {
  const node = shallow(<ReportControlPanel {...data} />);

  node.setState({
    variables: [{name: 'Foo'}, {name: 'Bar'}, {name: 'Foobar'}],
    variableTypeaheadValue: 'foo'
  });

  const varDropdown = node.find('[label="Group by"] Submenu DropdownOption');

  expect(varDropdown).toHaveLength(2);
  expect(varDropdown.at(0).prop('children')).toBe('Foo');
  expect(varDropdown.at(1).prop('children')).toBe('Foobar');
});

it('should show pagination for many variables', () => {
  const node = shallow(<ReportControlPanel {...data} />);

  node.setState({
    variables: [
      {name: 'varA'},
      {name: 'varB'},
      {name: 'varC'},
      {name: 'varD'},
      {name: 'varE'},
      {name: 'varF'},
      {name: 'varG'}
    ]
  });

  const varDropdown = node.find('[label="Group by"] Submenu DropdownOption');

  expect(varDropdown).toHaveLength(5);
  expect(node.find('.loadMore')).toBePresent();
});

it('should show an "Always show tooltips" button for heatmaps', () => {
  const node = shallow(<ReportControlPanel {...data} visualization="heat" />);

  expect(node).toIncludeText('Always show tooltips');
});

it('should not show an "Always show tooltips" button for other visualizations', () => {
  const node = shallow(<ReportControlPanel {...data} visualization="something" />);

  expect(node).not.toIncludeText('Always show tooltips');
});

it('should load the flownode names and hand them to the filter and process part', async () => {
  const node = shallow(
    <ReportControlPanel {...data} view={{entity: 'processInstance', property: 'duration'}} />
  );

  await flushPromises();
  node.update();

  expect(getFlowNodeNames).toHaveBeenCalled();
  expect(node.find('Filter').prop('flowNodeNames')).toEqual(getFlowNodeNames());
  expect(node.find('ProcessPart').prop('flowNodeNames')).toEqual(getFlowNodeNames());
});

it('should only display process part button if view is process instance duration', () => {
  const node = shallow(
    <ReportControlPanel {...data} view={{entity: 'processInstance', property: 'duration'}} />
  );

  expect(node.find('ProcessPart')).toBePresent();

  node.setProps({view: {entity: 'processInstance', property: 'frequency'}});

  expect(node.find('ProcessPart')).not.toBePresent();
});

it('should not update the target value when changing from line chart to barchart or the reverse', () => {
  const spy = jest.fn();
  const node = shallow(
    <ReportControlPanel
      {...data}
      visualization="bar"
      view={{entity: 'processInstance', property: 'duration'}}
      updateReport={spy}
    />
  );

  node.instance().update('visualization', 'line');
  expect(spy).toHaveBeenCalledWith({
    groupBy: null,
    visualization: null
  });
});

it('should reset the target value when changing from line chart or barchart to something else', () => {
  const spy = jest.fn();
  const node = shallow(
    <ReportControlPanel
      {...data}
      visualization="bar"
      view={{entity: 'processInstance', property: 'duration'}}
      updateReport={spy}
    />
  );

  node.instance().update('visualization', 'something else');
  expect(spy).toHaveBeenCalledWith({
    configuration: {targetValue: null, xml: 'fooXml'},
    groupBy: null,
    visualization: null
  });
});
