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
package org.apache.stanbol.ontologymanager.ontonet.api.io;

import java.io.InputStream;
import java.util.Collection;
import java.util.Collections;

import org.apache.clerezza.rdf.core.Graph;
import org.apache.clerezza.rdf.core.serializedform.Parser;
import org.apache.clerezza.rdf.core.serializedform.UnsupportedFormatException;
import org.apache.stanbol.ontologymanager.ontonet.impl.util.OntologyUtils;
import org.apache.stanbol.owl.util.OWLUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An ontology input source that returns a Clerezza {@link Graph} after parsing its serialized content from an
 * input stream. <br>
 * <br>
 * Note that this implementation does not tamper with the triple store. The resulting graph is created
 * in-memory, and its triples will have to be manually added to a stored graph if necessary.
 * 
 * @author alexdma
 * 
 */
public class GraphContentInputSource extends AbstractClerezzaGraphInputSource {

    private Logger log = LoggerFactory.getLogger(getClass());

    public GraphContentInputSource(InputStream content) {
        this(content, null);
    }

    public GraphContentInputSource(InputStream content, String formatIdentifier) {
        this(content, formatIdentifier, Parser.getInstance());
    }

    public GraphContentInputSource(InputStream content, String formatIdentifier, Parser parser) {
        long before = System.currentTimeMillis();

        if (content == null) throw new IllegalArgumentException("No content supplied");

        // No physical IRI
        bindPhysicalIri(null);
        boolean loaded = false;

        Collection<String> formats;
        if (formatIdentifier == null || "".equals(formatIdentifier.trim())) formats = OntologyUtils
                .getPreferredSupportedFormats(parser.getSupportedFormats());
        else formats = Collections.singleton(formatIdentifier);
        Graph graph = null;
        for (String format : formats) {
            try {
                graph = parser.parse(content, format);
                loaded = true;
                break;
            } catch (UnsupportedFormatException e) {
                log.debug("Parsing format {} failed.", format);
                continue;
            } catch (Exception e) {
                log.error("Error parsing " + format, e);
                continue;
            }
        }
        if (loaded) bindRootOntology(graph);
        log.debug("Input source initialization completed in {} ms.", (System.currentTimeMillis() - before));
    }

    @Override
    public String toString() {
        return "<TRIPLE_COLLECTION_CONTENT>" + OWLUtils.guessOntologyIdentifier(getRootOntology());
    }

}