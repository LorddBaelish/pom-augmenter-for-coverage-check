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
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.wso2.preparelinecheck.Constants;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

/**
 * contains methods to process pom/xml files
 */
public class POMNodeProcess {

    public static final Log log = LogFactory.getLog(POMNodeProcess.class);

    /**
     * Copy a given Node and it's subtree to a target POM file as an execution node under 'jacoco-maven-plugin' plugin
     *
     * @param sourceNodeTree An XML file path containing a node tree of a 'jacoco-maven-plugin' execution
     * @param targetDOM      POM file containing 'jacoco-maven-plugin' plugin
     * @throws Exception DOMException
     */
    public static void appendJacocoExecutionNode(String sourceNodeTree, String targetDOM, String COVERAGE_THRESHOLD, String coveragePerParameter) {

        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setValidating(false);
            DocumentBuilder db = dbf.newDocumentBuilder();

            //Parse two xml files
            Document sourceXmlFile = db.parse(new FileInputStream(new File(sourceNodeTree)));
            Document targetPomFile = db.parse(new FileInputStream(new File(targetDOM)));

            //Set line coverage threshold and 'coverage per' parameter
            sourceXmlFile.getElementsByTagName(Constants.JACOCO_TAG_COVERAGE_CHECK_VALUE).item(0).setTextContent(COVERAGE_THRESHOLD);
            sourceXmlFile.getElementsByTagName(Constants.JACOCO_TAG_COVERAGE_PER_ELEMENT).item(0).setTextContent(coveragePerParameter);

            //Root node of the source XML
            Node sourceExecutionNode = sourceXmlFile.getDocumentElement();

            //Import root node and all of it's elements from source to target pom file
            Node importedExecutionNode = targetPomFile.importNode(sourceExecutionNode, true);

            //Find the Node named as executions under Jacoco plugin
            Node parentExecutionsNode = getJacocoPluginNodeExecutions(targetPomFile);

            //Append root node and it's tree to parentExecutionsNode
            parentExecutionsNode.appendChild(importedExecutionNode);

            //Write back modified POM
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(targetPomFile);
            StreamResult result = new StreamResult(new File(targetDOM));
            transformer.transform(source, result);
        } catch (ParserConfigurationException e) {
            log.error("Error creating xml parser");
            e.printStackTrace();
        } catch (IOException e) {
            log.error("Error reading xml files");
            e.printStackTrace();
        } catch (SAXException e) {
            log.error("Error parsing xml files");
            e.printStackTrace();
        } catch (TransformerConfigurationException e) {
            log.error("Error while configuring writing to pom step");
            e.printStackTrace();
        } catch (TransformerException e) {
            log.error("Error while writing to pom");
            e.printStackTrace();
        }

    }

    /**
     * Traverse through a DOM and return the parent executions node of the Jacoco plugin
     *
     * @param xml DOM file
     * @return executions node in Jacoco plugin
     */
    private static Node getJacocoPluginNodeExecutions(Document xml) throws DOMException {
        //Get a list of plugin nodes
        NodeList plugins = xml.getElementsByTagName(Constants.MAVEN_TAG_PLUGIN);

        //Find Jacoco plugin by traversing through all available plugins
        for (int i = 0; i < plugins.getLength(); i++) {
            Element pluginNode = (Element) plugins.item(i);

            //Grab 'artifactId' value from the plugin Node
            String artifactId = pluginNode.getElementsByTagName(Constants.MAVEN_TAG_ARTIFACT_ID).item(0).getTextContent(); // Exception is thrown when no value is present

            //Check for Jacoco maven plugin using the retrieved artifactId value
            if (artifactId.equals(Constants.JACOCO_MAVEN_PLUGIN)) {
                //Return the Node 'Executions' from the jacoco plugin node
                return pluginNode.getElementsByTagName(Constants.MAVEN_TAG_EXECUTIONS).item(0);
            }
        }

        return null;
    }
}
