/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package org.gradle.api.internal.dependencies.maven;

import java.io.*;

import org.gradle.api.dependencies.maven.MavenPom;

public final class DefaultPomModuleDescriptorFileWriter implements PomModuleDescriptorFileWriter {
    PomModuleDescriptorWriter pomModuleDescriptorWriter;

    public DefaultPomModuleDescriptorFileWriter(PomModuleDescriptorWriter pomModuleDescriptorWriter) {
        this.pomModuleDescriptorWriter = pomModuleDescriptorWriter;
    }

    public void write(MavenPom pom, File output) {
        if (output.getParentFile() != null) {
            output.getParentFile().mkdirs();
        }
        PrintWriter out = null;
        try {
            out = new PrintWriter(new OutputStreamWriter(new FileOutputStream(output),
                    "UTF-8"));
            pomModuleDescriptorWriter.convert(pom, out);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } finally {
            if (out != null) {
                out.close();
            }
        }
    }
}