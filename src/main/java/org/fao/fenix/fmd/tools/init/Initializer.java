package org.fao.fenix.fmd.tools.init;

import org.fao.fenix.fmd.tools.orient.OrientClient;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.servlet.*;
import javax.servlet.annotation.WebListener;
import java.util.*;

@WebListener
@ApplicationScoped
public class Initializer implements ServletContextListener {
    @Inject private OrientClient orientClient;



    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        ServletContext context = servletContextEvent.getServletContext();
        for (Object key : Collections.list(context.getInitParameterNames()))
            initParameters.setProperty((String)key, context.getInitParameter((String)key));

        orientClient.init(initParameters);
    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {
        orientClient.destroy();
        initParameters.clear();
    }


    //Utils
    private static Properties initParameters = new Properties();
    public Properties getInitParameters() { return initParameters; }
    public String getInitParameter(String key) { return initParameters.getProperty(key); }
}
