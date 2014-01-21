package org.artofsolving.jodconverter.sample.web;

import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.startup.Tomcat;

import javax.servlet.DispatcherType;
import javax.servlet.FilterRegistration;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import java.io.File;
import java.util.EnumSet;

/**
 * Created by Aaron on 12/29/13.
 */
public class EmbeddedServer {

    public static void main(String[] args) throws LifecycleException, InterruptedException, ServletException {

        Tomcat tomcat = new Tomcat();
        tomcat.setPort(9080);
        final File workingDirectory = new File(System.getProperty("user.dir"));
        final File baseDir = (workingDirectory);
        final File appBase = new File(baseDir, "jodconverter-sample-webapp\\src\\main\\webapp");

        tomcat.setBaseDir(baseDir.getAbsolutePath());
        tomcat.getHost().setAppBase(appBase.getAbsolutePath());


        File base = new File(".");
        tomcat.addWebapp(null, "", appBase.getAbsolutePath());
         tomcat.start();
        tomcat.getServer().await();
    }
}
