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

package org.gradle.api.internal.dependencies.maven.deploy.groovy

import org.gradle.api.dependencies.maven.GroovyPomFilterContainer
import org.gradle.api.dependencies.maven.PomFilterContainer
import org.gradle.api.internal.dependencies.maven.deploy.BaseMavenDeployerTest
import org.gradle.api.internal.dependencies.maven.deploy.groovy.DefaultGroovyPomFilterContainerTest
import org.gradle.impl.api.internal.dependencies.maven.deploy.BaseMavenDeployer
import org.gradle.impl.api.internal.dependencies.maven.deploy.groovy.DefaultGroovyMavenDeployer
import org.jmock.integration.junit4.JMock
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import static org.junit.Assert.assertEquals

/**
 * @author Hans Dockter
 */
@RunWith (org.jmock.integration.junit4.JMock.class)
class DefaultGroovyMavenDeployerTest extends BaseMavenDeployerTest {
    private DefaultGroovyMavenDeployer groovyMavenDeployer;
    private DefaultGroovyPomFilterContainerTest groovyMavenResolverHelper

    protected PomFilterContainer createPomFilterContainerMock() {
        context.mock(GroovyPomFilterContainer.class);
    }

    protected BaseMavenDeployer createMavenDeployer() {
        groovyMavenDeployer = new DefaultGroovyMavenDeployer(TEST_NAME, pomFilterContainerMock, artifactPomContainerMock, dependencyManagerMock)
    }

    @Before
    void setUp() {
        super.setUp();
    }


    @Test
    void repositoryBuilder() {
        checkRepositoryBuilder(DefaultGroovyMavenDeployer.REPOSITORY_BUILDER)
    }

    @Test
    void snapshotRepositoryBuilder() {
        checkRepositoryBuilder(DefaultGroovyMavenDeployer.SNAPSHOT_REPOSITORY_BUILDER)
    }


    void checkRepositoryBuilder(String repositoryName) {
        String testUrl = 'testUrl'
        String testProxyHost = 'hans'
        String testUserName = 'userId'
        String testSnapshotUpdatePolicy = 'always'
        String testReleaseUpdatePolicy = 'never'
        groovyMavenDeployer."$repositoryName"(url: testUrl) {
            authentication(userName: testUserName)
            proxy(host: testProxyHost)
            releases(updatePolicy: testReleaseUpdatePolicy)
            snapshots(updatePolicy: testSnapshotUpdatePolicy)
        }
        assertEquals(testUrl, groovyMavenDeployer."$repositoryName".url)
        assertEquals(testUserName, groovyMavenDeployer."$repositoryName".authentication.userName)
        assertEquals(testProxyHost, groovyMavenDeployer."$repositoryName".proxy.host)
        assertEquals(testReleaseUpdatePolicy, groovyMavenDeployer."$repositoryName".releases.updatePolicy)
        assertEquals(testSnapshotUpdatePolicy, groovyMavenDeployer."$repositoryName".snapshots.updatePolicy)
    }

    @Test
    void filter() {
        Closure testClosure = {}
        context.checking {
            one(pomFilterContainerMock).filter(testClosure)
        }
        groovyMavenDeployer.filter(testClosure)
    }

    @Test
    void pom() {
        Closure testClosure = {}
        context.checking {
            one(pomFilterContainerMock).pom(testClosure)
        }
        groovyMavenDeployer.pom(testClosure)
    }

    @Test
    void pomWithName() {
        Closure testClosure = {}
        String testName = 'somename'
        context.checking {
            one(pomFilterContainerMock).pom(testName, testClosure)
        }
        groovyMavenDeployer.pom(testName, testClosure)
    }

    @Test
    void addFilter() {
        Closure testClosure = {}
        String testName = 'somename'
        context.checking {
            one(pomFilterContainerMock).addFilter(testName, testClosure)
        }
        groovyMavenDeployer.addFilter(testName, testClosure)
    }
}



