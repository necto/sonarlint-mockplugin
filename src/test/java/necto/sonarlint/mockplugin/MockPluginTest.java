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
import org.sonar.api.Plugin;
import org.sonar.api.SonarEdition;
import org.sonar.api.SonarQubeSide;
import org.sonar.api.SonarRuntime;
import org.sonar.api.internal.SonarRuntimeImpl;
import org.sonar.api.utils.Version;

import static org.assertj.core.api.Assertions.assertThat;

class MockPluginTest {

  private static final Version VERSION_8_9 = Version.create(8, 9);

  @Test
  void define_extensions() {
    SonarRuntime runtime = SonarRuntimeImpl.forSonarQube(VERSION_8_9, SonarQubeSide.SCANNER, SonarEdition.COMMUNITY);
    Plugin.Context context = new Plugin.Context(runtime);
    Plugin plugin = new MockPlugin();
    plugin.define(context);
    assertThat(context.getExtensions()).hasSize(4);
  }
}
