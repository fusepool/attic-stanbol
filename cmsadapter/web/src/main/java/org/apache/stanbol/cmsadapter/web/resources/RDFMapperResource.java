package org.apache.stanbol.cmsadapter.web.resources;

import java.io.ByteArrayInputStream;

import javax.servlet.ServletContext;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.clerezza.rdf.core.Graph;
import org.apache.clerezza.rdf.core.serializedform.Parser;
import org.apache.clerezza.rdf.core.serializedform.SupportedFormat;
import org.apache.stanbol.cmsadapter.core.mapping.RDFBridgeManager;
import org.apache.stanbol.cmsadapter.servicesapi.mapping.RDFBridge;
import org.apache.stanbol.cmsadapter.servicesapi.mapping.RDFBridgeException;
import org.apache.stanbol.cmsadapter.servicesapi.model.web.ConnectionInfo;
import org.apache.stanbol.cmsadapter.servicesapi.repository.RepositoryAccessException;
import org.apache.stanbol.commons.web.base.ContextHelper;
import org.apache.stanbol.commons.web.base.resource.BaseStanbolResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This resource is currently used to pass RDF data to CMS Adapter so that RDF data will be annotated with
 * "CMS vocabulary" annotations according to {@link RDFBridge}s. Afterwards, this annotated RDF is transformed
 * into nodes/object in the repository.
 */

@Path("/cmsadapter/rdfmap")
public class RDFMapperResource extends BaseStanbolResource {
    private static final Logger logger = LoggerFactory.getLogger(RDFMapperResource.class);
    private Parser clerezzaParser;
    private RDFBridgeManager bridgeManager;

    public RDFMapperResource(@Context ServletContext context) {
        clerezzaParser = ContextHelper.getServiceFromContext(Parser.class, context);
        bridgeManager = ContextHelper.getServiceFromContext(RDFBridgeManager.class, context);
    }

    /**
     * This service takes credentials as {@link ConnectionInfo} object to access repository and an RDF data
     * first to be annotated and then stored in repository based on the annotations.
     * 
     * @param connectionInfo
     *            is the object that holds all necessary information to connect repository. Example connection
     *            info XML:<br>
     * <br>
     * 
     *            <pre>
     * &lt;?xml version="1.0" encoding="UTF-8"?>
     * &lt;connectionInfo
     *     xmlns="web.model.servicesapi.cmsadapter.stanbol.apache.org">
     *     &lt;repositoryURL>rmi://localhost:1099/crx&lt;/repositoryURL>
     *     &lt;workspaceName>demo&lt;/workspaceName>
     *     &lt;username>admin&lt;/username>
     *     &lt;password>admin&lt;/password>
     *     &lt;connectionType>JCR&lt;/connectionType>
     * &lt;/connectionInfo>
     * </pre>
     * @param serializedGraph
     *            is the serialized RDF graph that is desired to transformed into repository objects
     * @param rootPath
     *            is the path in which root objects in the annotated graph will be created
     */
    /*
     * TODO: It would be wise to get as MGraph in request data. Before that connection info should be get in a
     * different way.
     */
    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response mapRDF(@FormParam("connectionInfo") ConnectionInfo connectionInfo,
                           @FormParam("serializedGraph") String serializedGraph,
                           @QueryParam("rootPath") @DefaultValue("/") String rootPath) {

        if (connectionInfo == null) {
            logger.warn("There is no valid connection info specified");
            return Response.status(Status.BAD_REQUEST).entity("There is no valid connection info specified")
                    .build();
        }
        if (serializedGraph == null) {
            logger.warn("There is no valid RDF data specified");
            return Response.status(Status.BAD_REQUEST).entity("There is no valid RDF data specified").build();
        }

        Graph g = clerezzaParser.parse(new ByteArrayInputStream(serializedGraph.getBytes()),
            SupportedFormat.RDF_XML);
        try {
            bridgeManager.storeRDFToRepository(connectionInfo, rootPath, g);
        } catch (RepositoryAccessException e) {
            logger.warn("Failed to obtain a session from repository", e);
            return Response.status(Status.INTERNAL_SERVER_ERROR)
                    .entity("Failed to obtain a session from repository").build();
        } catch (RDFBridgeException e) {
            logger.warn(e.getMessage());
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
        return Response.ok().build();
    }

}
