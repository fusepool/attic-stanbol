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
package org.apache.stanbol.cmsadapter.core.mapping;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.clerezza.rdf.core.MGraph;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.apache.felix.scr.annotations.ReferencePolicy;
import org.apache.felix.scr.annotations.ReferenceStrategy;
import org.apache.felix.scr.annotations.Service;
import org.apache.stanbol.cmsadapter.servicesapi.mapping.RDFBridge;
import org.apache.stanbol.cmsadapter.servicesapi.mapping.RDFMapper;
import org.apache.stanbol.cmsadapter.servicesapi.model.web.ConnectionInfo;
import org.apache.stanbol.cmsadapter.servicesapi.repository.RepositoryAccess;
import org.apache.stanbol.cmsadapter.servicesapi.repository.RepositoryAccessException;
import org.apache.stanbol.cmsadapter.servicesapi.repository.RepositoryAccessManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This component keeps track of {@link RDFBridge}s and {@link RDFMapper}s in the environment and it provides
 * a method to submit RDF data to be annotated according to <code>RDFBridge</code>s. <code>RDFMapper</code>s
 * update repository based on the annotated RDF.
 * 
 * @author suat
 * 
 */
@Component(immediate = true)
@Service(value = RDFBridgeManager.class)
public class RDFBridgeManager {

    private static final Logger log = LoggerFactory.getLogger(RDFBridgeManager.class);

    @Reference(cardinality = ReferenceCardinality.OPTIONAL_MULTIPLE, referenceInterface = RDFBridge.class, policy = ReferencePolicy.DYNAMIC, bind = "bindRDFBridge", unbind = "unbindRDFBridge", strategy = ReferenceStrategy.EVENT)
    List<RDFBridge> rdfBridges = new CopyOnWriteArrayList<RDFBridge>();

    @Reference(cardinality = ReferenceCardinality.OPTIONAL_MULTIPLE, referenceInterface = RDFMapper.class, policy = ReferencePolicy.DYNAMIC, bind = "bindRDFMapper", unbind = "unbindRDFMapper", strategy = ReferenceStrategy.EVENT)
    List<RDFMapper> rdfMappers = new CopyOnWriteArrayList<RDFMapper>();

    @Reference
    RepositoryAccessManager accessManager;

    public void storeRDFToRepository(ConnectionInfo connectionInfo, MGraph rawRDFData) throws RepositoryAccessException {
        if(rdfBridges.size() == 0) {
            log.info("There is no RDF Bridge to execute");
            return;
        }
        
        // According to connection type get RDF mapper, repository accessor, session
        RDFMapper mapper = getRDFMapper(connectionInfo);
        RepositoryAccess repositoryAccess = accessManager.getRepositoryAccessor(connectionInfo);
        Object session = repositoryAccess.getSession(connectionInfo);

        // Annotate raw RDF with CMS vocabulary annotations according to bridges
        CMSVocabularyAnnotator annotator = new CMSVocabularyAnnotator();
        annotator.addAnnotationsToGraph(rdfBridges, rawRDFData);

        // Store annotated RDF in repository
        mapper.storeRDFinRepository(session, rawRDFData);
    }

    private RDFMapper getRDFMapper(ConnectionInfo connectionInfo) {
        RDFMapper mapper = null;
        String type = connectionInfo.getConnectionType();
        for (RDFMapper rdfMapper : rdfMappers) {
            if (rdfMapper.getClass().getSimpleName().startsWith(type)) {
                mapper = (RDFMapper) rdfMapper;
            }
        }
        if (mapper == null) {
            log.warn("Failed to retrieve RDFMapper for connection type: {}", type);
            throw new IllegalStateException("Failed to retrieve RDFMapper for connection type: " + type);
        }
        return mapper;
    }

    protected void bindRDFBridge(RDFBridge rdfBridge) {
        rdfBridges.add(rdfBridge);
    }

    protected void unbindRDFBridge(RDFBridge rdfBridge) {
        rdfBridges.remove(rdfBridge);
    }

    protected void bindRDFMapper(RDFMapper rdfMapper) {
        rdfMappers.add(rdfMapper);
    }

    protected void unbindRDFMapper(RDFMapper rdfMapper) {
        rdfMappers.remove(rdfMapper);
    }
}