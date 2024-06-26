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
package io.camunda.process.test.impl.extension;

import io.camunda.process.test.api.CamundaProcessTestContext;
import io.camunda.process.test.impl.containers.CamundaContainer;
import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.ZeebeClientBuilder;
import java.net.URI;
import java.util.function.Consumer;

public class CamundaProcessTestContextImpl implements CamundaProcessTestContext {

  private final CamundaContainer camundaContainer;
  private final Consumer<ZeebeClient> clientCreationCallback;

  public CamundaProcessTestContextImpl(
      final CamundaContainer camundaContainer, final Consumer<ZeebeClient> clientCreationCallback) {
    this.camundaContainer = camundaContainer;
    this.clientCreationCallback = clientCreationCallback;
  }

  @Override
  public ZeebeClient createZeebeClient() {
    return createZeebeClient(builder -> {});
  }

  @Override
  public ZeebeClient createZeebeClient(final Consumer<ZeebeClientBuilder> modifier) {
    final ZeebeClientBuilder builder =
        ZeebeClient.newClientBuilder()
            .usePlaintext()
            .grpcAddress(getCamundaGrpcAddress())
            .restAddress(getCamundaRestAddress());

    modifier.accept(builder);

    final ZeebeClient client = builder.build();
    clientCreationCallback.accept(client);

    return client;
  }

  @Override
  public URI getCamundaGrpcAddress() {
    return camundaContainer.getGrpcApiAddress();
  }

  @Override
  public URI getCamundaRestAddress() {
    return camundaContainer.getRestApiAddress();
  }
}
