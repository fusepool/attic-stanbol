package org.apache.stanbol.ontologymanager.web;

import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.core.Application;

import org.apache.stanbol.kres.jersey.SessionIDResource;
import org.apache.stanbol.kres.jersey.SessionResource;
import org.apache.stanbol.kres.jersey.processors.KReSViewProcessor;
import org.apache.stanbol.kres.jersey.writers.GraphWriter;
import org.apache.stanbol.kres.jersey.writers.OWLOntologyWriter;
import org.apache.stanbol.kres.jersey.writers.ResultSetWriter;

/**
 * Statically define the list of available resources and providers to be used by the KReS JAX-RS Endpoint.
 * 
 * The jersey auto-scan mechanism does not seem to work when deployed through OSGi's HttpService
 * initialization.
 * 
 * In the future this class might get refactored as an OSGi service to allow for dynamic configuration and
 * deployment of additional JAX-RS resources and providers.
 * 
 * @author andrea.nuzzolese
 */

public class JerseyEndpointApplication extends Application {

    @Override
    public Set<Class<?>> getClasses() {
        Set<Class<?>> classes = new HashSet<Class<?>>();

        classes.add(ONMRootResource.class);
        classes.add(ONMScopeResource.class);
        classes.add(ONMScopeOntologyResource.class);
        classes.add(ONMOntResource.class);
        
        classes.add(SessionResource.class);
        classes.add(SessionIDResource.class);
       
        // message body writers
        classes.add(GraphWriter.class);
        classes.add(ResultSetWriter.class);
        // classes.add(OwlModelWriter.class);
        classes.add(OWLOntologyWriter.class);
        return classes;
    }

    @Override
    public Set<Object> getSingletons() {
        Set<Object> singletons = new HashSet<Object>();
        // view processors
        singletons.add(new KReSViewProcessor());
        return singletons;
    }

}
