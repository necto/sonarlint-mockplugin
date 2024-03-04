/*
 * SonarLint Mock Plugin
 * Copyright (C) 2023-2023 Arseniy Zaostrovnykh
 * arseniy AT zaostrovnykh DOT ch
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package necto.sonarlint.mockplugin;

import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.rule.CheckFactory;
import org.sonar.api.batch.sensor.Sensor;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.internal.DefaultSensorDescriptor;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.api.utils.log.LogTesterJUnit5;

import static org.assertj.core.api.Assertions.assertThat;
import static necto.sonarlint.mockplugin.TestUtils.asString;
import static necto.sonarlint.mockplugin.TestUtils.defaultSensorContext;
import static necto.sonarlint.mockplugin.TestUtils.inputFile;

class MockSensorTest {

  @RegisterExtension
  LogTesterJUnit5 logTester = new LogTesterJUnit5();

  @Test
  void describe() {
    DefaultSensorDescriptor descriptor = new DefaultSensorDescriptor();
    sensor(defaultSensorContext()).describe(descriptor);

    assertThat(descriptor.name()).isEqualTo("MockSensor");
    assertThat(descriptor.languages()).containsExactlyInAnyOrder("c", "cpp");
    assertThat(descriptor.isProcessesFilesIndependently()).isTrue();
    assertThat(descriptor.ruleRepositories()).containsExactlyInAnyOrder("c", "cpp");
    assertThat(logTester.logs()).isEmpty();
  }

  @Test
  void shouldNotStartAnalysisWhenNoFileToAnalyze() {
    SensorContextTester context = defaultSensorContext();
    sensor(context).execute(context);

    assertThat(context.allIssues()).isEmpty();
    assertThat(logTester.logs()).isEmpty();
  }

  @Test
  void validCheckOnValidFileShouldRaiseIssue() {
    InputFile inputFile = inputFile("foo");
    SensorContextTester context = defaultSensorContext();
    analyse(sensor(context), context, inputFile);

    assertThat(asString(context.allIssues())).containsExactly("cpp:S99 [1:0-1:3] Bang Bang Issue!");
    assertThat(logTester.logs()).containsExactly(
      "1 source file to be analyzed",
      "1/1 source file has been analyzed");
  }

  private void analyse(Sensor sensor, SensorContextTester context, InputFile... inputFiles) {
    for (InputFile inputFile : inputFiles) {
      context.fileSystem().add(inputFile);
    }
    sensor.execute(context);
  }

  @Test
  void stopOnCancellation() {
    SensorContextTester context = defaultSensorContext();
    context.setCancelled(true);
    analyse(sensor(context), context, inputFile("{}"));
    assertThat(context.allIssues()).isEmpty();
    assertThat(logTester.logs()).containsExactly(
      "1 source file to be analyzed");
  }

  @Test
  void shouldNotRaiseAnIssueOrErrorWhenTheInputFileDoesNotExist() {
    SensorContextTester context = defaultSensorContext();
    context.fileSystem().add(inputFile(Path.of("invalid-path.txt")));

    sensor(context).execute(context);

    assertThat(context.allIssues()).isEmpty();
    assertThat(logTester.logs()).anyMatch(log -> log.startsWith("Unable to analyze file") && log.contains("invalid-path.txt"));
  }

  private static MockSensor sensor(SensorContext sensorContext) {
    return new MockSensor(new CheckFactory(sensorContext.activeRules()));
  }

}
