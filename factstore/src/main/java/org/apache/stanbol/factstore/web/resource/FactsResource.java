package org.apache.stanbol.factstore.web.resource;

import static javax.ws.rs.core.MediaType.TEXT_HTML;

import javax.servlet.ServletContext;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.stanbol.commons.jsonld.JsonLdProfile;
import org.apache.stanbol.commons.jsonld.JsonLdProfileParser;
import org.apache.stanbol.commons.web.base.ContextHelper;
import org.apache.stanbol.commons.web.base.resource.BaseStanbolResource;
import org.apache.stanbol.factstore.api.FactStore;
import org.apache.stanbol.factstore.model.FactSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.jersey.api.view.Viewable;

@Path("/factstore/facts")
public class FactsResource extends BaseStanbolResource {

    private static Logger logger = LoggerFactory.getLogger(FactsResource.class);

    private final FactStore factStore;

    public FactsResource(@Context ServletContext context) {
        this.factStore = ContextHelper.getServiceFromContext(FactStore.class, context);
    }

    @GET
    @Produces(MediaType.TEXT_HTML)
    public Response get() {
        return Response.ok(new Viewable("index", this), TEXT_HTML).build();
    }

    @GET
    @Path("/{factSchemaURN}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getFactSchema(@PathParam("factSchemaURN") String factSchemaURN) {
        Response validationResponse = standardValidation(factSchemaURN);
        if (validationResponse != null) {
            return validationResponse;
        }

        FactSchema factSchema = this.factStore.getFactSchema(factSchemaURN);
        if (factSchema == null) {
            return Response.status(Status.NOT_FOUND).entity("Could not find fact schema " + factSchemaURN)
                    .build();
        }

        return Response.ok(factSchema.toJsonLdProfile().toString()).build();
    }

    @PUT
    @Path("/{factSchemaURN}")
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.APPLICATION_JSON)
    public Response putFactSchema(String jsonLdProfileString, @PathParam("factSchemaURN") String factSchemaURN) {
        Response validationResponse = standardValidation(factSchemaURN);
        if (validationResponse != null) {
            return validationResponse;
        }

        JsonLdProfile profile = null;
        try {
            profile = JsonLdProfileParser.parseProfile(jsonLdProfileString);
        } catch (Exception e) { /* ignore this exception here - it was logged by the parser */}

        if (profile == null) {
            return Response.status(Status.BAD_REQUEST).entity(
                "Could not parse provided JSON-LD Profile structure.").build();
        }

        try {
            if (this.factStore.existsFactSchema(factSchemaURN)) {
                return Response.status(Status.CONFLICT).entity(
                    "The fact schema " + factSchemaURN + " already exists.").build();
            }
        } catch (Exception e) {
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(
                "Error while checking existence of fact schema " + factSchemaURN).build();
        }

        try {
            this.factStore.createFactSchema(FactSchema.fromJsonLdProfile(factSchemaURN, profile));
        } catch (Exception e) {
            logger.error("Error creating new fact schema {}", factSchemaURN, e);
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(
                "Error while creating new fact in database.").build();
        }

        return Response.status(Status.CREATED).build();
    }

    private Response standardValidation(String factSchemaURN) {
        if (this.factStore == null) {
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(
                "The FactStore is not configured properly").build();
        }

        if (factSchemaURN == null || factSchemaURN.isEmpty()) {
            return Response.status(Status.BAD_REQUEST).entity("No fact schema URN specified.").build();
        }

        if (factSchemaURN.length() > this.factStore.getMaxFactSchemaURNLength()) {
            return Response.status(Status.BAD_REQUEST).entity(
                "The fact schema URN " + factSchemaURN + " is too long. A maximum of "
                        + this.factStore.getMaxFactSchemaURNLength() + " characters is allowed").build();
        }

        return null;
    }
}
