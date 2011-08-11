// See the COPYRIGHT file for copyright and license information
package org.znerd.logdoc.gen;

import java.io.File;
import java.io.IOException;

import org.xml.sax.SAXException;
import org.znerd.logdoc.LogDef;
import org.znerd.logdoc.LogLevel;
import org.znerd.logdoc.internal.InternalLogging;
import org.znerd.logdoc.internal.CheckDirUtils;

/**
 * Abstract base class for generators.
 */
public abstract class Generator {

    protected Generator(File sourceDir, File destDir) throws IllegalArgumentException {
        if (sourceDir == null) {
            throw new IllegalArgumentException("sourceDir == null");
        }
        _sourceDir = sourceDir;
        _destDir = destDir;
    }

    private final File _sourceDir, _destDir;

    public void generate() throws IOException {
        File actualDestDir = determineDestDir(_sourceDir, _destDir);
        checkDirs(_sourceDir, actualDestDir);
        processFiles(_sourceDir, actualDestDir);
    }

    private File determineDestDir(File sourceDir, File specifiedDestDir) {
        File actualDestDir = (specifiedDestDir != null) ? specifiedDestDir : sourceDir;
        return actualDestDir;
    }

    private void checkDirs(File sourceDir, File destDir) throws IOException {
        CheckDirUtils.checkDir("Source directory", sourceDir, true, false, false);
        CheckDirUtils.checkDir("Destination directory", destDir, false, true, true);
    }

    private void processFiles(File sourceDir, File destDir) throws IOException {
        long start = System.currentTimeMillis();
        logProcessingStart(sourceDir, destDir);
        LogDef logDef = loadAndValidateDefinitions(sourceDir);
        generateImpl(logDef, destDir);
        logProcessingFinish(start);
    }

    private void logProcessingStart(File sourceDir, File destDir) {
        InternalLogging.log(LogLevel.INFO, "Processing from " + sourceDir.getPath() + " to " + destDir.getPath() + '.');
    }

    private void logProcessingFinish(long start) {
        long duration = System.currentTimeMillis() - start;
        InternalLogging.log(LogLevel.NOTICE, "Processed definitions in " + duration + " ms.");
    }

    private LogDef loadAndValidateDefinitions(File sourceDir) throws IOException {
        try {
            return LogDef.loadFromDirectory(sourceDir);
        } catch (IOException cause) {
            throw new IOException("Failed to load log definitions due to an I/O error.", cause);
        } catch (SAXException cause) {
            throw new IOException("Failed to load log definitions due to an XML parsing error.", cause);
        }
    }

    protected abstract void generateImpl(LogDef logDef, File destDir) throws IOException;
}
