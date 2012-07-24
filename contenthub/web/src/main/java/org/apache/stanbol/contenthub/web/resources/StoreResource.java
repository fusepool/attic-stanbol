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

package org.apache.stanbol.contenthub.web.resources;

import static javax.ws.rs.HttpMethod.DELETE;
import static javax.ws.rs.HttpMethod.OPTIONS;
import static javax.ws.rs.HttpMethod.POST;
import static javax.ws.rs.core.MediaType.APPLICATION_FORM_URLENCODED;
import static javax.ws.rs.core.MediaType.MULTIPART_FORM_DATA;
import static javax.ws.rs.core.MediaType.TEXT_HTML;
import static javax.ws.rs.core.MediaType.TEXT_PLAIN_TYPE;
import static javax.ws.rs.core.MediaType.WILDCARD;
import static org.apache.clerezza.rdf.core.serializedform.SupportedFormat.N3;
import static org.apache.clerezza.rdf.core.serializedform.SupportedFormat.N_TRIPLE;
import static org.apache.clerezza.rdf.core.serializedform.SupportedFormat.RDF_JSON;
import static org.apache.clerezza.rdf.core.serializedform.SupportedFormat.RDF_XML;
import static org.apache.clerezza.rdf.core.serializedform.SupportedFormat.TURTLE;
import static org.apache.clerezza.rdf.core.serializedform.SupportedFormat.X_TURTLE;
import static org.apache.stanbol.commons.web.base.CorsHelper.addCORSOrigin;
import static org.apache.stanbol.commons.web.base.CorsHelper.enableCORS;
import static org.apache.stanbol.contenthub.store.file.FileStore.FIELD_ENHANCEMENT_COUNT;
import static org.apache.stanbol.contenthub.store.file.FileStore.FIELD_ID;
import static org.apache.stanbol.contenthub.store.file.FileStore.FIELD_MIME_TYPE;
import static org.apache.stanbol.contenthub.store.file.FileStore.FIELD_TITLE;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.net.ssl.SSLEngineResult.Status;
import javax.servlet.ServletContext;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.OPTIONS;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.UriInfo;

import org.apache.clerezza.rdf.core.UriRef;
import org.apache.clerezza.rdf.core.access.TcManager;
import org.apache.clerezza.rdf.core.serializedform.Serializer;
import org.apache.clerezza.rdf.core.serializedform.SupportedFormat;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.stanbol.commons.web.base.ContextHelper;
import org.apache.stanbol.commons.web.base.resource.BaseStanbolResource;
import org.apache.stanbol.contenthub.servicesapi.store.Store;
import org.apache.stanbol.contenthub.servicesapi.store.StoreException;
import org.apache.stanbol.contenthub.store.file.FileStore;
import org.apache.stanbol.enhancer.servicesapi.ContentItem;
import org.apache.stanbol.enhancer.servicesapi.ContentItemFactory;
import org.apache.stanbol.enhancer.servicesapi.EngineException;
import org.apache.stanbol.enhancer.servicesapi.helper.ContentItemHelper;
import org.apache.stanbol.enhancer.servicesapi.impl.ByteArraySource;
import org.apache.stanbol.enhancer.servicesapi.impl.StringSource;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.osgi.framework.InvalidSyntaxException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.jersey.api.view.Viewable;
import com.sun.jersey.core.header.FormDataContentDisposition;
import com.sun.jersey.multipart.FormDataBodyPart;
import com.sun.jersey.multipart.FormDataParam;

/**
 * Resource to provide a CRU[D] REST API for content items and there related enhancements.
 * <p>
 * Creation is achieved using either POST requests on the root of the store or as PUT requests on the expected
 * content item URI.
 * <p>
 * Retrieval is achieved using simple GET requests on the content item or enhancement public URIs.
 * <p>
 * Update is achieved by issue a PUT request on an existing content item public URI.
 */
@Path("/contenthub/store")
public class StoreResource extends BaseStanbolResource {

    public static final Set<String> RDF_MEDIA_TYPES = new TreeSet<String>(Arrays.asList(N3, N_TRIPLE,
        RDF_XML, TURTLE, X_TURTLE, RDF_JSON));

    private static final Logger log = LoggerFactory.getLogger(StoreResource.class);

    private TcManager tcManager;

    //TODO: make shure that the store is of typeContentItem
    private Store<ContentItem> store;

    private ContentItemFactory cif;

