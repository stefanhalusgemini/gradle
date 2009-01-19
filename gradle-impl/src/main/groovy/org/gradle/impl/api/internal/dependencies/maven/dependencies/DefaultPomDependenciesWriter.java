/*
 * Copyright 2007-2008 the original author or authors.
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
package org.gradle.impl.api.internal.dependencies.maven.dependencies;

import org.apache.ivy.core.module.descriptor.DependencyDescriptor;
import org.gradle.api.dependencies.maven.MavenPom;
import org.gradle.impl.api.internal.dependencies.maven.PomWriter;
import org.gradle.impl.api.internal.dependencies.maven.XmlHelper;

import java.io.PrintWriter;
import java.util.List;

/**
 * @author Hans Dockter
 */
public class DefaultPomDependenciesWriter implements PomDependenciesWriter {
    private PomDependenciesConverter dependenciesConverter;

    public DefaultPomDependenciesWriter(PomDependenciesConverter dependenciesConverter) {
        this.dependenciesConverter = dependenciesConverter;
    }

    public void convert(MavenPom pom, List<DependencyDescriptor> dependencies, PrintWriter printWriter) {
        List<MavenDependency> mavenDependencies = dependenciesConverter.convert(pom, dependencies);
        if (mavenDependencies.size() == 0) {
            return;
        }
        printWriter.println(XmlHelper.openTag(PomWriter.DEFAULT_INDENT, DEPENDENCIES));
        for (MavenDependency mavenDependency : mavenDependencies) {
            mavenDependency.write(printWriter);
        }
        printWriter.println(XmlHelper.closeTag(PomWriter.DEFAULT_INDENT, DEPENDENCIES));
    }

    public PomDependenciesConverter getDependenciesConverter() {
        return dependenciesConverter;
    }
}
