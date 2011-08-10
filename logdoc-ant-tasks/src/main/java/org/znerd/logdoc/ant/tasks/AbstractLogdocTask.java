// See the COPYRIGHT file for copyright and license information
package org.znerd.logdoc.ant.tasks;

import java.io.File;
import java.io.IOException;

import org.apache.tools.ant.BuildException;
import static org.apache.tools.ant.Project.MSG_VERBOSE;
import org.apache.tools.ant.taskdefs.MatchingTask;

import org.znerd.logdoc.gen.Generator;
import org.znerd.logdoc.internal.InternalLogging;
import static org.znerd.logdoc.internal.TextUtils.quote;

import org.znerd.logdoc.ant.tasks.internal.AntInternalLogging;

/**
 * Abstract base class for the Logdoc Ant task implementations.
 * <p>
 * The most notable parameters supported by task implementations derived from this class are:
 * <dl>
 * <dt>in
 * <dd>The input directory, to read the input files (the Logdoc definitions) from. Optional, defaults to project base directory.
 * <dt>out
 * <dd>The output directory, to write the output files to. Optional, defaults to source directory.
 * <dt>overwrite
 * <dd>Flag that indicates if each existing file should always be overwritten, even if it is newer than the source file. Default is <code>false</code>.
 * </dl>
 * <p>
 * This task supports more parameters and contained elements, inherited from {@link MatchingTask}, see <a href="http://ant.apache.org/manual/dirtasks.html">the Ant site</a>.
 */
public abstract class AbstractLogdocTask extends MatchingTask {

    protected AbstractLogdocTask() {
    }

    public void setIn(File dir) {
        log("Setting \"in\" to: " + quote(dir) + '.', MSG_VERBOSE);
        _sourceDir = dir;
    }

    protected File _sourceDir;

    public void setOut(File dir) {
        log("Setting \"out\" to: " + quote(dir) + '.', MSG_VERBOSE);
        _destDir = dir;
    }

    protected File _destDir;

    public void setOverwrite(boolean flag) {
        log("Setting \"overwrite\" to: \"" + flag + "\".", MSG_VERBOSE);
        _overwrite = flag;
    }

    protected boolean _overwrite;

    @Override
    public final void execute() throws BuildException {
        sendInternalLoggingThroughAnt();
        File actualSourceDir = determineSourceDir(_sourceDir);
        generate(actualSourceDir);
    }

    private void sendInternalLoggingThroughAnt() {
        InternalLogging.setLogger(new AntInternalLogging(this));
    }

    private File determineSourceDir(File specifiedSourceDir) {
        return (specifiedSourceDir != null) ? specifiedSourceDir : getDefaultSourceDir();
    }

    private File getDefaultSourceDir() {
        return getProject().getBaseDir();
    }

    private void generate(File actualSourceDir) {
        Generator generator = createGenerator(actualSourceDir, _destDir, _overwrite);
        try {
            generator.generate();
        } catch (IOException cause) {
            throw new BuildException("Failed to perform transformation.", cause);
        }
    }

    protected abstract Generator createGenerator(File sourceDir, File destDir, boolean overwrite);
}
