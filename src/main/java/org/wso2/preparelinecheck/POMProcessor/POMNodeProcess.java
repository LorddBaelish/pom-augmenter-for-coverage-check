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

import java.io.File;
import java.io.FileInputStream;
import java.io.StringWriter;
import java.io.Writer;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.wso2.preparelinecheck.Constants;

/**
 * contains methods to process xml files
 */
public class POMNodeProcess {

    /**
     * Add a given Node and it's subtree to another POM file as an execution node under 'jacoco-maven-plugin' plugin
     * @param sourceNodeTree An XML node tree of a 'jacoco-maven-plugin' execution
     * @param targetDOM POM file containing 'jacoco-maven-plugin' plugin
     * @throws Exception DOMException
     */
    public static void addJacocoExecution(String sourceNodeTree, String targetDOM, String COVERAGE_THRESHOLD) throws Exception {

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setValidating(false);
        DocumentBuilder db = dbf.newDocumentBuilder();

        //Parse two xml files
        Document doc1 = db.parse(new FileInputStream(new File(sourceNodeTree)));
        Document doc2 = db.parse(new FileInputStream(new File(targetDOM)));

        //Add line coverage threshold
        doc1.getElementsByTagName(Constants.JACOCO_TAG_COVERAGE_CHECK_VALUE).item(0).setTextContent(COVERAGE_THRESHOLD);

        //Print results to console
        //prettyPrint(doc2);

        //Root node of the source XML
        Node sourceXmlRootNode = doc1.getDocumentElement();

        //Import root node and all of it's elements from source to target DOM
        Node apendingNode = doc2.importNode(sourceXmlRootNode, true);

        //Find the Node named as executions under Jacoco plugin
        Node executionsNode = getJacocoPluginExecutionsElement(doc2);

        //Append root node and it's tree to executionsNode
        executionsNode.appendChild(apendingNode);

        //Print results to console
        //prettyPrint(doc2);

        //Write back modified POM
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        DOMSource source = new DOMSource(doc2);
        StreamResult result = new StreamResult(new File(targetDOM));
        transformer.transform(source, result);
    }

    /**
     * traverse through a DOM and return the executions node of the Jacoco plugin
     * Note: Handle exceptions
     * @param xml DOM file
     * @return executions node in Jacoco plugin
     */
    private static Node getJacocoPluginExecutionsElement(Document xml) throws DOMException{
        //Get a list of plugin nodes
        NodeList plugins = xml.getElementsByTagName(Constants.MAVEN_TAG_PLUGIN);

        //Find Jacoco plugin by traversing through all available plugins
        for(int i = 0; i < plugins.getLength(); i++){
            Element pluginElement = (Element) plugins.item(i);
            String artifactId = pluginElement.getElementsByTagName(Constants.MAVEN_TAG_ARTIFACT_ID).item(0).getTextContent(); // Exception is thrown when no value is present

            //Check for Jacoco maven plugin using artifactId value
            if(artifactId.equals(Constants.JACOCO_MAVEN_PLUGIN)){
                return pluginElement.getElementsByTagName(Constants.MAVEN_TAG_EXECUTIONS).item(0);
            }
        }

        return null;
    }

    /**
     * print a given DOM file on console
     * @param xml DOM
     * @throws Exception
     */
    public static final void prettyPrint(Document xml) throws Exception {
        Transformer tf = TransformerFactory.newInstance().newTransformer();
        tf.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        tf.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        tf.setOutputProperty(OutputKeys.INDENT, "yes");
        Writer out = new StringWriter();
        tf.transform(new DOMSource(xml), new StreamResult(out));
        System.out.println(out.toString());
    }

}
