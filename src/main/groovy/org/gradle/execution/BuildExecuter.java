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

package org.gradle.execution;

import org.gradle.api.*;
import org.gradle.api.internal.DefaultTask;
import org.gradle.api.internal.project.DefaultProject;
import org.gradle.util.Clock;
import org.slf4j.Logger;

/**
 * @author Hans Dockter
 */
public class BuildExecuter {
    private static Logger logger = LoggerFactory.getLogger(BuildExecuter.class);

    Dag dag;

    public BuildExecuter() {
    }

    public BuildExecuter(Dag dag) {
        this.dag = dag;
    }

    public Boolean execute(Iterable<Task> tasks, Project rootProject, boolean checkForRebuildDag) {
        assert tasks != null;
        assert rootProject != null;

        Clock clock = new Clock();
        dag.reset();
        fillDag(this.dag, tasks, rootProject);
        logger.info("Timing: Creating the DAG took " + clock.getTime());
        clock.reset();
        dag.execute();
        logger.info("Timing: Executing the DAG took " + clock.getTime());
        if (!checkForRebuildDag) {
            return null;
        }
        for (Task calledTask : tasks) {
            if (!calledTask.isDagNeutral()) {
                return true;
            }
        }
        return false;
    }

    private void fillDag(Dag dag, Iterable<Task> tasks, Project rootProject) {
        for (Task task : tasks) {
            Set<Task> dependsOnTasks = findDependsOnTasks(task, rootProject);
            dag.addTask(task, dependsOnTasks);
            if (dependsOnTasks.size() > 0) {
                logger.debug("Found dependsOn tasks for {}: {}", task, dependsOnTasks);
                fillDag(dag, dependsOnTasks, rootProject);
            } else {
                logger.debug("Found no dependsOn tasks for {}", task);
            }
        }
    }

    private Set<Task> findDependsOnTasks(Task task, Project rootProject) {
        logger.debug("Find dependsOn tasks for {}", task);
        Set<Task> dependsOnTasks = new HashSet<Task>();
        for (Object taskDescriptor : task.getDependsOn()) {
            String absolutePath = absolutePath(task.getProject(), taskDescriptor);
            Path path = new Path(absolutePath);
            DefaultProject project = (DefaultProject) getProjectFromTaskPath(path, rootProject);
            DefaultTask dependsOnTask = (DefaultTask) project.getTasks().get(path.taskName);
            if (dependsOnTask == null)
                throw new UnknownTaskException("Task with path " + taskDescriptor + " could not be found.");
            dependsOnTasks.add(dependsOnTask);
        }
        return dependsOnTasks;
    }

    private String absolutePath(Project project, Object taskDescriptor) {
        if (taskDescriptor instanceof Task) {
            return ((Task) taskDescriptor).getPath();
        }
        return project.absolutePath(taskDescriptor.toString());
    }

    private Project getProjectFromTaskPath(Path path, Project rootProject) {
        try {
            return rootProject.project(path.projectPath);
        } catch (UnknownProjectException e) {
            throw new UnknownTaskException("Task with path " + path + " could not be found!");
        }
    }

    private static class Path {
        private String projectPath;
        private String taskName;

        private Path(String taskPath) {
            int index = taskPath.lastIndexOf(Project.PATH_SEPARATOR);
            if (index == -1) {
                throw new InvalidUserDataException("Taskpath " + taskPath + " is not a valid path.");
            }
            this.projectPath = index == 0 ? Project.PATH_SEPARATOR : taskPath.substring(0, index);
            this.taskName = taskPath.substring(index + 1);
        }

        public String getProjectPath() {
            return projectPath;
        }

        public String getTaskName() {
            return taskName;
        }

        public String toString() {
            return projectPath + Project.PATH_SEPARATOR + taskName;
        }
    }

}
