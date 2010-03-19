// See the COPYRIGHT file for copyright and license information
package org.znerd.logdoc;

import java.io.File;
import java.io.InputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import static org.znerd.logdoc.internal.InternalLogging.log;

/**
 * Log definition. Typically read from a <code>log.xml</code> file.
 *
 * @author <a href="mailto:ernst@ernstdehaan.com">Ernst de Haan</a>
 */
public final class LogDef {

   //-------------------------------------------------------------------------
   // Class functions
   //-------------------------------------------------------------------------

   /**
    * Loads a log definition from a specified directory.
    *
    * @param dir
    *    the directory to load the log definition from,
    *    cannot be <code>null</code>.
    *
    * @throws IllegalArgumentException
    *    if <code>dir == null</code>, or if it is not a directory.
    *
    * @throws IOException
    *    if the definition could not be loaded.
    */
   public static final LogDef loadFromDirectory(File dir)
   throws IllegalArgumentException, IOException {
      return new LogDef(dir);
   }


   //-------------------------------------------------------------------------
   // Constructors
   //-------------------------------------------------------------------------

   /**
    * Constructs a new <code>LogDef</code>.
    *
    * @param dir
    *    the directory to load the log definition from,
    *    cannot be <code>null</code>.
    *
    * @throws IllegalArgumentException
    *    if <code>dir == null</code>, or if it is not a directory.
    *
    * @throws IOException
    *    if the definition(s) could not be loaded.
    */
   private LogDef(File dir)
   throws IllegalArgumentException, IOException {

      // Check preconditions
      if (dir == null) {
         throw new IllegalArgumentException("dir == null");
      } else if (! dir.isDirectory()) {
         throw new IllegalArgumentException("Path (\"" + dir.getPath() + "\") is not a directory.");
      }
      
      // Create a resolver for the specified input directory
      _resolver = new Resolver(dir);
      
      // Load the log.xml file
      _xml = _resolver.loadInputDocument("log.xml");
      
      // Parse the domain name and determine access level
      Element docElem = _xml.getDocumentElement();
      _domainName     = docElem.getAttribute("domain");
      _public         = Boolean.parseBoolean(docElem.getAttribute("public"));
            
      // Load the translation bundles
      _translations = new HashMap<String,Document>();
      NodeList elems = docElem.getElementsByTagName("translation-bundle");
      for (int index = 0; index < elems.getLength(); index++) {
         Element elem = (Element) elems.item(index);
         String locale = elem.getAttribute("locale");
 
         Document tbXML = _resolver.loadInputDocument("translation-bundle-" + locale + ".xml");
         _translations.put(locale, tbXML);
      }
   }


   //-------------------------------------------------------------------------
   // Fields
   //-------------------------------------------------------------------------

   /**
    * The resolver that can resolve input files and XSLT files.
    * Never <code>null</code>.
    */
   private final Resolver _resolver;

   /**
    * The source file as a DOM document. Never <code>null</code>.
    */
   private final Document _xml;
   
   /**
    * The domain name. Never <code>null</code>.
    */
   private final String _domainName;
   
   /**
    * Flag that indicates if the generated code should be considered
    * accessible even outside its own domain/namespace.
    */
   private final boolean _public;
   
   /**
    * The translation bundles, indexed by name. Never <code>null</code>.
    */
   private final Map<String,Document> _translations;


   //-------------------------------------------------------------------------
   // Methods
   //-------------------------------------------------------------------------

   /**
    * Generates the Java code for this log definition.
    *
    * @param targetDir
    *    the target directory to create the Java source files in,
    *    cannot be <code>null</code>, and must be an existent writable
    *    directory.
    *
    * @throws IllegalArgumentException
    *    if <code>targetDir == null</code>.
    *
    * @throws IOException
    *    if the Java code could not be generated.
    */
   public void generateCode(File targetDir)
   throws IllegalArgumentException, IOException {

      // Check preconditions
      if (targetDir == null) {
         throw new IllegalArgumentException("targetDir == null");
      }

      // Perform transformations
      transform(targetDir, "Log");
      transform(targetDir, "TranslationBundle");
      for (String locale : _translations.keySet()) {
         transformForLocale(targetDir, locale);
      }
   }

