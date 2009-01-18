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

package org.gradle.api.tasks.wrapper;

import org.apache.commons.io.FileUtils;
import org.gradle.api.*;
import org.gradle.api.internal.DefaultTask;
import org.gradle.util.GUtil;
import org.gradle.impl.wrapper.Install;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

/**
 * The wrapper task generates scripts (for *nix and windows) which enable to build your project with Gradle, without having to install Gradle.
 * The scripts generated by this task are supposed to be commited to your version control system. This tasks also copies
 * a gradle-wrapper.jar to your project dir which needs also be commited into your VCS.
 * The scripts delegates to this jar. If a user execute a wrapper script the first time, the script downloads the gradle-distribution and
 * runs the build against the downloaded distribution. Any installed Gradle distribution is ignored when using the wrapper scripts.
 * Alternatively you can store the distribution for the wrapper in your version control system.
 *
 * @author Hans Dockter
 */
public class Wrapper extends DefaultTask {
    
    public static final String DEFAULT_URL_ROOT = "http://dist.codehaus.org/gradle";
    public static final String WRAPPER_JAR_BASE_NAME = "gradle-wrapper";
    public static final String DEFAULT_DISTRIBUTION_PARENT_NAME = "wrapper/dists";
    public static final String DEFAULT_ARCHIVE_NAME = "gradle";
    public static final String DEFAULT_ARCHIVE_CLASSIFIER = "bin";

    public enum PathBase { PROJECT, GRADLE_USER_HOME }

    private String scriptDestinationPath;

    private String jarPath;

    private String distributionPath;

    private String archiveName;

    private String archiveClassifier;

    private PathBase distributionBase = PathBase.GRADLE_USER_HOME;

    private String gradleVersion;

    private String urlRoot;

    private String archivePath;

    private PathBase archiveBase = PathBase.GRADLE_USER_HOME;

    private WrapperScriptGenerator wrapperScriptGenerator = new WrapperScriptGenerator();

    public Wrapper(Project project, String name) {
        super(project, name);
        doFirst(new TaskAction() {
            public void execute(Task task) {
                generate(task);
            }
        });
        scriptDestinationPath = "";
        jarPath = "";
        distributionPath = DEFAULT_DISTRIBUTION_PARENT_NAME;
        archiveName = DEFAULT_ARCHIVE_NAME;
        archiveClassifier = DEFAULT_ARCHIVE_CLASSIFIER;
        archivePath = DEFAULT_DISTRIBUTION_PARENT_NAME;
        urlRoot = DEFAULT_URL_ROOT;
    }

