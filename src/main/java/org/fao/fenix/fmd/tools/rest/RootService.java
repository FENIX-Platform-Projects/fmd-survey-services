package org.fao.fenix.fmd.tools.rest;

import org.fao.fenix.fmd.tools.utils.FileUtils;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import java.io.IOException;

@Path("/")
public class RootService {
    @Context HttpServletRequest request;

    @Inject private FileUtils fileUtils;

    @GET
    public String info() {
        try {
            return fileUtils.readTextFile(this.getClass().getResourceAsStream("/index.htm"));
        } catch (IOException e) {
            return null;
        }
    }

    @GET
    @Path("/id")
    public String getSessionId() {
        return request!=null ? request.getSession().getId() : null;
    }

}
