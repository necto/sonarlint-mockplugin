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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import org.sonar.api.SonarEdition;
import org.sonar.api.SonarProduct;
import org.sonar.api.SonarQubeSide;
import org.sonar.api.SonarRuntime;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.TextRange;
import org.sonar.api.batch.fs.internal.TestInputFileBuilder;
import org.sonar.api.batch.rule.internal.ActiveRulesBuilder;
import org.sonar.api.batch.rule.internal.DefaultActiveRules;
import org.sonar.api.batch.rule.internal.NewActiveRule;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.api.batch.sensor.issue.Issue;
import org.sonar.api.batch.sensor.issue.IssueLocation;
import org.sonar.api.internal.SonarRuntimeImpl;
import org.sonar.api.utils.Version;

import static java.nio.charset.StandardCharsets.UTF_8;

public class TestUtils {

  private static final Version VERSION = Version.create(9, 9);
  public static final SonarRuntime SONARLINT_RUNTIME = SonarRuntimeImpl.forSonarLint(VERSION);
  public static final SonarRuntime SONARQUBE_RUNTIME = SonarRuntimeImpl.forSonarQube(VERSION, SonarQubeSide.SERVER, SonarEdition.COMMUNITY);

  public static List<String> asString(Collection<Issue> issues) {
    return issues.stream().map(TestUtils::asString).collect(Collectors.toList());
  }

  public static String asString(Issue issue) {
    IssueLocation location = issue.primaryLocation();
    TextRange range = location.textRange();
    return String.format("%s [%d:%d-%d:%d] %s", issue.ruleKey(), range.start().line(), range.start().lineOffset(),
      range.end().line(), range.end().lineOffset(), location.message());
  }

  public static InputFile inputFile(String fileContent) {
    return inputFile(Path.of("file.txt"), fileContent);
  }

  public static InputFile inputFile(Path path) {
    String content = null;
    try {
      content = Files.readString(path, UTF_8);
    } catch (IOException e) {
      // ignored, so InputFile.inputStream() will fail
    }
    return inputFile(path, content);
  }

  public static InputFile inputFile(Path path, @Nullable String content) {
    return  inputFile( path, content, null);
  }

  public static InputFile inputFile(Path path, @Nullable String content, @Nullable String language) {
    TestInputFileBuilder builder = new TestInputFileBuilder(".", path.toString())
      .setType(InputFile.Type.MAIN)
      .setLanguage(language)
      .setCharset(UTF_8);
    if (content != null) {
      builder.setContents(content);
    }
    return builder.build();
  }

  public static DefaultActiveRules activeRules() {
    ActiveRulesBuilder builder = new ActiveRulesBuilder();
    return builder.build();
  }

  public static SensorContextTester defaultSensorContext() {
    return sensorContext();
  }

  public static SensorContextTester sensorContext() {
    return sensorContext(new File(".").getAbsoluteFile());
  }

  /**
   * By default, the SonarLint runtime is chosen because it allows to analyze all files and not only files assigned to a language
   */
  public static SensorContextTester sensorContext(File baseDir) {
    return SensorContextTester.create(baseDir)
      .setRuntime(SONARLINT_RUNTIME)
      .setActiveRules(activeRules());
  }

}
