package com.acme;

import org.jboss.shrinkwrap.api.Asset;
import org.jboss.shrinkwrap.descriptor.api.Descriptors;
import org.jboss.shrinkwrap.descriptor.spec.web.WebAppDef;

public class WebAppFactory
{
   // @Descriptor("${project.build.directory}/${project.build.finalName}/WEB-INF/web.xml")
   public Asset create()
   {
      return Descriptors.create(WebAppDef.class)
            .displayName("My Webapp")
            .facesDevelopmentMode()
            .servlet("javax.faces.webapp.FacesServlet", new String[] { "*.jsf" })
            .sessionTimeout(3600);
   }
}
