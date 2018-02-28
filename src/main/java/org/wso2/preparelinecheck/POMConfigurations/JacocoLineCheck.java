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

package org.wso2.preparelinecheck.POMConfigurations;

import org.apache.maven.model.PluginExecution;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import org.wso2.preparelinecheck.Constants;

import java.util.ArrayList;
import java.util.List;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * contain methods to generate objects in  org.apache.maven.model
 */
public class JacocoLineCheck {

    /**
     * generate an object of org.apache.maven.model.PluginExecution for jacoco default-check execution
     * @return  PluginExecution object
     */
    public static PluginExecution jacocoDefaultCheckExecutionObject(){
        PluginExecution default_check = new PluginExecution();

        default_check.setId("default-check");

        List<String> goals = new ArrayList<String>();
        goals.add("check");
        default_check.setGoals(goals);

        DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = null;
        try {
            builder = builderFactory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }
        try {
            FileInputStream xmlFileStream = new FileInputStream(Constants.DEFAULT_CHECK_XML_FILE);
            Document document = builder.parse(xmlFileStream);
            default_check.setConfiguration(document);
            return default_check;
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }
}
