/*
 * Copyright © 2017 camunda services GmbH (info@camunda.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.camunda.zeebe.client.impl.search.sort;

import io.camunda.zeebe.client.api.search.sort.IncidentSort;
import io.camunda.zeebe.client.impl.search.query.SearchQuerySortBase;

public class IncidentSortImpl extends SearchQuerySortBase<IncidentSort> implements IncidentSort {

  @Override
  public IncidentSort key() {
    return field("key");
  }

  @Override
  public IncidentSort processDefinitionKey() {
    return field("processDefinitionKey");
  }

  @Override
  public IncidentSort processInstanceKey() {
    return field("processInstanceKey");
  }

  @Override
  public IncidentSort type() {
    return field("type");
  }

  @Override
  public IncidentSort flowNodeId() {
    return field("flowNodeId");
  }

  @Override
  public IncidentSort flowNodeInstanceId() {
    return field("flowNodeInstanceId");
  }

  @Override
  public IncidentSort creationTime() {
    return field("creationTime");
  }

  @Override
  public IncidentSort state() {
    return field("state");
  }

  @Override
  public IncidentSort jobKey() {
    return field("jobKey");
  }

  @Override
  public IncidentSort tenantId() {
    return field("tenantId");
  }

  @Override
  protected IncidentSort self() {
    return this;
  }
}
