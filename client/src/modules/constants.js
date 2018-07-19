export const DIRECTION = {
  UP: 'UP',
  DOWN: 'DOWN',
  RIGHT: 'RIGHT',
  LEFT: 'LEFT'
};

export const HEADER = 'HEADER';

export const FILTER_TYPES = {
  RUNNING: 'running',
  FINISHED: 'finished'
};

export const FILTER_SELECTION = {
  running: {
    active: true,
    incidents: true
  },
  incidents: {
    active: false,
    incidents: true
  },
  active: {
    active: true,
    incidents: false
  }
};

export const DEFAULT_FILTER = FILTER_SELECTION.running;

export const INCIDENTS_FILTER = FILTER_SELECTION.incidents;

export const INSTANCES_LABELS = {
  running: 'Running Instances',
  active: 'Active',
  incidents: 'Incidents',
  finished: 'Finished Instances',
  completed: 'Completed',
  canceled: 'Canceled'
};

export const INSTANCE_STATE = {
  ACTIVE: 'ACTIVE',
  COMPLETED: 'COMPLETED',
  CANCELED: 'CANCELED',
  INCIDENT: 'INCIDENT'
};

export const SORT_ORDER = {
  ASC: 'asc',
  DESC: 'desc'
};

export const DEFAULT_SORTING = {sortBy: 'id', sortOrder: SORT_ORDER.DESC};

export const PANE_ID = {
  TOP: 'TOP',
  BOTTOM: 'BOTTOM',
  LEFT: 'LEFT',
  RIGHT: 'RIGHT'
};

export const EXPAND_STATE = {
  DEFAULT: 'DEFAULT',
  EXPANDED: 'EXPANDED',
  COLLAPSED: 'COLLAPSED'
};

export const UNNAMED_ACTIVITY = 'Unnamed Activity';

export const ACTIVITY_TYPE = {
  TASK: 'TASK',
  EVENT: 'EVENT',
  GATEWAY: 'GATEWAY'
};

export const ACTIVITY_STATE = {
  ACTIVE: 'ACTIVE',
  COMPLETED: 'COMPLETED',
  TERMINATED: 'TERMINATED',
  INCIDENT: 'INCIDENT'
};

export const BADGE_TYPE = {
  FILTERS: 'filters',
  SELECTIONS: 'selections',
  SELECTIONHEAD: 'selectionHead',
  OPENSELECTIONHEAD: 'openSelectionHead',
  INCIDENTS: 'incidents',
  INSTANCES: 'instances'
};
