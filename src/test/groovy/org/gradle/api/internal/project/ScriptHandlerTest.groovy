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

package org.gradle.api.internal.project

import org.gradle.api.InputStreamClassLoader
import org.gradle.api.internal.project.DefaultProject
import org.gradle.util.HelperUtil

/**
 * @author Hans Dockter
 */
class ScriptHandlerTest extends GroovyTestCase {
    static final String TEST_BUILD_FILE_NAME = 'somename.craidle'
    static final String TEST_BUILD_FILE_CACHE_NAME = 'somename_craidle'

    ScriptHandler scriptHandler

    DefaultProject testProject

    File testProjectDir

    File gradleDir
    File cacheDir
    File cachedFile

    boolean projectMethodCalled

    ClassLoader classLoader

    void setUp() {
        testProjectDir = HelperUtil.makeNewTestDir('projectdir')
        classLoader = new InputStreamClassLoader()
        InputStream inputStream = this.getClass().getResourceAsStream('/org/gradle/api/ClasspathTester.dat')
        classLoader.loadClass("org.gradle.api.ClasspathTester", inputStream)
        testProject = [
                someProjectMethod: {projectMethodCalled = true},
                getRootDir: {testProjectDir.parentFile},
                getProjectDir: {testProjectDir},
                getBuildFileName: {TEST_BUILD_FILE_NAME},
                getBuildScriptClassLoader: {classLoader}
        ] as DefaultProject
        scriptHandler = new ScriptHandler()
        projectMethodCalled = false
        gradleDir = new File(testProjectDir, ScriptHandler.GRADLE_DIR_NAME)
        cacheDir = new File(gradleDir, TEST_BUILD_FILE_CACHE_NAME)
        cachedFile = new File(gradleDir, "$TEST_BUILD_FILE_CACHE_NAME/${TEST_BUILD_FILE_CACHE_NAME}.class")
    }

    protected void tearDown() {
        HelperUtil.deleteTestDir()
    }

    void testWriteToCacheAndLoadFromCache() {
        Script script = scriptHandler.writeToCache(testProject, testScriptText)
        checkCacheDestination()
        evaluateScript(script)
        projectMethodCalled = false
        evaluateScript(scriptHandler.loadFromCache(testProject, 0))
    }

    private void checkCacheDestination() {
        assert cacheDir.isDirectory()
        assert cachedFile.isFile()
    }

    void testCreateScript() {
        Script script = scriptHandler.createScript(testProject, testScriptText)
        evaluateScript(script)
    }

    private void evaluateScript(Script script) {
        testProject.buildScript = script
        script.run()
        assertTrue(projectMethodCalled)
        assertEquals("scriptMethod", script.scriptMethod())
        assertEquals(testProject.path + 'mySuffix', script.scriptProperty)
        assertEquals(testProject.path + 'mySuffix', testProject.additionalProperties['scriptProperty'])
    }

    void testLoadFromCacheWithNonCacheBuildFile() {
        assertNull(scriptHandler.loadFromCache(testProject, 0))
    }

    void testLoadFromCacheWithStaleCache() {
        scriptHandler.writeToCache(testProject, testScriptText)
        cachedFile.setLastModified(0)
        assertNull(scriptHandler.loadFromCache(testProject, 100000))
    }

//    void testWithException() {
//        try {
//            testProject.buildScriptFinder = mockBuildScriptFinder('unknownProp')
//            scriptHandler.evaluate(testProject)
//            fail()
//        } catch (GradleScriptException e) {
//            assert e.cause instanceof MissingPropertyException
//        }
//    }

    private String getTestScriptText() {
        '''
// We leave out the path to check import adding
new ClasspathTester().testMethod()
def scriptMethod() { 'scriptMethod' }
someProjectMethod()
scriptProperty = project.path + 'mySuffix'
String internalProp = 'a'
assert internalProp == 'a'
newProperty = 'a'
assert newProperty == 'a'
assert newProperty == project.newProperty
import org.gradle.api.*
'''
    }
}