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

import java.util.List;
import org.sonar.api.SonarRuntime;
import org.sonar.api.server.rule.RulesDefinition;
import org.sonarsource.analyzer.commons.RuleMetadataLoader;

public class MockRulesDefinition implements RulesDefinition {
  public static final String REPOSITORY_KEY = "cpp";
  public static final String REPOSITORY_NAME = "SonarLint";
  private static final String RESOURCE_FOLDER_FORMAT = "/necto/sonarlint/rules/%s";

  private final SonarRuntime sonarRuntime;

  public MockRulesDefinition(SonarRuntime sonarRuntime) {
    this.sonarRuntime = sonarRuntime;
  }

  public static class DefaultQualityProfile extends DefaultQualityProfileDefinition {
    public DefaultQualityProfile() {
      super(REPOSITORY_KEY, MockCppLanguage.KEY);
    }
  }

  @Override
  public void define(Context context) {
    NewRepository repository = context.createRepository(REPOSITORY_KEY, MockCppLanguage.KEY).setName(REPOSITORY_NAME);
    String resourcePath = resourcePath(MockCppLanguage.KEY);
    String defaultProfilePath = DefaultQualityProfileDefinition.profilePath(REPOSITORY_KEY, MockCppLanguage.KEY);
    RuleMetadataLoader ruleMetadataLoader = new RuleMetadataLoader(resourcePath, defaultProfilePath, sonarRuntime);
    ruleMetadataLoader.addRulesByRuleKey(repository, List.of("S99"));
    repository.done();
  }

  public static String resourcePath(String language) {
    return String.format(RESOURCE_FOLDER_FORMAT, language);
  }
}
