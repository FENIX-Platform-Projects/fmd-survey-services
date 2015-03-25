package org.fao.fenix.fmd.tools.orient;

import org.fao.fenix.fmd.storage.dto.templates.ResponseBeanFactory;
import com.orientechnologies.orient.object.db.OObjectDatabaseTx;

import javax.inject.Inject;
import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import java.io.IOException;

@WebFilter(filterName="OrientConnectionManager", urlPatterns={"/*"})
public class OrientConnectionWebFilter implements Filter {
    @Inject
    OrientClient client;
    @Inject
    DatabaseStandards dbParameters;


    @Override public void init(FilterConfig filterConfig) throws ServletException { }
    @Override public void destroy() { }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        OObjectDatabaseTx connection = null;
        try {
            dbParameters.setConnection(connection = client.getConnection());
        } catch (Exception ex) {
            throw new ServletException("Database connection error.", ex);
        }

        try {
            dbParameters.setOrderingInfo(new Order(servletRequest));
            dbParameters.setPaginationInfo(new Page(servletRequest));
            ResponseBeanFactory.template.set(servletRequest.getParameter("template"));

            filterChain.doFilter(servletRequest,servletResponse);

        } finally {
            if (connection!=null)
                connection.close();
        }
    }



}
