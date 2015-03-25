package org.fao.fenix.fmd.storage.services;

import org.fao.fenix.fmd.storage.dao.TestDao;
import org.fao.fenix.fmd.storage.dto.templates.ResponseBeanFactory;
import org.fao.fenix.fmd.storage.dto.templates.admin.TestBean;
import org.fao.fenix.fmd.tools.rest.PATCH;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.Collection;

@Path("championships")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class TestService {
    @Inject
    TestDao dao;


    @GET
    public Collection getTestBeans() throws Exception {
        return ResponseBeanFactory.getInstances(dao.getTestBeans(), TestBean.class, true);
    }

    @GET
    @Path("/{rid}")
    public Object getTestBean(@PathParam("rid") String rid) throws Exception {
        return ResponseBeanFactory.getInstance(dao.getTestBean(rid), TestBean.class, true);
    }

    @POST
    public Object insertTestBean(org.fao.fenix.fmd.storage.dto.full.TestBean championship) throws Exception {
        return ResponseBeanFactory.getInstance(dao.newCustomEntity(championship), TestBean.class, true);
    }

    @PUT
    @Path("/{rid}")
    public Object updateTestBean(@PathParam("rid") String rid, org.fao.fenix.fmd.storage.dto.full.TestBean championship) throws Exception {
        championship.setRID(rid);
        return ResponseBeanFactory.getInstance(dao.saveCustomEntity(championship, true), TestBean.class, true);
    }

    @PATCH
    @Path("/{rid}")
    public Object appendTestBean(@PathParam("rid") String rid, org.fao.fenix.fmd.storage.dto.full.TestBean championship) throws Exception {
        championship.setRID(rid);
        return ResponseBeanFactory.getInstance(dao.saveCustomEntity(championship, false), TestBean.class, true);
    }

    @DELETE
    @Path("/{rid}")
    public Object deleteTestBean(@PathParam("rid") String rid) throws Exception {
        return ResponseBeanFactory.getInstance(dao.deleteTestBean(rid), TestBean.class, true);
    }

}
