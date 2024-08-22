/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH under
 * one or more contributor license agreements. See the NOTICE file distributed
 * with this work for additional information regarding copyright ownership.
 * Licensed under the Camunda License 1.0. You may not use this file
 * except in compliance with the Camunda License 1.0.
 */

import {StructuredList as BaseStructuredList} from 'modules/components/StructuredList';
import styled from 'styled-components';

const Content = styled.div`
  position: relative;
  height: 100%;
  .cds--loading-overlay {
    position: absolute;
  }
`;

const StructuredList = styled(BaseStructuredList)`
  [role='table'] {
    table-layout: fixed;
  }
`;

const CellContainer = styled.div`
  padding: var(--cds-spacing-03) var(--cds-spacing-01);
`;

export {Content, StructuredList, CellContainer};
