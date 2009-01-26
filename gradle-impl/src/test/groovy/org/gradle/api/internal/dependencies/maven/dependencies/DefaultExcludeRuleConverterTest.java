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
package org.gradle.api.internal.dependencies.maven.dependencies;

import org.apache.ivy.core.module.descriptor.DefaultExcludeRule;
import org.apache.ivy.core.module.id.ArtifactId;
import org.apache.ivy.core.module.id.ModuleId;
import org.apache.ivy.plugins.matcher.ExactPatternMatcher;
import org.apache.ivy.plugins.matcher.GlobPatternMatcher;
import org.apache.ivy.plugins.matcher.PatternMatcher;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Hans Dockter
 */
public class DefaultExcludeRuleConverterTest {
    private static final String TEST_ORG = "org";
    private static final String TEST_MODULE = "module";

    private DefaultExcludeRuleConverter excludeRuleConverter;

    @Before
    public void setUp() {
        excludeRuleConverter = new DefaultExcludeRuleConverter();
    }
    
    @Test
    public void convertableRule() {
        DefaultExcludeRule excludeRule = new DefaultExcludeRule(new ArtifactId(
                new ModuleId(TEST_ORG, TEST_MODULE), PatternMatcher.ANY_EXPRESSION,
                PatternMatcher.ANY_EXPRESSION,
                PatternMatcher.ANY_EXPRESSION),
                ExactPatternMatcher.INSTANCE, null);
        MavenExclude mavenExclude = excludeRuleConverter.convert(excludeRule);
        assertEquals(TEST_ORG, mavenExclude.getGroupId());
        assertEquals(TEST_MODULE, mavenExclude.getArtifactId());
    }
    
    @Test
    public void unconvertableRules() {
        checkForNull(new DefaultExcludeRule(new ArtifactId(
                new ModuleId(PatternMatcher.ANY_EXPRESSION, TEST_MODULE), PatternMatcher.ANY_EXPRESSION,
                PatternMatcher.ANY_EXPRESSION,
                PatternMatcher.ANY_EXPRESSION),
                ExactPatternMatcher.INSTANCE, null));
        checkForNull(new DefaultExcludeRule(new ArtifactId(
                new ModuleId(TEST_ORG, PatternMatcher.ANY_EXPRESSION), PatternMatcher.ANY_EXPRESSION,
                PatternMatcher.ANY_EXPRESSION,
                PatternMatcher.ANY_EXPRESSION),
                ExactPatternMatcher.INSTANCE, null));
        checkForNull(new DefaultExcludeRule(new ArtifactId(
                new ModuleId(TEST_ORG, TEST_MODULE), PatternMatcher.ANY_EXPRESSION,
                PatternMatcher.ANY_EXPRESSION,
                PatternMatcher.ANY_EXPRESSION),
                GlobPatternMatcher.INSTANCE, null));
    }

    private void checkForNull(DefaultExcludeRule excludeRule) {
        assertNull(excludeRuleConverter.convert(excludeRule));
    }
}
