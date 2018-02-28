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

package org.wso2.preparelinecheck.FileHandler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

import org.wso2.preparelinecheck.Constants;
import org.wso2.preparelinecheck.Application;

/**
 * Contains methods for reading a pom.xml file
 */
public class POMReader {

    private static final Log log = LogFactory.getLog(Application.class);

    /**
     * This method reads pom file as a model
     * @param path  path of the pom.xml
     * @return  org.apache.maven.model.Model Object corresponding to the pom.xml file given;
     */
    public static Model getPOMModel(String path) throws Exception{

        File pomFile = new File(path + File.separator + Constants.POM_NAME);
        InputStreamReader reader;
        Model model = new Model();
        MavenXpp3Reader mavenReader = new MavenXpp3Reader();
        try {
            FileInputStream fileStream = new FileInputStream(path + File.separator + Constants.POM_NAME);
            reader = new InputStreamReader(fileStream, Constants.UTF_8_CHARSET_NAME);
            model = mavenReader.read(reader);
            model.setPomFile(pomFile);

            fileStream.close();
            reader.close();

        } catch (FileNotFoundException e) {
            //e.printStackTrace();
            log.info("NO Pom Files found");
            return model;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        }
        return model;
    }
}

