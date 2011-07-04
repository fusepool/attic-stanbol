package org.apache.stanbol.ontologymanager.web.resources;

import static javax.ws.rs.core.Response.Status.*;

import javax.servlet.ServletContext;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.apache.stanbol.commons.web.base.ContextHelper;
import org.apache.stanbol.commons.web.base.format.KRFormat;
import org.apache.stanbol.commons.web.base.resource.BaseStanbolResource;
import org.apache.stanbol.ontologymanager.ontonet.api.DuplicateIDException;
import org.apache.stanbol.ontologymanager.ontonet.api.ONManager;
import org.apache.stanbol.ontologymanager.ontonet.api.io.BlankOntologySource;
import org.apache.stanbol.ontologymanager.ontonet.api.io.OntologyInputSource;
import org.apache.stanbol.ontologymanager.ontonet.api.io.RootOntologyIRISource;
import org.apache.stanbol.ontologymanager.ontonet.api.ontology.OntologyScope;
import org.apache.stanbol.ontologymanager.ontonet.api.ontology.OntologyScopeFactory;
import org.apache.stanbol.ontologymanager.ontonet.api.ontology.OntologySpace;
import org.apache.stanbol.ontologymanager.ontonet.api.ontology.ScopeRegistry;
import org.apache.stanbol.ontologymanager.ontonet.api.ontology.UnmodifiableOntologySpaceException;
import org.apache.stanbol.ontologymanager.ontonet.api.registry.io.OntologyRegistryIRISource;
import org.apache.stanbol.ontologymanager.ontonet.impl.io.ClerezzaOntologyStorage;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/ontonet/ontology/{scopeid}")
public class ONMScopeResource extends BaseStanbolResource {

	private Logger log = LoggerFactory.getLogger(getClass());

	/*
	 * Placeholder for the ONManager to be fetched from the servlet context.
	 */
	protected ONManager onm;
	
	protected ClerezzaOntologyStorage storage;

	protected ServletContext servletContext;

	public ONMScopeResource(@Context ServletContext servletContext) {
		this.servletContext = servletContext;
		this.onm = (ONManager) ContextHelper.getServiceFromContext(ONManager.class, servletContext);
		this.storage = (ClerezzaOntologyStorage) ContextHelper.getServiceFromContext(ClerezzaOntologyStorage.class, servletContext);
	}

	@DELETE
	public void deregisterScope(@PathParam("scopeid") String scopeid,
			@Context UriInfo uriInfo, @Context HttpHeaders headers,
			@Context ServletContext servletContext) {

		ScopeRegistry reg = onm.getScopeRegistry();

		OntologyScope scope = reg.getScope(IRI
				.create(uriInfo.getAbsolutePath()));
		if (scope == null)
			return;
		reg.deregisterScope(scope);
	}

	@GET
	@Produces(value = { KRFormat.RDF_XML, KRFormat.OWL_XML,
			KRFormat.TURTLE, KRFormat.FUNCTIONAL_OWL,
			KRFormat.MANCHESTER_OWL, KRFormat.RDF_JSON })
	public Response getTopOntology(@Context UriInfo uriInfo,
			@Context HttpHeaders headers, @Context ServletContext servletContext) {

		ScopeRegistry reg = onm.getScopeRegistry();

		OntologyScope scope = reg.getScope(IRI
				.create(uriInfo.getAbsolutePath()));
		if (scope == null)
			return Response.status(404).build();

		OntologySpace cs = scope.getCustomSpace();
		OWLOntology ont = null;
		if (cs != null)
			ont = scope.getCustomSpace().getTopOntology();
		if (ont == null)
			ont = scope.getCoreSpace().getTopOntology();

		return Response.ok(ont).build();

	}

