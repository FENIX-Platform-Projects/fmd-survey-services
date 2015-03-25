package org.fao.fenix.fmd.tools.rest;

import org.fao.fenix.fmd.storage.dto.JSONdto;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.Provider;
import java.io.IOException;

@Provider
public class ResponseInterceptor implements ContainerResponseFilter {

    @Override
    public void filter(ContainerRequestContext containerRequestContext, final ContainerResponseContext containerResponseContext) throws IOException {
        UriInfo urlInfo = containerRequestContext.getUriInfo();

        //Support standard POST services
        if (containerRequestContext.getMethod().equals("POST") && Response.Status.OK.equals(containerResponseContext.getStatusInfo())) {
            containerResponseContext.setStatus(Response.Status.CREATED.getStatusCode());
            containerResponseContext.getHeaders().putSingle("Link", createGetPath(urlInfo, containerResponseContext.getEntity()));
        }
        //Support void response services
        if (Response.Status.NO_CONTENT.equals(containerResponseContext.getStatusInfo()) && containerResponseContext.getEntityClass()==null)
            containerResponseContext.setStatusInfo(Response.Status.OK);
        //Support paginated select
        if (containerRequestContext.getMethod().equals("GET") && Response.Status.OK.equals(containerResponseContext.getStatusInfo()) && urlInfo!=null && urlInfo.getQueryParameters().get("per_page")!=null)
            containerResponseContext.getHeaders().putSingle("Link", createPagePath(urlInfo));
    }


    //Utils
    private String createGetPath(UriInfo urlInfo, Object entity) {
        String serviceURL = getRelativeURL(urlInfo.getAbsolutePath().toString());
        if (entity!=null && entity instanceof JSONdto)
            return serviceURL+(serviceURL.endsWith("/")?"":'/')+((JSONdto)entity).getRID();
        else
            return null;
    }

    private String createPagePath(UriInfo urlInfo) {
        MultivaluedMap<String,String> queryParameters = urlInfo.getQueryParameters();
        UriBuilder buffer = urlInfo.getRequestUriBuilder();
        buffer.replaceQueryParam("page", queryParameters.containsKey("page") ? Integer.parseInt(queryParameters.get("page").iterator().next())+1 : 2);

        return '<'+getRelativeURL(buffer.toTemplate())+">; rel=\"next\"";
    }

    private String getRelativeURL (String url) {
        int offset = -1;
        for (int i=0; i<5; i++)
            offset = url.indexOf('/',offset+1);
        return url.substring(offset);
    }
}
