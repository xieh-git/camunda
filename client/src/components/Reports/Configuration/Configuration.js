import React from 'react';

import {Popover, Icon, Button} from 'components';
import * as visualizations from './visualizations';

import './Configuration.scss';

export default class Configuration extends React.Component {
  resetToDefaults = () => {
    const {defaults} = visualizations[this.props.type];
    if (defaults) {
      Object.keys(defaults).forEach(prop => {
        this.props.onChange(prop)(defaults[prop]);
      });
    }
  };

  render() {
    const Component = visualizations[this.props.type];
    return (
      <li className="Configuration">
        <Popover title={<Icon type="settings" />} disabled={!this.props.type}>
          <div className="content">
            {Component && (
              <Component configuration={this.props.configuration} onChange={this.props.onChange} />
            )}
            <Button className="resetButton" onClick={this.resetToDefaults}>
              Reset to Defaults
            </Button>
          </div>
        </Popover>
      </li>
    );
  }
}
