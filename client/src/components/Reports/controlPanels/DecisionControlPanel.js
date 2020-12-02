/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. Licensed under a commercial license.
 * You may not use this file except in compliance with the commercial license.
 */

import React from 'react';
import equal from 'deep-equal';

import {DefinitionSelection} from 'components';
import {DecisionFilter} from 'filter';
import {
  loadInputVariables,
  loadOutputVariables,
  reportConfig,
  loadDecisionDefinitionXml,
} from 'services';
import {t} from 'translation';

import ReportSelect from './ReportSelect';

const {decision: decisionConfig} = reportConfig;

export default class DecisionControlPanel extends React.Component {
  state = {
    variables: {
      inputVariable: [],
      outputVariable: [],
    },
  };

  componentDidMount() {
    this.loadVariables();
  }

  componentDidUpdate(prevProps) {
    const {data} = this.props.report;
    const {data: prevData} = prevProps.report;

    if (
      data.decisionDefinitionKey !== prevData.decisionDefinitionKey ||
      !equal(data.decisionDefinitionVersions, prevData.decisionDefinitionVersions) ||
      !equal(data.tenantIds, prevData.tenantIds)
    ) {
      this.loadVariables();
    }
  }

  loadVariables = async () => {
    const {decisionDefinitionKey, decisionDefinitionVersions, tenantIds} = this.props.report.data;
    if (decisionDefinitionKey && decisionDefinitionVersions && tenantIds) {
      const payload = {decisionDefinitionKey, decisionDefinitionVersions, tenantIds};
      this.setState({
        variables: {
          inputVariable: await loadInputVariables(payload),
          outputVariable: await loadOutputVariables(payload),
        },
      });
    }
  };

  changeDefinition = async ({key, versions, tenantIds, name}) => {
    const {groupBy, filter} = this.props.report.data;

    const change = {
      decisionDefinitionKey: {$set: key},
      decisionDefinitionName: {$set: name},
      decisionDefinitionVersions: {$set: versions},
      tenantIds: {$set: tenantIds},
      configuration: {
        tableColumns: {
          $set: {
            includeNewVariables: true,
            includedColumns: [],
            excludedColumns: [],
          },
        },
        columnOrder: {
          $set: {
            inputVariables: [],
            instanceProps: [],
            outputVariables: [],
            variables: [],
          },
        },
        xml: {
          $set:
            key && versions && versions[0]
              ? await loadDecisionDefinitionXml(key, versions[0], tenantIds[0])
              : null,
        },
      },
      filter: {
        $set: filter.filter(({type}) => type !== 'inputVariable' && type !== 'outputVariable'),
      },
    };

    if (groupBy && (groupBy.type === 'inputVariable' || groupBy.type === 'outputVariable')) {
      change.groupBy = {$set: null};
      change.visualization = {$set: null};
    }

    this.props.updateReport(change, true);
  };

  updateReport = (type, newValue) => {
    this.props.updateReport(decisionConfig.update(type, newValue, this.props), true);
  };

  render() {
    const {data, result} = this.props.report;
    const {
      decisionDefinitionKey,
      decisionDefinitionVersions,
      tenantIds,
      filter,
      configuration: {xml},
    } = data;

    return (
      <div className="DecisionControlPanel ReportControlPanel">
        <div className="select source">
          <h3 className="sectionTitle">{t('common.dataSource')}</h3>
          <DefinitionSelection
            type="decision"
            definitionKey={decisionDefinitionKey}
            versions={decisionDefinitionVersions}
            tenants={tenantIds}
            xml={xml}
            onChange={this.changeDefinition}
          />
        </div>
        <div className="scrollable">
          <div className="filter">
            <DecisionFilter
              data={filter}
              onChange={this.props.updateReport}
              decisionDefinitionKey={decisionDefinitionKey}
              decisionDefinitionVersions={decisionDefinitionVersions}
              tenants={tenantIds}
              variables={this.state.variables}
            />
          </div>
          <div className="reportSetup">
            <h3 className="sectionTitle">{t('report.reportSetup')}</h3>
            <ul>
              {['view', 'groupBy'].map((field, idx, fields) => {
                const previous = fields
                  .filter((prev, prevIdx) => prevIdx < idx)
                  .map((prev) => data[prev]);

                return (
                  <li className="select" key={field}>
                    <span className="label">{t(`report.${field}.label`)}</span>
                    <ReportSelect
                      type="decision"
                      field={field}
                      report={this.props.report}
                      value={data[field]}
                      variables={this.state.variables}
                      previous={previous}
                      disabled={!decisionDefinitionKey || previous.some((entry) => !entry)}
                      onChange={(newValue) => this.updateReport(field, newValue)}
                    />
                  </li>
                );
              })}
            </ul>
          </div>
          {result && typeof result.instanceCount !== 'undefined' && (
            <div className="instanceCount">
              {t(
                `report.instanceCount.decision.label${result.instanceCount !== 1 ? '-plural' : ''}`,
                {
                  count: result.instanceCount,
                }
              )}
            </div>
          )}
        </div>
      </div>
    );
  }
}
