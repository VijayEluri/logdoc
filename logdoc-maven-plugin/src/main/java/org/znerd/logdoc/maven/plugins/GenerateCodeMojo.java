// See the COPYRIGHT file for copyright and license information
package org.znerd.logdoc.maven.plugins;

import java.io.File;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.znerd.logdoc.gen.CodeGenerator;

/**
 * A Maven plugin for generating source files and/or documentation from Logdoc definitions.
 * 
 * @goal generate-code
 * @phase generate-sources
 */
public class GenerateCodeMojo extends AbstractMojo {

    /**
     * @parameter default-value="${project}"
     * @readonly
     * @required
     */
    private MavenProject project;

    /**
     * @parameter expression="${basedir}/src/main/logdoc"
     * @required
     */
    private File in;

    /**
     * @parameter expression="${basedir}/target/generated-sources/logdoc"
     * @required
     */
    private File out;
    
    private LogdocMojoSupport support = new LogdocMojoSupport();

    @Override
    public void execute() throws MojoExecutionException {
        support.sendInternalLoggingThroughMaven(getLog());
        support.generate(new CodeGenerator(in, out));
        markGeneratedSourcesForCompilation();
    }

    private void markGeneratedSourcesForCompilation() {
        project.addCompileSourceRoot(out.getAbsolutePath());
    }
}