    private Serializer serializer;

    private UriInfo uriInfo;

    private int offset = 0;

    private int pageSize = 5;

    private List<RecentlyEnhanced> recentlyEnhancedItems;

    public class RecentlyEnhanced {
        private String id;
        private String dereferencableURI;
        private String mimetype;
        private long enhancementCount;
        private String title;

        public RecentlyEnhanced(String uri,
                                String dereferencableURI,
                                String mimeType,
                                long enhancementCount,
                                String title) {
            this.id = uri;
            this.dereferencableURI = dereferencableURI;
            this.mimetype = mimeType;
            this.title = (title == null || title.trim().equals("") ? id : title);
            this.enhancementCount = enhancementCount;
        }

        public String getLocalId() {
            return this.id;
        }

        public String getDereferencableURI() {
            return this.dereferencableURI;
        }

        public String getMimetype() {
            return this.mimetype;
        }

        public long getEnhancementCount() {
            return this.enhancementCount;
        }

        public String getTitle() {
            return this.title;
        }
    }

    /**
     * 
     * @param context
     * @param uriInfo
     */
    public StoreResource(@Context ServletContext context, @Context UriInfo uriInfo) {

        this.tcManager = ContextHelper.getServiceFromContext(TcManager.class, context);
        //TODO: this has the assumption that there is only a single Store instance.
        //      The store should be referenced by a name
        this.store = ContextHelper.getServiceFromContext(Store.class, context);
        if (this.store == null) {
            log.error("Missing Store Service");
            throw new WebApplicationException(404);
        }
        if(ContentItem.class.isAssignableFrom(store.getClass())){
        	throw new WebApplicationException(Response.status(Response.Status.INTERNAL_SERVER_ERROR)
        			.entity("Store instance is not compatible with ContentItem!").build());
        }
        this.cif = ContextHelper.getServiceFromContext(ContentItemFactory.class, context);
        this.serializer = ContextHelper.getServiceFromContext(Serializer.class, context);
        this.uriInfo = uriInfo;

    }

    @OPTIONS
    public Response handleCorsPreflight(@Context HttpHeaders headers) {
        ResponseBuilder res = Response.ok();
        enableCORS(servletContext, res, headers);
        return res.build();
    }

    @OPTIONS
    @Path("/content/{uri:.+}")
    public Response handleCorsPreflightContent(@Context HttpHeaders headers) {
        ResponseBuilder res = Response.ok();
        enableCORS(servletContext, res, headers);
        return res.build();
    }

    @OPTIONS
    @Path("/download/{type}/{uri:.+}")
    public Response handleCorsPreflightDownload(@Context HttpHeaders headers) {
        ResponseBuilder res = Response.ok();
        enableCORS(servletContext, res, headers);
        return res.build();
    }

    @OPTIONS
    @Path("/metadata/{uri:.+}")
    public Response handleCorsPreflightMetadata(@Context HttpHeaders headers) {
        ResponseBuilder res = Response.ok();
        enableCORS(servletContext, res, headers);
        return res.build();
    }

    @OPTIONS
    @Path("/raw/{uri:.+}")
    public Response handleCorsPreflightRaw(@Context HttpHeaders headers) {
        ResponseBuilder res = Response.ok();
        enableCORS(servletContext, res, headers);
        return res.build();
    }

    @OPTIONS
    @Path("/{uri:.+}")
    public Response handleCorsPreflightURI(@Context HttpHeaders headers) {
        ResponseBuilder res = Response.ok();
        enableCORS(servletContext, res, headers, POST, DELETE, OPTIONS);
        return res.build();
    }

    /*
     * Methods for retrieving various parts e.g raw content, metadata of content items
     */

