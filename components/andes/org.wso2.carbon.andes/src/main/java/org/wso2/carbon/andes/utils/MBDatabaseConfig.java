/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.andes.utils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xerces.impl.Constants;
import org.apache.xerces.util.SecurityManager;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.IOException;

/**
 * Reads [repository/conf/broker.xml] and load message store and context store jndi names
 */
public class MBDatabaseConfig {

    private static final Log log = LogFactory.getLog(MBDatabaseConfig.class);
    private String messageStoreJndiName;
    private String contextStoreJndiName;
    private static final int ENTITY_EXPANSION_LIMIT = 0;

    /**
     * Construct MB database config. This will load config values
     * reading from file
     *
     * @param filePath path of the file containing configs of data source jndi names
     */
    public MBDatabaseConfig(String filePath) {

        DocumentBuilderFactory domFactory = getSecuredDocumentBuilderFactory();

        try {
            DocumentBuilder builder = domFactory.newDocumentBuilder();
            Document dDoc = builder.parse(filePath);
            XPath xPath = XPathFactory.newInstance().newXPath();

            String messageStoreXpath = "/broker/persistence/messageStore/property[@name='dataSource']";
            String contextStoreXpath = "/broker/persistence/contextStore/property[@name='dataSource']";

            messageStoreJndiName = (String) xPath.evaluate(messageStoreXpath, dDoc, XPathConstants.STRING);
            contextStoreJndiName = (String) xPath.evaluate(contextStoreXpath, dDoc, XPathConstants.STRING);

        } catch (ParserConfigurationException | IOException
                | XPathExpressionException | SAXException e) {

            log.error("Error when parsing file " + filePath + " to get MB data source JNDI names", e);

        }

    }

    /**
     * Create DocumentBuilderFactory with the XXE and XEE prevention measurements.
     *
     * @return DocumentBuilderFactory instance
     */
    public static DocumentBuilderFactory getSecuredDocumentBuilderFactory() {

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        dbf.setXIncludeAware(false);
        dbf.setExpandEntityReferences(false);
        try {
            dbf.setFeature(Constants.SAX_FEATURE_PREFIX + Constants.EXTERNAL_GENERAL_ENTITIES_FEATURE, false);
            dbf.setFeature(Constants.SAX_FEATURE_PREFIX + Constants.EXTERNAL_PARAMETER_ENTITIES_FEATURE, false);
            dbf.setFeature(Constants.XERCES_FEATURE_PREFIX + Constants.LOAD_EXTERNAL_DTD_FEATURE, false);
            dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);

        } catch (ParserConfigurationException e) {
            log.error("Failed to load XML Processor Feature " + Constants.EXTERNAL_GENERAL_ENTITIES_FEATURE + " or " +
                    Constants.EXTERNAL_PARAMETER_ENTITIES_FEATURE + " or " + Constants.LOAD_EXTERNAL_DTD_FEATURE +
                    " or secure-processing.");
        }

        SecurityManager securityManager = new SecurityManager();
        securityManager.setEntityExpansionLimit(ENTITY_EXPANSION_LIMIT);
        dbf.setAttribute(Constants.XERCES_PROPERTY_PREFIX + Constants.SECURITY_MANAGER_PROPERTY, securityManager);

        return dbf;

    }

    /**
     * Get JNDI name of message store (used to store messages)
     * @return JNDI name as string
     */
    public String getMessageStoreJndiName() {
        return messageStoreJndiName;
    }

    /**
     * Get JNDI name of context store (used to store context information of broker)
     * @return JNDI name as string
     */
    public String getContextStoreJndiName() {
        return contextStoreJndiName;
    }

    /**
     * Check if context store is set. If context store name is same as message store name
     * context store is not in effect
     * @return if context store is in effect
     */
    public boolean isContextStoreAvaliable() {
        return !(messageStoreJndiName.equals(contextStoreJndiName));
    }

}
