/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

package org.wso2.preparelinecheck.POMProcessor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.maven.model.Model;
import org.apache.maven.model.Plugin;
import org.apache.maven.model.PluginExecution;
import org.wso2.preparelinecheck.Constants;

import java.io.File;
import java.util.List;

/**
 * contains methods to invoke Jacoco line coverage in a given pom Model object
 */
public class JacocoLineCoverage {

    /**
     * This method first check whether jacoco prepare-agent is available in the given pom. Then apply coverage check rule
     *
     * @param pomFile              org.apache.maven.model.Model object corresponding to the pom file
     * @param coverageThreshold    Jacoco line coverage threshold value to break the build
     * @param coveragePerParameter Per which element-wise coverage check should be applied [per BUNDLE, PACKAGE, CLASS, SOURCEFILE or METHOD]
     * @return Boolean value for the condition -> The pom file was modified or not
     */
    public static boolean addLineCoverageRule(Model pomFile, String coverageThreshold, String coveragePerParameter) {

        final Log log = LogFactory.getLog(JacocoLineCoverage.class);

        //Load all available plugins
        List<Plugin> plugins = pomFile.getBuild().getPlugins();

        boolean JACOCO_PREPARE_AGENT_AVAILABLE = false;
        boolean JACOCO_DEFAULT_CHECK_RULE_PRESENT = false;

        //Go through each plugin
        for (Plugin plugin : plugins) {

            //Check if jacoco maven plugin is present
            if (plugin.getArtifactId().equals(Constants.JACOCO_PLUGIN_ARTIFACT_ID)) {

                //Check if prepare-agent execution step is present
                for (PluginExecution execution : plugin.getExecutions()) {
                    if (execution.getGoals().contains(Constants.JACOCO_GOAL_AGENT_INVOKE)) {

                        log.info("Jacoco execution prepare-agent is available");
                        JACOCO_PREPARE_AGENT_AVAILABLE = true;
                    } else if (execution.getGoals().contains(Constants.JACOCO_GOAL_COVERAGE_RULE_INVOKE)) {

                        log.warn("default-check execution already applied");
                        JACOCO_DEFAULT_CHECK_RULE_PRESENT = true;
                        break;
                    }
                }

                //Add line coverage check rule if the prepare-agent is present but coverage check rule currently not present in the pom
                if (JACOCO_PREPARE_AGENT_AVAILABLE && (!JACOCO_DEFAULT_CHECK_RULE_PRESENT)) {

                    log.info("adding default-check execution to the POM file");

                    String targetXmlPath = pomFile.getProjectDirectory().getAbsolutePath() + File.separator + Constants.POM_NAME;

                    //Copy a node tree from a given xml and append it as a new Jacoco execution node in the target pom file
                    POMNodeProcess.appendJacocoCoverageCheckExecutionNode(Constants.JACOCO_NODE_COVERAGE_CHECK_RULE, targetXmlPath, coverageThreshold, coveragePerParameter);

                    return true;

                } else if (!JACOCO_PREPARE_AGENT_AVAILABLE) {

                    log.warn("Cannot apply line coverage check. Jacoco prepare-agent has not been invoked");
                }
                else {
                    log.info("Skipping rule adding due to the default-check execution is already available");
                }
            }
        }

        return false;
    }

    /**
     * To change the jacoco unit test report generation path to the default value({$project.build.directory}/jacoco.exec)
     * This method search for <destFile> and <dataFile> nodes in the given pom file under jacoco plugin and modify them according to the input path parameter
     *
     * @param pomFile Name of the pom file model where the path set should be performed
     * @return Truth value whether the path set procedure was performed or not
     */
    public static boolean setDefaultJacocoUTReportPath(Model pomFile){

        final Log log = LogFactory.getLog(JacocoLineCoverage.class);

        //Load all available plugins
        List<Plugin> plugins = pomFile.getBuild().getPlugins();

        boolean JACOCO_PREPARE_AGENT_AVAILABLE = false;

        //Go through each plugin
        for (Plugin plugin : plugins) {

            //Check if jacoco maven plugin is present
            if (plugin.getArtifactId().equals(Constants.JACOCO_PLUGIN_ARTIFACT_ID)) {

                //Check if prepare-agent execution step is present
                for (PluginExecution execution : plugin.getExecutions()) {
                    if (execution.getGoals().contains(Constants.JACOCO_GOAL_AGENT_INVOKE)) {

                        log.info("Jacoco_execution:prepare-agent is available");
                        JACOCO_PREPARE_AGENT_AVAILABLE = true;
                    }
                }

                //Check whether <destFile> node is present. Modify if available. Path already set to default if not
                if (JACOCO_PREPARE_AGENT_AVAILABLE) {

                    String targetXmlPath = pomFile.getProjectDirectory().getAbsolutePath() + File.separator + Constants.POM_NAME;
                    return POMNodeProcess.setDefaultJacocoUTDestinationNode(targetXmlPath);
                }
                else {

                    log.info("Jacoco execution prepare-agent has not invoked");
                }
            }
        }

        return false;
    }
}
