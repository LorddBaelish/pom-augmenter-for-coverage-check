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

import org.wso2.preparelinecheck.Application;
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
     * @param pomFile           org.apache.maven.model.Model object corresponding to the pom file
     * @param coverageThreshold Jacoco line coverage threshold value to break the build
     */
    public static boolean applyLineCoverageCheck(Model pomFile, String coverageThreshold) {

        final Log log = LogFactory.getLog(Application.class);

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
                    }
                    if (execution.getGoals().contains(Constants.JACOCO_GOAL_COVERAGE_RULE_INVOKE)){

                        log.warn("default-check execution already applied");
                        JACOCO_DEFAULT_CHECK_RULE_PRESENT = true;
                        break;
                    }
                }

                //Add line coverage check rule if prepare-agent is present
                if(JACOCO_PREPARE_AGENT_AVAILABLE && (!JACOCO_DEFAULT_CHECK_RULE_PRESENT)){
                    try{

                        log.info("adding default-check execution to the POM file");
                        String targetXmlPath = pomFile.getProjectDirectory().getAbsolutePath()+ File.separator + Constants.POM_NAME;

                        POMNodeProcess.addJacocoExecution(Constants.DEFAULT_CHECK_XML_FILE, targetXmlPath, coverageThreshold);

                        return true;
                    }
                    catch (Exception e){

                        e.printStackTrace();
                    }
                }
                else{
                    log.warn("Cannot apply line coverage check. Jacoco has not been invoked");
                }
            }
        }

        return false;
    }
}
