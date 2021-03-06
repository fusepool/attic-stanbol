/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.stanbol.cmsadapter.servicesapi.mapping;

import java.util.List;

import org.apache.stanbol.cmsadapter.servicesapi.helper.OntologyResourceHelper;
import org.apache.stanbol.cmsadapter.servicesapi.model.mapping.BridgeDefinitions;
import org.apache.stanbol.cmsadapter.servicesapi.model.web.ConnectionInfo;
import org.apache.stanbol.cmsadapter.servicesapi.model.web.decorated.AdapterMode;

import com.hp.hpl.jena.ontology.OntModel;

/**
 * {@link MappingConfiguration} describes an environment where an ontology extraction operation from CMS takes
 * place.
 * 
 * @author Suat
 * 
 */
public interface MappingConfiguration {
    /**
     * Changes ontology model that will be generated by the extraction process.
     * Using this function after the processors started may result in loss of triples.
     * @param ontModel
     *            New Ontology
     */
    void setOntModel(OntModel ontModel);

    /**
     * Method to retrieve ontology model, although modifications to ontology through this model is discouraged
     * (see {@link OntologyResourceHelper}), any processor can access and modify this model.
     * 
     * @return Ontology that is generated through extraction process.
     */
    OntModel getOntModel();

    /**
     * Method to change or set {@link BridgeDefinitions} of the configuration.
     * 
     * @param bridgeDefinitions
     *            {@link BridgeDefinitions} to be used in the extraction process.
     */
    void setBridgeDefinitions(BridgeDefinitions bridgeDefinitions);

    /**
     * Method to retrieve {@link BridgeDefinitions} to be used in the extraction process.
     * 
     * @return {@link BridgeDefinitions} instance.
     */
    BridgeDefinitions getBridgeDefinitions();

    /**
     * Method to change or set {@link AdapterMode} of the configuration. {@link AdapterMode}s are used to tune
     * when to access a remote repository during extraction process. For detailed explanation see
     * {@link AdapterMode}
     * 
     * @param adapterMode
     */
    void setAdapterMode(AdapterMode adapterMode);

    /**
     * Method to retrieve {@link AdapterMode} of the configuration.
     * 
     * @return
     */
    AdapterMode getAdapterMode();

    /**
     * Method to change or set the URI of the ontology being generated. Changing this configuration during
     * extraction process may result in inconsistent ontology. The extracted ontology will be automatically
     * saved to persistence store by by this URI.
     * 
     * @param ontologyURI
     *            URI of the ontology.
     */
    void setOntologyURI(String ontologyURI);

    /**
     * Method to retrieve URI of the ontology.
     * 
     * @return URI of the ontology.
     */
    String getOntologyURI();

    /**
     * Method to change or set {@link ConnectionInfo} of the mapping configuration. ConnectionInfo is used for
     * accessing the content repository in <b>TOLERATED OFFLINE</b> and <b>ONLINE</b> {@link AdapterMode}s.
     * 
     * @param connectionInfo
     */
    void setConnectionInfo(ConnectionInfo connectionInfo);

    /**
     * Method to retrieve {@link ConnectionInfo} of the mapping configuration.
     * 
     * @return
     */
    ConnectionInfo getConnectionInfo();

    /**
     * Method to change or set CMS Objects of the mapping configuration. If {@link AdapterMode} is
     * <b>ONLINE</b> there is no need to set these objects. They will be retrieved from a CMS repository using
     * {@link ConnectionInfo} supplied to this configuration. </br>An Interface for a common CMS Object is not yet
     * defined. So {@link Object} is used as type.
     * 
     * @param objects
     *            A list of objects to be processed by processors during the extraction.
     */
    void setObjects(List<Object> objects);

    /**
     * Method to retrieve CMS OBjects of the configuration.
     * @return List of CMS objects 
     */
    List<Object> getObjects();
}