	@POST
	// @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces("text/plain")
	public Response loadCustomOntology(@PathParam("scopeid") String scopeid,
			@FormParam("location") String physIri,
			@FormParam("registry") boolean asRegistry,
			@Context UriInfo uriInfo, @Context HttpHeaders headers,
			@Context ServletContext servletContext) {

		ScopeRegistry reg = onm.getScopeRegistry();

		String res = "";
		IRI scopeiri = null;
		IRI ontoiri = null;
		try {
			scopeiri = IRI.create(uriInfo.getAbsolutePath());
			ontoiri = IRI.create(physIri);
		} catch (Exception ex) {
			// Malformed IRI, throw bad request.
			throw new WebApplicationException(ex, BAD_REQUEST);
		}
		if (reg.containsScope(scopeiri)) {
			res = "Ok, scope is there";
			OntologyScope scope = reg.getScope(scopeiri);
			try {
				OntologyInputSource src = new RootOntologyIRISource(ontoiri);
				OntologySpace space = scope.getCustomSpace();
				if (space == null) {
					space = onm.getOntologySpaceFactory()
							.createCustomOntologySpace(scopeiri, src);

					scope.setCustomSpace(space);
					// space.setUp();
				} else
					space.addOntology(src);
			} catch (OWLOntologyCreationException e) {
				throw new WebApplicationException(e, INTERNAL_SERVER_ERROR);
			} catch (UnmodifiableOntologySpaceException e) {
				throw new WebApplicationException(e, INTERNAL_SERVER_ERROR);
			}
		} else
			throw new WebApplicationException(NOT_FOUND);
		return Response.ok(res).build();
	}

	/**
	 * At least one between corereg and coreont must be present. Registry iris
	 * supersede ontology iris.
	 * 
	 * @param scopeid
	 * @param coreRegistry
	 *            a. If it is a well-formed IRI it supersedes
	 *            <code>coreOntology</code>.
	 * @param coreOntology
	 * @param customRegistry
	 *            a. If it is a well-formed IRI it supersedes
	 *            <code>customOntology</code>.
	 * @param customOntology
	 * @param activate
	 *            if true, the new scope will be activated upon creation.
	 * @param uriInfo
	 * @param headers
	 * @return
	 */
	@PUT
	@Consumes(MediaType.WILDCARD)
	public Response registerScope(@PathParam("scopeid") String scopeid,
			@QueryParam("corereg") String coreRegistry,
			@QueryParam("coreont") String coreOntology,
			@QueryParam("customreg") String customRegistry,
			@QueryParam("customont") String customOntology,
			@DefaultValue("false") @QueryParam("activate") String activate,
			@Context UriInfo uriInfo, @Context HttpHeaders headers,
			@Context ServletContext servletContext) {

		ScopeRegistry reg = onm.getScopeRegistry();
		OntologyScopeFactory f = onm.getOntologyScopeFactory();

		OntologyScope scope;
		OntologyInputSource coreSrc = null, custSrc = null;

		if (coreOntology==null && coreRegistry==null) {
		    coreSrc = new BlankOntologySource();
		}
		
		// First thing, check the core source.
		try {
			coreSrc = new OntologyRegistryIRISource(IRI.create(coreRegistry),
					onm.getOwlCacheManager(), onm.getRegistryLoader());
		} catch (Exception e1) {
			// Bad or not supplied core registry, try the ontology.
			try {
				coreSrc = new RootOntologyIRISource(IRI.create(coreOntology));
			} catch (Exception e2) {
				// If this fails too, throw a bad request.
				throw new WebApplicationException(e2, BAD_REQUEST);
			}
		}

		// Don't bother if no custom was supplied at all...
		if (customOntology != null || customRegistry != null) {
			// ...but if it was, be prepared to throw exceptions.
			try {
				custSrc = new OntologyRegistryIRISource(IRI
						.create(customRegistry), onm.getOwlCacheManager(), onm
						.getRegistryLoader());
			} catch (Exception e1) {
				// Bad or not supplied custom registry, try the ontology.
				try {
					custSrc = new RootOntologyIRISource(IRI
							.create(customOntology));
				} catch (Exception e2) {
					// If this fails too, throw a bad request.
					throw new WebApplicationException(e2, BAD_REQUEST);
				}
			}
		}
		
		// Now the creation.
		try {
			IRI scopeId = IRI.create(uriInfo.getAbsolutePath());
			// Invoke the appropriate factory method depending on the
			// availability of a custom source.
			scope = (custSrc != null) ? f.createOntologyScope(scopeId, coreSrc,
					custSrc) : f.createOntologyScope(scopeId, coreSrc);
			// Setup and register the scope. If no custom space was set, it will
			// still be open for modification.
			scope.setUp();
			reg.registerScope(scope);
			boolean activateBool = true;
			if (activate != null && !activate.equals("")) {
				activateBool = Boolean.valueOf(activate);
			}
			reg.setScopeActive(scopeId, activateBool);
		} catch (DuplicateIDException e) {
			throw new WebApplicationException(e, CONFLICT);
		} catch (Exception ex){
			throw new WebApplicationException(ex, INTERNAL_SERVER_ERROR);
		}

		return Response.ok().build();
	}

}
