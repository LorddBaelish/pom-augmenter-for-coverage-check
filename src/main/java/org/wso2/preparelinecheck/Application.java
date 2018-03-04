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

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;

/**
 * Contain main method for execution.
 * This application analyzes a given project directory to apply Jacoco line coverage threshold
 * This application start with the parent pom file and then move on to each submodule by analyzing pom files in each
 * If Jacoco prepare-agent execution is available in a POM file, line coverage check will be added if it does not exists
 */
public class Application {

    private static final Log log = LogFactory.getLog(Application.class);

    /**
     * Main method
     *
     * @param args args[0]: Path to the project's directory containing parent pom
     *             args[1]: Line coverage threshold
     *             args[2]: Per which element this coverage check should be performed (per BUNDLE, per CLASS etc)
     */
    public static void main(String[] args) {

        try {
            Model parentPom = POMReader.getPOMModel(args[0]);

            //Analyze parent POM
            log.info("Analyzing parent pom");
            JacocoLineCoverage.addLineCoverageRule(parentPom, args[1], args[2]);

            //List all child modules under <modules> tag
            List<String> subModulePaths = parentPom.getModules();

            //Go through each child and analyze for line coverage
            for (String eachSubModulePath : subModulePaths) {

                try {
                    Model childPom = POMReader.getPOMModel(args[0] + File.separator + eachSubModulePath);

                    log.info("Analyzing " + eachSubModulePath);
                    JacocoLineCoverage.addLineCoverageRule(childPom, args[1], args[2]);

                } catch (FileNotFoundException e) {
                    log.warn("skipping this module. POM file not found");

                }
            }

        } catch (FileNotFoundException e) {
            log.fatal("Cannot find the parent POM");
        } catch (ArrayIndexOutOfBoundsException e) {
            log.error("Invalid arguments");
        }
    }
}
