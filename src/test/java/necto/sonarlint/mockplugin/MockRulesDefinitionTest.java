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

import org.junit.jupiter.api.Test;
import org.sonar.api.SonarRuntime;
import org.sonar.api.internal.SonarRuntimeImpl;
import org.sonar.api.server.profile.BuiltInQualityProfilesDefinition;
import org.sonar.api.server.rule.RulesDefinition;
import org.sonar.api.utils.Version;
import necto.sonarlint.mockplugin.MockRulesDefinition;

import static org.assertj.core.api.Assertions.assertThat;

class MockRulesDefinitionTest {

  @Test
  void define_rules() {
    SonarRuntime sonarRuntime = SonarRuntimeImpl.forSonarLint(Version.parse("7.2.1.58118"));
    MockRulesDefinition rulesDefinition = new MockRulesDefinition(sonarRuntime);
    RulesDefinition.Context context = new RulesDefinition.Context();
    rulesDefinition.define(context);

    RulesDefinition.Repository repository = context.repository("cpp");
    assertThat(repository).isNotNull();
    assertThat(repository.name()).isEqualTo("SonarLint");
    assertThat(repository.language()).isEqualTo("cpp");
    assertThat(repository.rules()).hasSize(1);
  }

  @Test
  void define_default_profile() {
    BuiltInQualityProfilesDefinition.Context context = new BuiltInQualityProfilesDefinition.Context();
    BuiltInQualityProfilesDefinition profileDefinition = new MockRulesDefinition.DefaultQualityProfile();
    profileDefinition.define(context);
    BuiltInQualityProfilesDefinition.BuiltInQualityProfile profile = context.profile("cpp", "Default");
    assertThat(profile.language()).isEqualTo("cpp");
    assertThat(profile.name()).isEqualTo("Default");
    assertThat(profile.rules()).hasSize(1);
  }

}
