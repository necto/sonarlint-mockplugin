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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.sonar.api.batch.fs.FilePredicate;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.rule.CheckFactory;
import org.sonar.api.batch.sensor.Sensor;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.SensorDescriptor;
import org.sonar.api.batch.sensor.issue.NewIssue;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.sonarsource.analyzer.commons.ProgressReport;
import java.io.InputStreamReader;
import org.sonar.api.rule.RuleKey;

public class MockSensor implements Sensor {
  private static final Logger LOG = Loggers.get(MockSensor.class);
  private static final FilePredicate ANY_FILE_PREDICATE = inputFile -> true;

  public MockSensor(CheckFactory checkFactory) {
  }

  @Override
  public void describe(SensorDescriptor descriptor) {
    descriptor
      .name("MockSensor")
      .onlyOnLanguages(new String[]{"c", "cpp"})
      .createIssuesForRuleRepositories("c", "cpp")
      .processesFilesIndependently();
  }

  @Override
  public void execute(SensorContext sensorContext) {
    FilePredicate filePredicate = ANY_FILE_PREDICATE;
    List<InputFile> inputFiles = getInputFiles(sensorContext, filePredicate);
    if (inputFiles.isEmpty()) {
      return;
    }

    List<String> filenames = inputFiles.stream().map(InputFile::toString).collect(Collectors.toList());
    ProgressReport progressReport = new ProgressReport("Progress of the text and secrets analysis", TimeUnit.SECONDS.toMillis(10));
    progressReport.start(filenames);
    boolean cancelled = false;
    try {
      for (InputFile inputFile : inputFiles) {
        if (sensorContext.isCancelled()) {
          cancelled = true;
          break;
        }
        analyze(sensorContext, inputFile);
        progressReport.nextFile();
      }
    } finally {
      if (cancelled) {
        progressReport.cancel();
      } else {
        progressReport.stop();
      }
    }

  }

  /**
   * In SonarLint context we want to analyze all non-binary input files, even when they are not analyzed or assigned to a language.
   * To avoid analyzing all non-binary files to reduce time and memory consumption in a non SonarLint context only files assigned to a
   * language are analyzed.
   */
  private static List<InputFile> getInputFiles(SensorContext sensorContext, FilePredicate filePredicate) {
    List<InputFile> inputFiles = new ArrayList<>();
    FileSystem fileSystem = sensorContext.fileSystem();
    for (InputFile inputFile : fileSystem.inputFiles(filePredicate)) {
      inputFiles.add(inputFile);
    }
    return inputFiles;
  }

  public final RuleKey ruleKey = RuleKey.of(MockRulesDefinition.REPOSITORY_KEY, "S99");

  private void analyze(SensorContext sensorContext, InputFile inputFile) {
    try {
      List<String> contentLines = new ArrayList<>();
      try (InputStream in = inputFile.inputStream()) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(in, inputFile.charset()));
        String line = reader.readLine();
        while (line != null) {
          contentLines.add(line);
          line = reader.readLine();
        }
      }
      if (!contentLines.isEmpty()) {
        NewIssue issue = sensorContext.newIssue();
        issue
            .forRule(ruleKey)
            .at(issue.newLocation()
                .on(inputFile)
                .at(inputFile.selectLine(1))
                .message("Bang Bang Issue!"))
            .save();
      }
    } catch (IOException | RuntimeException e) {
      logAnalysisError(sensorContext, inputFile, e);
    }
  }

  private static void logAnalysisError(SensorContext sensorContext, InputFile inputFile, Exception e) {
    String message = String.format("Unable to analyze file %s: %s", inputFile, e.getMessage());
    sensorContext.newAnalysisError()
      .message(message)
      .onFile(inputFile)
      .save();
    LOG.warn(message);
    LOG.debug(e.toString());
  }

}
