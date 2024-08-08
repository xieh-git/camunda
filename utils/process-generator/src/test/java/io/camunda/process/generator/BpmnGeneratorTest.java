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
package io.camunda.process.generator;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import io.camunda.zeebe.model.bpmn.Bpmn;
import java.awt.Desktop;
import java.io.File;
import java.io.FileWriter;
import org.junit.jupiter.api.Test;

public class BpmnGeneratorTest {

  @Test
  void shouldGenerateProcess() {
    // given
    final var generator = new BpmnGenerator();

    // when
    final var generatedProcess = generator.generateProcess();

    // then
    assertThat(generatedProcess).isNotNull();
  }

  @Test
  void shouldGenerateARandomProcess() throws Exception {
    // given
    final var generator = new BpmnGenerator();

    // when
    final var generatedProcess = generator.generateProcess();

    // then
    assertThat(generatedProcess).isNotNull();

    System.out.println(Bpmn.convertToString(generatedProcess.process()));
    System.out.println("----------");
    System.out.printf("Seed: %sL%n", generatedProcess.seed());

    //    Uncomment to open the generated process in the modeler automatically

    final File tempFile = File.createTempFile("temp", ".bpmn");
    final FileWriter writer = new FileWriter(tempFile);
    writer.write(Bpmn.convertToString(generatedProcess.process()));
    writer.close();
    Desktop.getDesktop().open(tempFile);
  }
}