   private Source getSource() {
      return new DOMSource(_xml);
   }
   
   private Source getTranslationBundleSource(String locale) {
	   return new DOMSource(_translations.get(locale));
   }

   private void transform(File baseDir, String className)
   throws IOException {

      try {

         // Create an XSLT Transforer
         String                   xsltPath = "xslt/log_to_" + className + "_java.xslt";
         InputStream            xsltStream = Library.getMetaResourceAsStream(xsltPath);
         StreamSource     xsltStreamSource = new StreamSource(xsltStream);
         TransformerFactory xformerFactory = TransformerFactory.newInstance();
         xformerFactory.setURIResolver(_resolver);
         Transformer               xformer = xformerFactory.newTransformer(xsltStreamSource);

         // Set the parameters for the template
         xformer.setParameter("package_name", _domainName);
         xformer.setParameter("accesslevel",  _public ? "public" : "protected");

         // Make sure the output directory exists
         String     domainPath = _domainName.replace(".", "/");
         File           outDir = new File(baseDir, domainPath);
         if (! outDir.exists()) {
            boolean outDirCreated = outDir.mkdirs();
            if (! outDirCreated) {
               throw new IOException("Failed to create output directory \"" + outDir.getPath() + "\".");
            }
         } else if (! outDir.isDirectory()) {
            throw new IOException("Path \"" + outDir.getPath() + "\" exists, but it is not a directory.");
         }

         // Declare where the XSLT output should go
         File        outFile = new File(outDir, className + ".java");
         StreamResult result = new StreamResult(outFile);

         // Perform the transformation
         xformer.transform(getSource(), result);

      // Transformer configuration error
      } catch (TransformerConfigurationException cause) {
         throw ExceptionUtils.newIOException("Unable to perform XSLT transformation due to configuration problem.", cause);

      // Transformer error
      } catch (TransformerException cause) {
         throw ExceptionUtils.newIOException("Failed to perform XSLT transformation.", cause);
      }
   }
   
   private void transformForLocale(File baseDir, String locale)
   throws IOException {

      try {

         // Create an XSLT Transforer
         String                   xsltPath = "xslt/translation-bundle_to_java.xslt";
         InputStream            xsltStream = Library.getMetaResourceAsStream(xsltPath);
         StreamSource     xsltStreamSource = new StreamSource(xsltStream);
         TransformerFactory xformerFactory = TransformerFactory.newInstance();
         xformerFactory.setURIResolver(_resolver);
         Transformer               xformer = xformerFactory.newTransformer(xsltStreamSource);

         // Set the parameters for the template
         xformer.setParameter("locale",       locale);
         xformer.setParameter("package_name", _domainName);
         xformer.setParameter("accesslevel",  _public ? "public" : "protected");
         xformer.setParameter("log_file",     "log.xml");

         // Make sure the output directory exists
         String domainPath = _domainName.replace(".", "/");
         File       outDir = new File(baseDir, domainPath);
         if (! outDir.exists()) {
            boolean outDirCreated = outDir.mkdirs();
            if (! outDirCreated) {
               throw new IOException("Failed to create output directory \"" + outDir.getPath() + "\".");
            }
         } else if (! outDir.isDirectory()) {
            throw new IOException("Path \"" + outDir.getPath() + "\" exists, but it is not a directory.");
         }

         // Declare where the XSLT output should go
         String    className = "TranslationBundle_" + locale;
         File        outFile = new File(outDir, className + ".java");
         StreamResult result = new StreamResult(outFile);

         // Perform the transformation
         log(LogLevel.INFO, "About to perform XSLT transformation. xsltPath=\"" + xsltPath + "\"; domainName=\"" + _domainName + "\"; domainPath=\"" + domainPath + "\".");
         xformer.transform(getTranslationBundleSource(locale), result);

         log(LogLevel.INFO, "Generated file \"" + outFile.getPath() + "\".");

      // Transformer configuration error
      } catch (TransformerConfigurationException cause) {
         throw ExceptionUtils.newIOException("Unable to perform XSLT transformation due to configuration problem.", cause);

      // Transformer error
      } catch (TransformerException cause) {
         throw ExceptionUtils.newIOException("Failed to perform XSLT transformation.", cause);
      }
   }
}