    private void generate(Task task) {
        if (scriptDestinationPath == null) {
            throw new InvalidUserDataException("The scriptDestinationPath property must be specified!");
        }
        String wrapperDir = (GUtil.isTrue(jarPath) ? jarPath + "/" : "");
        new File(getProject().getProjectDir(), wrapperDir).mkdirs();
        String wrapperJar = wrapperDir + Install.WRAPPER_JAR;
        String wrapperPropertiesPath = wrapperDir + Install.WRAPPER_PROPERTIES;
        File jarFileDestination = new File(getProject().getProjectDir(), wrapperJar);
        File propertiesFileDestination = new File(getProject().getProjectDir(), wrapperPropertiesPath);
        File jarFileSource = new File(getProject().getBuild().getGradleHomeDir() + "/lib",
                WRAPPER_JAR_BASE_NAME + "-" + getProject().getBuild().getGradleVersion() + ".jar");
        propertiesFileDestination.delete();
        jarFileDestination.delete();
        writeProperties(propertiesFileDestination);
        try {
            FileUtils.copyFile(jarFileSource, jarFileDestination);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        File scriptDestinationDir = new File(getProject().getProjectDir(), scriptDestinationPath);
        wrapperScriptGenerator.generate(wrapperJar, wrapperPropertiesPath, scriptDestinationDir);
    }

    private void writeProperties(File propertiesFileDestination) {
        Properties wrapperProperties = new Properties();
        wrapperProperties.put(org.gradle.impl.wrapper.Wrapper.URL_ROOT_PROPERTY, urlRoot);
        wrapperProperties.put(org.gradle.impl.wrapper.Wrapper.DISTRIBUTION_BASE_PROPERTY, distributionBase.toString());
        wrapperProperties.put(org.gradle.impl.wrapper.Wrapper.DISTRIBUTION_PATH_PROPERTY, distributionPath);
        wrapperProperties.put(org.gradle.impl.wrapper.Wrapper.DISTRIBUTION_NAME_PROPERTY, archiveName);
        wrapperProperties.put(org.gradle.impl.wrapper.Wrapper.DISTRIBUTION_CLASSIFIER_PROPERTY, archiveClassifier);
        wrapperProperties.put(org.gradle.impl.wrapper.Wrapper.DISTRIBUTION_VERSION_PROPERTY, gradleVersion);
        wrapperProperties.put(org.gradle.impl.wrapper.Wrapper.ZIP_STORE_BASE_PROPERTY, archiveBase.toString());
        wrapperProperties.put(org.gradle.impl.wrapper.Wrapper.ZIP_STORE_PATH_PROPERTY, archivePath);
        GUtil.saveProperties(wrapperProperties, propertiesFileDestination);
    }

    /**
     * Returns the script destination path.
     *
     * @see #setScriptDestinationPath(String) 
     */
    public String getScriptDestinationPath() {
        return scriptDestinationPath.toString();
    }

    /**
     * Specifies a path as the parent dir of the scripts which are generated when executing the wrapper task.
     * This path specifies a directory <i>relative</i> to the project dir.  Defaults to empty string, i.e. the scripts
     * are placed into the project root dir.
     *
     * @param scriptDestinationPath Any object which <code>toString</code> method specifies the path.
     * Most likely a String or File object.
     */
    public void setScriptDestinationPath(String scriptDestinationPath) {
        this.scriptDestinationPath = scriptDestinationPath;
    }

    /**
     * Returns the jar path.
     *
     * @see #setJarPath(String) 
     */
    public String getJarPath() {
        return jarPath.toString();
    }

    /**
     * When executing the wrapper task, the jar path specifies the path where the gradle-wrapper.jar is copied to. The
     * jar path must be a path relative to the project dir. The gradle-wrapper.jar must be submitted to your version
     * control system. Defaults to empty string, i.e. the jar is placed into the project root dir.
     *
     * @param jarPath
     */
    public void setJarPath(String jarPath) {
        this.jarPath = jarPath;
    }

    /**
     * Returns the distribution path.
     *
     * @see #setDistributionPath(String) 
     */
    public String getDistributionPath() {
        return distributionPath.toString();
    }

    /**
     * Set's the path where the gradle distributions needed by the wrapper are unzipped. The path is relative to the
     * dir specified with {@link #distributionBase}.
     *
     * @param distributionPath  
     */
    public void setDistributionPath(String distributionPath) {
        this.distributionPath = distributionPath;
    }

    /**
     * Returns the gradle version for the wrapper.
     *
     * @see #setGradleVersion(String) 
     */
    public String getGradleVersion() {
        return gradleVersion;
    }

    /**
     * The version of the gradle distribution required by the wrapper. This is usually the same version of Gradle you use
     * for building your project.
     *
     * @param gradleVersion
     */
    public void setGradleVersion(String gradleVersion) {
        this.gradleVersion = gradleVersion;
    }

    public String getUrlRoot() {
        return urlRoot;
    }

    /**
     * A URL where to download the gradle distribution from. The pattern used by the wrapper for downloading is:
     * <code>{@link #getUrlRoot()}</i>/{@link #getArchiveName()}-{@link #getArchiveClassifier()}-{@link #getGradleVersion()}.zip</code>.
     * The default for the URL root is {@link #DEFAULT_URL_ROOT}.
     *
     * The wrapper downloads a certain distribution only ones and caches it.
     * If your {@link #getDistributionBase()} is the project, you might submit the distribution to your version control system.
     * That way no download is necessary at all. This might be in particular interesting, if you provide a custom gradle snapshot to the wrapper,
     * because you don't need to provide a download server then.
     *  
     * @param urlRoot
     */
    public void setUrlRoot(String urlRoot) {
        this.urlRoot = urlRoot;
    }

    /**
     * Returns the distribution base.
     *
     * @see #setDistributionBase(org.gradle.api.tasks.wrapper.Wrapper.PathBase) 
     */
    public PathBase getDistributionBase() {
        return distributionBase;
    }

    /**
     * The distribution base specifies whether the unpacked wrapper distribution should be stored in the project or in
     * the gradle user home dir. The path specified in {@link #distributionPath} is a relative path to either of those
     * dirs.  
     *
     * @param distributionBase
     */
    public void setDistributionBase(PathBase distributionBase) {
        this.distributionBase = distributionBase;
    }

    /**
     * Returns the archive path.
     *
     * @see #setArchivePath(String) 
     */
    public String getArchivePath() {
        return archivePath;
    }

    /**
     * Set's the path where the gradle distributions archive should be saved (i.e. the parent dir). The path is relative to the
     * parent dir specified with {@link #getArchiveBase()}.
     *
     * @param archivePath
     */
    public void setArchivePath(String archivePath) {
        this.archivePath = archivePath;
    }

    /**
     * Returns the archive base.
     *
     * @see #setArchiveBase(org.gradle.api.tasks.wrapper.Wrapper.PathBase) 
     */
    public PathBase getArchiveBase() {
        return archiveBase;
    }

    /**
     * The distribution base specifies whether the unpacked wrapper distribution should be stored in the project or in
     * the gradle user home dir. The path specified in {@link #getArchivePath()} is a relative path to etiher of those dirs.
     *
     * @param archiveBase
     */
    public void setArchiveBase(PathBase archiveBase) {
        this.archiveBase = archiveBase;
    }

    /**
     * Returnes the archive name.
     *
     * @see #setArchiveName(String) 
     */
    public String getArchiveName() {
        return archiveName;
    }

    /**
     * Specifies the name of the archive as part of the download URL. The download URL is assembled by the pattern:
     *
     * <code>{@link #getUrlRoot()}</i>/{@link #getArchiveName()}-{@link #getArchiveClassifier()}-{@link #getGradleVersion()}.zip</code>
     *
     * The default for the archive name is {@link #DEFAULT_ARCHIVE_NAME}.
     *
     * @param archiveName
     */
    public void setArchiveName(String archiveName) {
        this.archiveName = archiveName;
    }

    /**
     * Returns the archive classifier.
     *
     * @see #setArchiveClassifier(String) 
     */
    public String getArchiveClassifier() {
        return archiveClassifier;
    }

    /**
     * Specifies the classifier of the archive as part of the download URL. The download URL is assembled by the pattern:
     *
     * <code>{@link #getUrlRoot()}</i>/{@link #getArchiveName()}-{@link #getArchiveClassifier()}-{@link #getGradleVersion()}.zip</code>
     *
     * The default for the archive classifier is {@link #DEFAULT_ARCHIVE_CLASSIFIER}.
     *
     * @param archiveClassifier
     */
    public void setArchiveClassifier(String archiveClassifier) {
        this.archiveClassifier = archiveClassifier;
    }

    public WrapperScriptGenerator getUnixWrapperScriptGenerator() {
        return wrapperScriptGenerator;
    }

    public void setUnixWrapperScriptGenerator(WrapperScriptGenerator wrapperScriptGenerator) {
        this.wrapperScriptGenerator = wrapperScriptGenerator;
    }
}
