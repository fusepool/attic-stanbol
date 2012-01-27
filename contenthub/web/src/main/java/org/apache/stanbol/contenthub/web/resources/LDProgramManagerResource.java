/**
 * 
 */
package org.apache.stanbol.contenthub.web.resources;

import static javax.ws.rs.core.MediaType.APPLICATION_FORM_URLENCODED;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.apache.stanbol.commons.web.base.CorsHelper.addCORSOrigin;
import static org.apache.stanbol.commons.web.base.CorsHelper.enableCORS;

import javax.servlet.ServletContext;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.OPTIONS;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;

import org.apache.stanbol.commons.web.base.ContextHelper;
import org.apache.stanbol.commons.web.base.resource.BaseStanbolResource;
import org.apache.stanbol.contenthub.servicesapi.ldpath.LDPathException;
import org.apache.stanbol.contenthub.servicesapi.ldpath.LDProgramCollection;
import org.apache.stanbol.contenthub.servicesapi.ldpath.LDProgramManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author anil.pacaci
 * @author anil.sinaci
 * 
 */
@Path("/contenthub/ldpath")
public class LDProgramManagerResource extends BaseStanbolResource {

    private static final Logger logger = LoggerFactory.getLogger(LDProgramManagerResource.class);

    private LDProgramManager programManager;

    public LDProgramManagerResource(@Context ServletContext context) {
        programManager = ContextHelper.getServiceFromContext(LDProgramManager.class, context);
        if (programManager == null) {
            logger.error("Missing LDProgramManager = {}", programManager);
            throw new WebApplicationException(404);
        }
    }

    // copied from SiteManagerRootResource
    @OPTIONS
    public Response handleCorsPreflight(@Context HttpHeaders headers) {
        ResponseBuilder res = Response.ok();
        enableCORS(servletContext, res, headers);
        return res.build();
    }
    
    @OPTIONS
    @Path("/program")
    public Response handleCorsPreflightProgram(@Context HttpHeaders headers) {
        ResponseBuilder res = Response.ok();
        enableCORS(servletContext, res, headers);
        return res.build();
    }
    
    @OPTIONS
    @Path("/exists")
    public Response handleCorsPreflightExists(@Context HttpHeaders headers) {
        ResponseBuilder res = Response.ok();
        enableCORS(servletContext, res, headers);
        return res.build();
    }
    
    @GET
    @Produces(APPLICATION_JSON)
    public Response retrieveAllPrograms(@Context HttpHeaders headers) {
        LDProgramCollection ldProgramCollection = programManager.retrieveAllPrograms();
        ResponseBuilder rb = Response.ok(ldProgramCollection);
        addCORSOrigin(servletContext, rb, headers);
        return rb.build();
    }

    @POST
    @Path("/program")
    @Consumes(APPLICATION_FORM_URLENCODED)
    public Response submitProgram(@FormParam("name") String programName,
                                  @FormParam("program") String program,
                                  @Context HttpHeaders headers) throws LDPathException {

        try {
            programManager.submitProgram(programName, program);
        } catch (LDPathException e) {
            logger.error("LDPath program cannot be submitted", e);
            return Response.status(Status.BAD_REQUEST).entity(e).build();
        }
        ResponseBuilder rb = Response
                .ok("LDPath program has been successfully saved and corresponding Solr Core has been successfully created.");
        addCORSOrigin(servletContext, rb, headers);
        return rb.build();
    }
    
    @GET
    @Path("/program")
    public Response getProgramByName(@QueryParam("name") String programName, @Context HttpHeaders headers) {
        String ldPathProgram = programManager.getProgramByName(programName);
        if (ldPathProgram == null) {
            return Response.status(Status.NOT_FOUND).build();
        } else {
            ResponseBuilder rb = Response.ok(ldPathProgram);
            addCORSOrigin(servletContext, rb, headers);
            return rb.build();
        }
    }
    
    @DELETE
    @Path("/program")
    @Consumes(APPLICATION_FORM_URLENCODED)
    public Response deleteProgram(@QueryParam("name") String programName, @Context HttpHeaders headers) {
        programManager.deleteProgram(programName);
        ResponseBuilder rb = Response.ok();
        addCORSOrigin(servletContext, rb, headers);
        return rb.build();
    }

    @GET
    @Path("/exists")
    public Response isManagedProgram(@QueryParam("name") String programName, @Context HttpHeaders headers) {
        if (programManager.isManagedProgram(programName)) {
            ResponseBuilder rb = Response.ok();
            addCORSOrigin(servletContext, rb, headers);
            return rb.build();
        } else {
            ResponseBuilder rb = Response.status(Status.NOT_FOUND);
            addCORSOrigin(servletContext, rb, headers);
            return rb.build();
        }
    }
    
}