    /**
     * Cool URI handler for the uploaded resource.
     * 
     * @param contentURI
     *            The URI of the resource in the Stanbol Contenthub store
     * @param headers
     *            HTTP headers
     * @return a redirection to either a browser view, the RDF metadata or the raw binary content
     */
    @GET
    @Path("/content/{uri:.+}")
    public Response getContent(@PathParam(value = "uri") String contentURI, @Context HttpHeaders headers) throws StoreException {

        ContentItem ci = store.get(contentURI);
        if (ci == null) {
            throw new WebApplicationException(404);
        }

        // handle smart redirection to browser view
        for (MediaType mt : headers.getAcceptableMediaTypes()) {
            if (mt.toString().startsWith(TEXT_HTML)) {
                URI pageUri = uriInfo.getBaseUriBuilder().path("/contenthub").path("store/page")
                        .path(contentURI).build();
                ResponseBuilder rb = Response.temporaryRedirect(pageUri);
                addCORSOrigin(servletContext, rb, headers);
                return rb.build();
            }
        }

        // handle smart redirection to RDF metadata view
        for (MediaType mt : headers.getAcceptableMediaTypes()) {
            if (RDF_MEDIA_TYPES.contains(mt.toString())) {
                URI metadataUri = uriInfo.getBaseUriBuilder().path("/contenthub").path("store/metadata")
                        .path(contentURI).build();
                ResponseBuilder rb = Response.temporaryRedirect(metadataUri);
                addCORSOrigin(servletContext, rb, headers);
                return rb.build();
            }
        }
        URI rawUri = uriInfo.getBaseUriBuilder().path("/contenthub").path("store/raw").path(contentURI)
                .build();
        ResponseBuilder rb = Response.temporaryRedirect(rawUri);
        addCORSOrigin(servletContext, rb, headers);
        return rb.build();
    }

