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

import org.sonar.api.server.profile.BuiltInQualityProfilesDefinition;
import org.sonarsource.analyzer.commons.BuiltInQualityProfileJsonLoader;

public class DefaultQualityProfileDefinition implements BuiltInQualityProfilesDefinition {

  public static final String NAME = "Default";
  public static final String FILE_NAME = "Default_profile.json";

  public final String repositoryKey;
  public final String languageKey;

  public DefaultQualityProfileDefinition(String repositoryKey, String languageKey) {
    this.repositoryKey = repositoryKey;
    this.languageKey = languageKey;
  }

  public void define(BuiltInQualityProfilesDefinition.Context context) {
    BuiltInQualityProfilesDefinition.NewBuiltInQualityProfile profile = context.createBuiltInQualityProfile(NAME, languageKey);
    BuiltInQualityProfileJsonLoader.load(profile, repositoryKey, profilePath(repositoryKey, languageKey));
    profile.setDefault(true);
    profile.done();
  }

  public static String profilePath(String repository, String language) {
    return MockRulesDefinition.resourcePath(language) + "/" + FILE_NAME;
  }

}
