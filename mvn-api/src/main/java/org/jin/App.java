package org.jin;

import org.springframework.boot.Banner.Mode;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.util.ClassUtils;

/**
 * Hello world!
 *
 *
 *
 */

//public static final String DEFAULT_CONTEXT_CLASS = "org.springframework.context."
//		+ "annotation.AnnotationConfigApplicationContext";
//
///**
// * The class name of application context that will be used by default for web
// * environments.
// */
//public static final String DEFAULT_WEB_CONTEXT_CLASS = "org.springframework.boot."
//		+ "web.servlet.context.AnnotationConfigServletWebServerApplicationContext";
//
//private static final String[] WEB_ENVIRONMENT_CLASSES = { "javax.servlet.Servlet",
//		"org.springframework.web.context.ConfigurableWebApplicationContext" };
//
///**
// * The class name of application context that will be used by default for reactive web
// * environments.
// */
//public static final String DEFAULT_REACTIVE_WEB_CONTEXT_CLASS = "org.springframework."
//		+ "boot.web.reactive.context.AnnotationConfigReactiveWebServerApplicationContext";
//
//private static final String REACTIVE_WEB_ENVIRONMENT_CLASS = "org.springframework."
//		+ "web.reactive.DispatcherHandler";
//
//private static final String MVC_WEB_ENVIRONMENT_CLASS = "org.springframework."
//		+ "web.servlet.DispatcherServlet";
//
///**
// * Default banner location.
// */
//public static final String BANNER_LOCATION_PROPERTY_VALUE = SpringApplicationBannerPrinter.DEFAULT_BANNER_LOCATION;
//
///**
// * Banner location property key.
// */
//public static final String BANNER_LOCATION_PROPERTY = SpringApplicationBannerPrinter.BANNER_LOCATION_PROPERTY;
//
//private static final String SYSTEM_PROPERTY_JAVA_AWT_HEADLESS = "java.awt.headless";


@SpringBootApplication
public class App {
	
    public static void main( String[] args ){

    	Class<?>[] primarySources = {App.class};
    	SpringApplication app = new SpringApplication(primarySources);
    	app.setBannerMode(Mode.OFF);
    	app.run(args);
    	
//        SpringApplication.run(App.class, args);
    }
    
}
