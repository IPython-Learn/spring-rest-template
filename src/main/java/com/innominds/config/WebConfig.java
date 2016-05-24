package com.innominds.config;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.catalina.connector.Connector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.embedded.EmbeddedServletContainerCustomizer;
import org.springframework.boot.context.embedded.EmbeddedServletContainerFactory;
import org.springframework.boot.context.embedded.tomcat.TomcatEmbeddedServletContainerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

/**
 * If you want to completely suppress the spring boot auto configuration(for webMVC) you need to use @EnableWebMvc <br>
 * and write class that extends WebMvcConfigurer define your own beans so that it will pickup these beans.
 *
 * @author ThirupathiReddy V
 *
 */
@EnableWebMvc
@Configuration
public class WebConfig extends WebMvcConfigurerAdapter {

    /** Reference to logger */
    private static final Logger LOGGER = LoggerFactory.getLogger(WebConfig.class);

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        super.addViewControllers(registry);
        registry.addViewController("/").setViewName("forward:/api/swagger-ui.html");
    }

    @Override
    public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        super.extendMessageConverters(converters);
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        super.addCorsMappings(registry);
        // https://en.wikipedia.org/wiki/Cross-origin_resource_sharing
        registry.addMapping("/*");// all the end-points now can be accisable from other domains
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // TODO Auto-generated method stub
        super.addResourceHandlers(registry);
        registry.addResourceHandler("/corsTest.html").addResourceLocations("classpath:/corsTest.html");
        registry.addResourceHandler("/cors/**").addResourceLocations("classpath:/");

        registry.addResourceHandler("swagger-ui.html").addResourceLocations("classpath:/META-INF/resources/");
        registry.addResourceHandler("/webjars/**").addResourceLocations("classpath:/META-INF/resources/webjars/");
    }

    @Bean
    public EmbeddedServletContainerCustomizer embeddedServletContainerCustomizer() {
        return container -> {
            LOGGER.info("Customizing embeddedServlet container  using port :{} and contextPath :{}", 8080, "/spring-rest-template");
            container.setSessionTimeout(30, TimeUnit.MINUTES);// since we are using hazelcast as token storage this session timeout has no significance
            container.setPort(8080);// on which port embedded tomcat should run
            container.setContextPath("/spring-rest-template");// This is to make in sync with direct TOMCAT deployment and embedded server deployment
        };
    }

    @Bean
    public EmbeddedServletContainerFactory servletContainer() {
        final TomcatEmbeddedServletContainerFactory tomcatFactory = new TomcatEmbeddedServletContainerFactory();

        // tomcatFactory.setAddress(InetAddress.getLocalHost());// you can restrict localhost access
        tomcatFactory.setPort(8080);
        // ServletContainerInitializer

        /** This line has no significance . handled by the addViewControllers() method */
        tomcatFactory.addContextCustomizers(context -> context.addWelcomeFile("/api/swagger-ui.html"));

        try {
            final ClassPathResource classPathResource = new ClassPathResource("keystore");

            /** This code snippet enabled SSL . when you run from executable Jar this is not working. adding if condition to avoid error */
            if (classPathResource.getFile().exists()) {
                final Connector connector = new Connector();
                connector.setPort(8443);
                connector.setSecure(true);
                connector.setScheme("https");
                connector.setProperty("SSLEnabled", "true");
                connector.setProperty("keystorePass", "spring");
                connector.setProperty("keystoreFile", classPathResource.getFile().getAbsolutePath());
                tomcatFactory.addAdditionalTomcatConnectors(connector);
            }

        } catch (final Exception e) {
            LOGGER.debug("Error while loading classpath resource  ", e);
        }

        return tomcatFactory;
    }

}
