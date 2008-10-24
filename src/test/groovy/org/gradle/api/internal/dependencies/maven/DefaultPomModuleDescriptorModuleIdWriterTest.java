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
package org.gradle.api.internal.dependencies.maven;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.assertEquals;
import org.gradle.api.dependencies.maven.MavenPom;
import org.jmock.integration.junit4.JMock;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.jmock.Expectations;

import java.io.StringWriter;
import java.io.PrintWriter;

/**
 * @author Hans Dockter
 */
@RunWith(JMock.class)
public class DefaultPomModuleDescriptorModuleIdWriterTest {
    private DefaultPomModuleDescriptorModuleIdWriter moduleIdConverter;
    private StringWriter stringWriter;
    private static final String NL = System.getProperty("line.separator");
    private static final String TEST_ORG = "testOrg";
    private static final String TEST_NAME = "testName";
    private static final String TEST_REVISION = "testRevision";
    private static final String TEST_CLASSIFIER = "testClassifier";
    private static final String TEST_PACKAGING = "testPackaging";

    private JUnit4Mockery context = new JUnit4Mockery();

    @Before
    public void setUp() {
        stringWriter = new StringWriter();
        moduleIdConverter = new DefaultPomModuleDescriptorModuleIdWriter();
    }

    @Test
    public void convert() {
        final MavenPom pom = context.mock(MavenPom.class);
        context.checking(new Expectations() {{
            allowing(pom).getArtifactId(); will(returnValue(TEST_NAME));
            allowing(pom).getGroupId(); will(returnValue(TEST_ORG));
            allowing(pom).getVersion(); will(returnValue(TEST_REVISION));
            allowing(pom).getPackaging(); will(returnValue(TEST_PACKAGING));
            allowing(pom).getClassifier(); will(returnValue(TEST_CLASSIFIER));
        }});
        moduleIdConverter.convert(pom, new PrintWriter(stringWriter));

        assertEquals(createAllText(TEST_ORG, TEST_NAME, TEST_REVISION, TEST_PACKAGING, TEST_CLASSIFIER), stringWriter.toString());
    }

    @Test
    public void convertWithNullValuesForOptionals() {
        final MavenPom pom = context.mock(MavenPom.class);
        context.checking(new Expectations() {{
            allowing(pom).getArtifactId(); will(returnValue(TEST_NAME));
            allowing(pom).getGroupId(); will(returnValue(TEST_ORG));
            allowing(pom).getVersion(); will(returnValue(null));
            allowing(pom).getPackaging(); will(returnValue(null));
            allowing(pom).getClassifier(); will(returnValue(null));
        }});
        moduleIdConverter.convert(pom, new PrintWriter(stringWriter));
        assertEquals(createBaseText(TEST_ORG, TEST_NAME), stringWriter.toString());
    }

    private String createBaseText(String org, String name) {
        return XmlHelper.enclose(PomModuleDescriptorWriter.DEFAULT_INDENT, PomModuleDescriptorWriter.GROUP_ID, org) + NL +
                XmlHelper.enclose(PomModuleDescriptorWriter.DEFAULT_INDENT, PomModuleDescriptorWriter.ARTIFACT_ID, name) + NL;
    }

    private String createAllText(String org, String name, String revision, String packaging, String modifier) {
        return createBaseText(org, name) +
                XmlHelper.enclose(PomModuleDescriptorWriter.DEFAULT_INDENT, PomModuleDescriptorWriter.VERSION, revision) + NL +
                XmlHelper.enclose(PomModuleDescriptorWriter.DEFAULT_INDENT, PomModuleDescriptorWriter.PACKAGING, packaging)
                + NL + XmlHelper.enclose(PomModuleDescriptorWriter.DEFAULT_INDENT, PomModuleDescriptorWriter.CLASSIFIER, modifier) + NL;
    }
}