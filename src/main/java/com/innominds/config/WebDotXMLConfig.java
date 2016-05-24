package com.innominds.config;

import java.util.EnumSet;

import javax.servlet.DispatcherType;
import javax.servlet.Filter;
import javax.servlet.FilterRegistration;
import javax.servlet.HttpConstraintElement;
import javax.servlet.MultipartConfigElement;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;
import javax.servlet.ServletSecurityElement;
import javax.servlet.annotation.ServletSecurity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.filter.DelegatingFilterProxy;
import org.springframework.web.servlet.support.AbstractAnnotationConfigDispatcherServletInitializer;

/**
 * This class executes only if you deploy on external TOMCAT container. <br>
 * If you are running spring boot application property (server.servletPath=/api/*) must set as a property on the class argument.
 *
 * @author ThirupathiReddy V
 *
 */
public class WebDotXMLConfig extends AbstractAnnotationConfigDispatcherServletInitializer {

    /** Reference to logger */
    private static final Logger LOGGER = LoggerFactory.getLogger(WebDotXMLConfig.class);

    /**
     * registering the new servlet and filters as similar to web.xml <servlet> and <filter> tags
     */
    @Override
    public void onStartup(ServletContext servletContext) throws ServletException {
        super.onStartup(servletContext);

        final FilterRegistration.Dynamic securityFilterChain = servletContext.addFilter("springSecurityFilterChain", new DelegatingFilterProxy(
                "springSecurityFilterChain"));

        securityFilterChain.addMappingForUrlPatterns(
                EnumSet.of(DispatcherType.ERROR, DispatcherType.ASYNC, DispatcherType.FORWARD, DispatcherType.INCLUDE, DispatcherType.REQUEST), true,
                new String[] { "/api/*" }); // all /api/* end-points will be handled by this filter

    }

    @Override
    public String getServletName() {
        LOGGER.info("Changing default DispatcherServlet name {} ", "restTemplateServlet");
        return "restTemplateServlet";
    }

    /**
     * This configuration is equals to the applicationContext.xml configuration
     */
    @Override
    protected Class<?>[] getRootConfigClasses() {
        return new Class[] { WebSecurityConfig.class, MySQLDBConfig.class };

    }

    /**
     * this configuration is equals to the dispatch-servlet.xml
     */
    @Override
    protected Class<?>[] getServletConfigClasses() {
        return new Class[] { WebConfig.class };// dispatch-servlet.xml configuration
    }

    /**
     * dispatch-servlet mappings
     */
    @Override
    protected String[] getServletMappings() {
        return new String[] { "/api/*" };
    }

    @Override
    protected void customizeRegistration(ServletRegistration.Dynamic registration) {
        registration.setLoadOnStartup(1);
        registration.setInitParameter("spring.profiles.active", "dev");
        registration.setMultipartConfig(new MultipartConfigElement("/tmp/spittr/uploads"));

        final HttpConstraintElement forceHttpsConstraint = new HttpConstraintElement(ServletSecurity.TransportGuarantee.CONFIDENTIAL, new String[0]);
        final ServletSecurityElement securityElement = new ServletSecurityElement(forceHttpsConstraint);
        registration.setServletSecurity(securityElement);

    }

    @Override
    protected Filter[] getServletFilters() {
        return new Filter[] {};
    }
}