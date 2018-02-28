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

package org.wso2.preparelinecheck;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.maven.model.Model;

import org.wso2.preparelinecheck.FileHandler.POMReader;
import org.wso2.preparelinecheck.POMProcessor.JacocoLineCoverage;

import java.io.FileNotFoundException;
import java.util.List;
import java.io.File;

/**
 * Contain main method for execution.
 * This application analyzes a given project directory to apply Jacoco line coverage threshold
 * This application start with the parent pom file and then move on to each submodule by analyzing pom files in each
 * If Jacoco prepare-agent execution is available in a POM file, line coverage check will be added if it is not already there
 */
public class Application {

    private static final Log log = LogFactory.getLog(Application.class);

    /**
     * Main method
     * @param args Path to the project, line coverage threshold
     */
    public static void main(String[] args) {

        try {
            Model parentPomModel = POMReader.getPOMModel(args[0]);

            //Analyze parent POM
            log.info("Analyzing parent pom");
            JacocoLineCoverage.applyLineCoverageCheck(parentPomModel, args[1]);

            //Load all child modules
            List<String> childPomList = parentPomModel.getModules();

            //Go through each child and analyze for line coverage
            for (String eachModule : childPomList) {

                try {
                    Model childPomModel = POMReader.getPOMModel(args[0] + File.separator + eachModule);

                    log.info("Analyzing " + eachModule);
                    JacocoLineCoverage.applyLineCoverageCheck(childPomModel, args[1]);
                }
                catch (FileNotFoundException e){
                    log.warn("skipping this module. POM file not found");
                }
                finally {
                    continue;
                }
            }

        }
        catch (FileNotFoundException e){
            log.fatal("Cannot find the parent POM");
        }
        catch (Exception e){
            log.fatal("Error occurred while reading the parent pom");
        }
    }
}
