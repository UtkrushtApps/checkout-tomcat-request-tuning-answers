package com.example.tomcat.listener;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.util.logging.Logger;

/**
 * Logs application startup and shutdown so operators can confirm clean
 * context lifecycle transitions during deploys and reloads.
 */
public class AppLifecycleListener implements ServletContextListener {

    private static final Logger LOG = Logger.getLogger(AppLifecycleListener.class.getName());

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        LOG.info("CheckoutWeb context initialized: " + sce.getServletContext().getContextPath());
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        LOG.info("CheckoutWeb context destroyed: " + sce.getServletContext().getContextPath());
    }
}
