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
package io.camunda.zeebe.model.bpmn.builder;

import io.camunda.zeebe.model.bpmn.instance.zeebe.ZeebeTaskListener;
import io.camunda.zeebe.model.bpmn.instance.zeebe.ZeebeTaskListenerEventType;

public class TaskListenerBuilder {

  private final ZeebeTaskListener element;
  private final AbstractBaseElementBuilder<?, ?> elementBuilder;

  protected TaskListenerBuilder(
      final ZeebeTaskListener element, final AbstractBaseElementBuilder<?, ?> elementBuilder) {
    this.element = element;
    this.elementBuilder = elementBuilder;
  }

  public TaskListenerBuilder eventType(final ZeebeTaskListenerEventType eventType) {
    element.setEventType(eventType);
    return this;
  }

  public TaskListenerBuilder create() {
    return eventType(ZeebeTaskListenerEventType.create);
  }

  public TaskListenerBuilder update() {
    return eventType(ZeebeTaskListenerEventType.update);
  }

  public TaskListenerBuilder assign() {
    return eventType(ZeebeTaskListenerEventType.assign);
  }

  public TaskListenerBuilder complete() {
    return eventType(ZeebeTaskListenerEventType.complete);
  }

  public TaskListenerBuilder type(final String type) {
    element.setType(type);
    return this;
  }

  public TaskListenerBuilder typeExpression(final String typeExpression) {
    return type(elementBuilder.asZeebeExpression(typeExpression));
  }

  public TaskListenerBuilder retries(final String retries) {
    element.setRetries(retries);
    return this;
  }

  public TaskListenerBuilder retriesExpression(final String retriesExpression) {
    return retries(elementBuilder.asZeebeExpression(retriesExpression));
  }
}
