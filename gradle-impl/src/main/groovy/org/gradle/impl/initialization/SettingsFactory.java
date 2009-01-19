/*
 * Copyright 2007 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gradle.impl.initialization;

import org.gradle.StartParameter;
import org.gradle.api.internal.SettingsInternal;
import org.gradle.groovy.scripts.ScriptSource;
import org.gradle.impl.api.internal.dependencies.DependencyManagerFactory;
import org.gradle.initialization.IProjectDescriptorRegistry;

import java.io.File;
import java.util.Map;

/**
 * @author Hans Dockter
 */
public class SettingsFactory {
    private IProjectDescriptorRegistry projectDescriptorRegistry;
    private DependencyManagerFactory dependencyManagerFactory;
    private BuildSourceBuilder buildSourceBuilder;

    public SettingsFactory(IProjectDescriptorRegistry projectDescriptorRegistry,
                           DependencyManagerFactory dependencyManagerFactory, BuildSourceBuilder buildSourceBuilder) {
        this.projectDescriptorRegistry = projectDescriptorRegistry;
        this.dependencyManagerFactory = dependencyManagerFactory;
        this.buildSourceBuilder = buildSourceBuilder;
    }

    public SettingsInternal createSettings(File settingsDir, ScriptSource settingsScript,
                                           Map<String, String> gradleProperties, StartParameter startParameter) {
        DefaultSettings settings = new DefaultSettings(dependencyManagerFactory, projectDescriptorRegistry,
                buildSourceBuilder, settingsDir, settingsScript, startParameter);
        settings.getAdditionalProperties().putAll(gradleProperties);
        return settings;
    }
}
