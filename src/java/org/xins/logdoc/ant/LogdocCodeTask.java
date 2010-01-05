// See the COPYRIGHT file for copyright and license information
package org.xins.logdoc.ant;

import org.xins.logdoc.LogDef;

/**
 * An Apache Ant task for generating source files from Logdoc definitions.
 *
 * @author <a href="mailto:ernst@ernstdehaan.com">Ernst de Haan</a>
 */
public final class LogdocCodeTask extends AbstractLogdocTask {

   //-------------------------------------------------------------------------
   // Constructors
   //-------------------------------------------------------------------------

   /**
    * Constructs a new <code>LogdocJavaTask</code> object.
    */
   public LogdocCodeTask() {
      // empty
   }


   //-------------------------------------------------------------------------
   // Methods
   //-------------------------------------------------------------------------

   @Override
   protected void executeImpl(LogDef def) throws Exception {
      def.generateCode(_destDir);
   }
}
