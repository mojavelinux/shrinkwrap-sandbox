package org.jboss.shrinkwrap.descriptor.mojo;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.jboss.shrinkwrap.api.Asset;

/**
 * Invokes the ShrinkWrap Asset factory method and
 * writes the resulting stream to the target file.
 * 
 * @goal generate
 * @phase process-classes
 */
public class DescriptorGenerator extends AbstractMojo
{
   /**
    * @parameter expression="${project}"
    * @required
    * @readonly
    */
   private MavenProject project;
   
   /**
    * Location of the file.
    * 
    * @parameter
    * @required
    */
   private File target;

   /**
    * @parameter
    * @required
    */
   private String factoryClass;

   public void execute() throws MojoExecutionException
   {
      try
      {
         List runtimeClasspathElements = project.getRuntimeClasspathElements();
         URL[] runtimeUrls = new URL[runtimeClasspathElements.size()];
         for (int i = 0; i < runtimeClasspathElements.size(); i++) {
            String element = (String) runtimeClasspathElements.get(i);
            runtimeUrls[i] = new File(element).toURI().toURL();
         }
         URLClassLoader newLoader = new URLClassLoader(runtimeUrls,
               Thread.currentThread().getContextClassLoader());
         
         Object factory = newLoader.loadClass(factoryClass).newInstance();
         Method createMethod = factory.getClass().getMethod("create");
         Asset asset = (Asset) createMethod.invoke(factory);
         File parent = target.getParentFile();
         if (!parent.exists())
         {
            parent.mkdirs();
         }
         
         InputStream in = asset.openStream();
         int bufferSize = 4096;
         final ByteArrayOutputStream out = new ByteArrayOutputStream(bufferSize * 2);
         final byte[] buffer = new byte[bufferSize];
         int read = 0;
         try {
            while (((read = in.read(buffer)) != -1)) {
               out.write(buffer, 0, read);
            }
         }
         finally {
            in.close();
         }
         
         FileWriter w = null;
         try {
            w = new FileWriter(target);
            w.write(out.toString());
         }
         finally {
            if (w != null)
            {
               try
               {
                  w.close();
               }
               catch (IOException e)
               {
               }
            }
         }
      }
      catch (Exception e)
      {
         throw new MojoExecutionException("Error loading factoryClass " + factoryClass, e);
      }
   }

}
