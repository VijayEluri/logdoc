// See the COPYRIGHT file for copyright and license information
package org.znerd.logdoc.ant.tasks;

import java.io.File;

import org.znerd.logdoc.gen.DocGenerator;
import org.znerd.logdoc.gen.Generator;

/**
 * An Apache Ant task for generating documentation from Logdoc definitions.
 */
public final class LogdocDocTask extends AbstractLogdocTask {
    @Override
    protected Generator createGenerator(File sourceDir, File destDir, boolean overwrite) {
        return new DocGenerator(sourceDir, destDir, overwrite);
    }
}