    /**
     * HTTP GET method specific for download operations. Raw data (content item) or only metadata of the
     * content item can be downloaded.
     * 
     * @param type
     *            Type can be {@code "metadata"} or {@code "raw"}. Based on the type, related parts of the
     *            content item will be prepared for download.
     * @param contentURI
     *            URI of the resource in the Stanbol Contenthub store
     * @return Raw content item or metadata of the content item.
     * @throws IOException
     * @throws StoreException
     */
    @GET
    @Path("/download/{type}/{uri:.+}")
    public Response downloadContentItem(@PathParam(value = "type") String type,
                                        @PathParam(value = "uri") String contentURI,
                                        @QueryParam(value = "format") String format,
                                        @Context HttpHeaders headers) throws IOException, StoreException {

        ContentItem ci = store.get(contentURI);
        if (ci == null) {
            throw new WebApplicationException(404);
        }
        if (type.equals("metadata")) {
            String fileName = URLEncoder.encode(contentURI, "utf-8") + "-metadata";
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            serializer.serialize(baos, ci.getMetadata(), format);
            InputStream is = new ByteArrayInputStream(baos.toByteArray());

            ResponseBuilder response = Response.ok((Object) is);
            response.header("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
            response.type("text/plain");
            addCORSOrigin(servletContext, response, headers);
            return response.build();
        } else if (type.equals("raw")) {
            String fileName = URLEncoder.encode(contentURI, "utf-8") + "-raw";
            ResponseBuilder response = Response.ok((Object) ci.getStream());
            response.header("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
            response.type(ci.getMimeType());
            addCORSOrigin(servletContext, response, headers);
            return response.build();
        } else {
            throw new WebApplicationException(404);
        }

    }

    /**
     * HTTP GET method to retrieve the metadata of the content item. Generally, metadata contains the
     * enhancements of the content item.
     * 
     * @param contentURI
     *            URI id of the resource in the Stanbol Contenthub store
     * @return RDF representation of the metadata of the content item.
     * @throws IOException
     * @throws StoreException
     */
    @GET
    @Path("/metadata/{uri:.+}")
    public Response getContentItemMetaData(@PathParam(value = "uri") String contentURI,
                                           @Context HttpHeaders headers) throws IOException, StoreException {
        ContentItem ci = store.get(contentURI);
        if (ci == null) {
            throw new WebApplicationException(404);
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        serializer.serialize(out, ci.getMetadata(), SupportedFormat.RDF_XML);
        ResponseBuilder rb = Response.ok(out.toString(), "text/plain");
        addCORSOrigin(servletContext, rb, headers);
        return rb.build();
    }

    /**
     * HTTP GET method to retrieve the raw content item.
     * 
     * @param contentURI
     *            URI of the resource in the Stanbol Contenthub store
     * @return Raw data of the content item.
     * @throws IOException
     * @throws StoreException
     */
    @GET
    @Path("/raw/{uri:.+}")
    public Response getRawContent(@PathParam(value = "uri") String contentURI, @Context HttpHeaders headers) throws IOException,
                                                                                                            StoreException {
        ContentItem ci = store.get(contentURI);
        if (ci == null) {
            throw new WebApplicationException(404);
        }
        ResponseBuilder rb = Response.ok(ci.getStream(), ci.getMimeType());
        addCORSOrigin(servletContext, rb, headers);
        return rb.build();
    }

    /*
     * Services for content item creation
     */
    /**
     * HTTP POST method to create a content item in Contenthub. This is the very basic method to create the
     * content item. The payload of the POST method should include the raw data of the content item to be
     * created.
     * 
     * @param data
     *            Raw data of the content item
     * @param headers
     *            HTTP Headers
     * @return Redirects to "contenthub/store/content/uri" which shows the content item in the HTML view.
     * @throws URISyntaxException
     * @throws EngineException
     * @throws StoreException
     */
    @POST
    @Consumes(WILDCARD + ";qs=0.5")
    public Response createContentItem(byte[] data, @Context HttpHeaders headers) throws URISyntaxException,
                                                                                EngineException,
                                                                                StoreException {
        String contentURI = ContentItemHelper.makeDefaultUrn(data).getUnicodeString();
        return createEnhanceAndRedirect(data, headers.getMediaType(), contentURI, headers);
    }

    /**
     * HTTP POST method to create a content item in Contenthub.
     * 
     * @param contentURI
     *            URI for the content item. If not supplied, Contenthub automatically assigns a unique ID
     *            (uri) to the content item.
     * @param data
     *            Raw data of the content item
     * @param headers
     *            HTTP headers
     * @return Redirects to "contenthub/store/content/uri" which shows the content item in the HTML view.
     * @throws URISyntaxException
     * @throws EngineException
     * @throws StoreException
     */
    @POST
    @Path("/{uri:.+}")
    @Consumes(WILDCARD)
    public Response createContentItemWithId(@PathParam(value = "uri") String contentURI,
                                            byte[] data,
                                            @Context HttpHeaders headers) throws URISyntaxException,
                                                                         EngineException,
                                                                         StoreException {
        return createEnhanceAndRedirect(data, headers.getMediaType(), contentURI, headers);
    }

    /**
     * HTTP POST method to create a content item in Contenthub. This method requires the content to be
     * text-based.
     * 
     * @param content
     *            Actual content in text format. If this parameter is supplied, {@link url} is ommitted.
     * @param url
     *            URL where the actual content resides. If this parameter is supplied (and {@link content} is
     *            {@code null}, then the content is retrieved from this url.
     * @param constraints
     *            Constraints in JSON format. Constraints are used to add supplementary metadata to the
     *            content item. For example, author of the content item may be supplied as {author:
     *            "John Doe"}. Then, this constraint is added to the Solr and will be indexed if the
     *            corresponding Solr schema includes the author field. Solr indexed can be created/adjusted
     *            through LDPath programs.
     * @param title
     *            The title for the content item. Titles can be used to present summary of the actual content.
     *            For example, search results are presented by showing the titles of resultant content items.
     * @param headers
     *            HTTP headers (optional)
     * @return Redirects to "contenthub/store/content/uri" which shows the content item in the HTML view.
     * @throws URISyntaxException
     * @throws EngineException
     * @throws MalformedURLException
     * @throws IOException
     * @throws StoreException
     */
    @POST
    @Consumes(APPLICATION_FORM_URLENCODED)
    public Response createContentItemFromForm(@FormParam("content") String content,
                                              @FormParam("url") String url,
                                              @FormParam("constraints") String constraints,
                                              @FormParam("title") String title,
                                              @Context HttpHeaders headers) throws URISyntaxException,
                                                                           EngineException,
                                                                           MalformedURLException,
                                                                           IOException,
                                                                           StoreException {

        return createContentItemFromForm(content, url, null, null, null, headers, constraints, title);
    }

    /**
     * HTTP POST method to create a content item from file. File is read and loaded as the actual content.
     * 
     * @param file
     *            {@link File} which contains the content for the content item.
     * @param disposition
     *            Additional information about the {@link file} parameter
     * @param constraints
     *            Constraints in JSON format. Constraints are used to add supplementary metadata to the
     *            content item. For example, author of the content item may be supplied as {author:
     *            "John Doe"}. Then, this constraint is added to the Solr and will be indexed if the
     *            corresponding Solr schema includes the author field. Solr indexed can be created/adjusted
     *            through LDPath programs.
     * @param title
     *            The title for the content item. Titles can be used to present summary of the actual content.
     *            For example, search results are presented by showing the titles of resultant content items.
     * @param headers
     *            HTTP headers (optional)
     * @return Redirects to "contenthub/store/content/uri" which shows the content item in the HTML view.
     * @throws URISyntaxException
     * @throws EngineException
     * @throws MalformedURLException
     * @throws IOException
     * @throws StoreException
     */
    @POST
    @Consumes(MULTIPART_FORM_DATA)
    public Response createContentItemFromForm(@FormDataParam("file") File file,
                                              @FormDataParam("file") FormDataContentDisposition disposition,
                                              @FormDataParam("file") FormDataBodyPart body,
                                              @FormDataParam("constraints") String constraints,
                                              @FormDataParam("title") String title,
                                              @Context HttpHeaders headers) throws URISyntaxException,
                                                                           EngineException,
                                                                           MalformedURLException,
                                                                           IOException,
                                                                           StoreException {
        return createContentItemFromForm(null, null, file, disposition, body, headers, constraints, title);
    }

    private Response createContentItemFromForm(String content,
                                               String url,
                                               File file,
                                               FormDataContentDisposition disposition,
                                               FormDataBodyPart body,
                                               HttpHeaders headers,
                                               String constraints,
                                               String title) throws URISyntaxException,
                                                            EngineException,
                                                            MalformedURLException,
                                                            IOException,
                                                            StoreException {
        byte[] data = null; // TODO: rewrite me in a streamable way to avoid
        // buffering all the content in memory
        MediaType mt = null;
        if (content != null && !content.trim().isEmpty()) {
            data = content.getBytes();
            mt = TEXT_PLAIN_TYPE;
        } else if (url != null && !url.trim().isEmpty()) {
            try {
                URLConnection uc = (new URL(url)).openConnection();
                data = IOUtils.toByteArray(uc.getInputStream());
                mt = MediaType.valueOf(uc.getContentType());
            } catch (IOException e) {
                // TODO: collect remote resource error message for user friendly
                // feedback
            }
        } else if (file != null) {
            mt = body.getMediaType();
            data = FileUtils.readFileToByteArray(file);
            if (title == null || title.isEmpty()) {
                title = disposition.getFileName();
            }
        }

        if (data != null && mt != null) {
            String uri = ContentItemHelper.makeDefaultUrn(data).getUnicodeString();
            return createEnhanceAndRedirect(data, mt, uri, true, constraints, title, headers);
        } else {
            // TODO: add user-friendly feedback on empty requests from a form
            ResponseBuilder rb = Response.seeOther(new URI("/contenthub/store"));
            addCORSOrigin(servletContext, rb, headers);
            return rb.build();
        }
    }

    /*
     * This method takes "title" argument as well as "constraints" so that it would be easy to specify title
     * while calling RESTful services.
     */
    private Response createEnhanceAndRedirect(byte[] content,
                                              MediaType mediaType,
                                              String contentURI,
                                              boolean useExplicitRedirect,
                                              String constraints,
                                              String title,
                                              HttpHeaders headers) throws EngineException,
                                                                  URISyntaxException,
                                                                  StoreException {

        ContentItem ci = null;
        try {
            ci = cif.createContentItem(new UriRef(contentURI),
                new ByteArraySource(content, mediaType.toString()));
            if (constraints != null && !constraints.trim().equals("")) {
                ci.addPart(FileStore.CONSTRAINTS_URI, cif.createBlob(new StringSource(constraints)));
            }
            if (title != null && !title.trim().equals("")) {
                JSONObject htmlMetadata = new JSONObject();
                try {
                    htmlMetadata.put("title", title);
                    ci.addPart(FileStore.HTMLMETADATA_URI,
                        cif.createBlob(new StringSource(htmlMetadata.toString())));
                } catch (JSONException e) {
                    log.error("Failed to add title to HTML metadata");
                }
            }
        } catch (IOException e) {
            log.error("Failed to create the ContentItem", e);
            throw new StoreException("Failed to create the ContentItem", e);
        }
        store.put(ci);

        if (useExplicitRedirect) {
            // use an redirect to point browsers to newly created content
            ResponseBuilder rb = Response.seeOther(makeRedirectionURI(ci.getUri().getUnicodeString()));
            addCORSOrigin(servletContext, rb, headers);
            return rb.build();
        } else {
            // use the correct way of notifying the RESTful client that the
            // resource has been successfully created
            ResponseBuilder rb = Response.created(makeRedirectionURI(ci.getUri().getUnicodeString()));
            addCORSOrigin(servletContext, rb, headers);
            return rb.build();
        }
    }

    private URI makeRedirectionURI(String contentURI) throws URISyntaxException {
        return new URI(uriInfo.getBaseUri() + "contenthub/store/content/" + contentURI);
    }

    /**
     * HTTP DELETE method to delete a content item from Contenhub.
     * 
     * @param contentURI
     *            URI of the content item to be deleted.
     * @return HTTP OK
     * @throws StoreException
     */
    @DELETE
    @Path("/{uri:.+}")
    public Response deleteContentItem(@PathParam(value = "uri") String contentURI,
                                      @Context HttpHeaders headers) throws StoreException {
        if(store.get(contentURI) == null){
            throw new WebApplicationException(404);
        }
        store.remove(contentURI);
        ResponseBuilder rb = Response.ok();
        addCORSOrigin(servletContext, rb, headers);
        return rb.build();
    }

    private Response createEnhanceAndRedirect(byte[] data,
                                              MediaType mediaType,
                                              String contentURI,
                                              HttpHeaders headers) throws EngineException,
                                                                  URISyntaxException,
                                                                  StoreException {
        return createEnhanceAndRedirect(data, mediaType, contentURI, false, null, null, headers);
    }

    /*
     * Services to draw HTML view
     */
    @GET
    @Produces(TEXT_HTML + ";qs=2")
    public Viewable getView(@Context ServletContext context,
                            @QueryParam(value = "offset") int offset,
                            @QueryParam(value = "pageSize") @DefaultValue("5") int pageSize) throws IllegalArgumentException,
                                                                                            IOException,
                                                                                            InvalidSyntaxException,
                                                                                            StoreException {
        this.offset = offset;
        this.pageSize = pageSize;
        this.recentlyEnhancedItems = new ArrayList<RecentlyEnhanced>();

        List<JSONObject> recentlyEnhancedList = new ArrayList<JSONObject>();
        try {
            recentlyEnhancedList = ((FileStore) store).getRecentlyEnhancedItems(pageSize + 1, offset);
        } catch (StoreException e) {
            log.error("Store cannot be casted to FileStore");
        }
        try {
            for (JSONObject recentlyEnhancedItem : recentlyEnhancedList) {

                String id = recentlyEnhancedItem.getString(FIELD_ID);
                String title;
                String dereferencableURI = getPublicBaseUri() != null ? (getPublicBaseUri()
                                                                         + "contenthub/store/content/" + id)
                        : null;

                try {
                    title = recentlyEnhancedItem.getString(FIELD_TITLE);
                } catch (JSONException e1) {
                    title = id;
                }
                recentlyEnhancedItems.add(new RecentlyEnhanced(id, dereferencableURI, recentlyEnhancedItem
                        .getString(FIELD_MIME_TYPE), recentlyEnhancedItem.getLong(FIELD_ENHANCEMENT_COUNT),
                        title));
            }
        } catch (JSONException e) {
            throw new StoreException("Failed to get recently enhanced items", e);
        }
        return new Viewable("index", this);
    }

    @Path("/page/{uri:.+}")
    @Produces(TEXT_HTML)
    public ContentItemResource getContentItemView(@PathParam(value = "uri") String contentURI) throws IOException,
                                                                                              StoreException {
        ContentItem ci = store.get(contentURI);
        if (ci == null) {
            throw new WebApplicationException(404);
        }
        return new ContentItemResource(contentURI, ci, uriInfo, "/contenthub/store/download", tcManager,
                serializer, servletContext);
    }

    // Helper methods for HTML view

    public List<RecentlyEnhanced> getRecentlyEnhancedItems() {
        if (recentlyEnhancedItems.size() > pageSize) {
            return recentlyEnhancedItems.subList(0, pageSize);
        } else {
            return recentlyEnhancedItems;
        }
    }

    public URI getMoreRecentItemsUri() {
        if (offset >= pageSize) {
            return uriInfo.getBaseUriBuilder().path("contenthub").path("store")
                    .queryParam("offset", offset - pageSize).build();
        } else {
            return null;
        }
    }

    public URI getOlderItemsUri() {
        if (recentlyEnhancedItems.size() <= pageSize) {
            return null;
        } else {
            return uriInfo.getBaseUriBuilder().path("contenthub").path("store")
                    .queryParam("offset", offset + pageSize).build();
        }
    }
}