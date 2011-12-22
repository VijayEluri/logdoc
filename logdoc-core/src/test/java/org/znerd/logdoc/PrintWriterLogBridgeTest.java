// See the COPYRIGHT file for copyright and license information
package org.znerd.logdoc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.junit.Test;
import org.znerd.util.log.LogLevel;

public class PrintWriterLogBridgeTest extends AbstractLogBridgeTest {

    private StringWriter stringWriter;

    @Override
    protected LogBridge provideLogBridge() {
        final boolean writeImmediately = true;
        stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter, writeImmediately);
        return new PrintWriterLogBridge(printWriter);
    }

    private PrintWriterLogBridge getPrintWriterLogBridge() {
        return (PrintWriterLogBridge) super.getLogBridge();
    }

    @Test
    public void testDefaultLevelIsDebug() {
        assertEquals(LogLevel.DEBUG, getPrintWriterLogBridge().getLevel());
    }

    @Test
    public void testLogOneMessageGoesToPrintWriter() {
        String fqcn = getClass().getName();
        String domain = "org.znerd";
        String groupId = "sample";
        String entryId = "9876";
        LogLevel level = LogLevel.FATAL;
        String message = "Bla";
        Throwable exception = new Error();
        getLogBridge().logOneMessage(fqcn, domain, groupId, entryId, level, message, exception);

        String outputString = stringWriter.toString();
        assertNotNull(outputString);
        assertTrue(outputString.length() > 0);
    }

    @Test
    public void testLogOneMessageComposedMessage() {
        String fqcn = getClass().getName();
        String domain = "org.znerd";
        String groupId = "sample";
        String entryId = "9876";
        LogLevel level = LogLevel.FATAL;
        String message = "Bla";
        Throwable exception = null;
        getLogBridge().logOneMessage(fqcn, domain, groupId, entryId, level, message, exception);

        String outputString = stringWriter.toString();
        String expectedComposedMessage = level.name() + " [] " + domain + "." + groupId + '.' + entryId + ' ' + message + System.getProperty("line.separator");
        assertEquals(expectedComposedMessage, outputString);
    }

    @Test
    public void testLogOneMessageComposedMessageWithContextId() {
        String fqcn = getClass().getName();
        String domain = "org.znerd";
        String groupId = "sample";
        String entryId = "9876";
        LogLevel level = LogLevel.FATAL;
        String message = "Bla";
        String contextId = "TEST-CONTEXT-ID-123";
        Throwable exception = null;
        LogBridge logBridge = getLogBridge();
        logBridge.putContextId(contextId);
        try {
            logBridge.logOneMessage(fqcn, domain, groupId, entryId, level, message, exception);

            String outputString = stringWriter.toString();
            String expectedComposedMessage = level.name() + " [" + contextId + "] " + domain + "." + groupId + '.' + entryId + ' ' + message + System.getProperty("line.separator");
            assertEquals(expectedComposedMessage, outputString);
        } finally {
            logBridge.unputContextId();
        }
    }

    @Test
    public void testLogOneMessageWithException() {
        String fqcn = getClass().getName();
        String domain = "org.znerd";
        String groupId = "sample";
        String entryId = "9876";
        LogLevel level = LogLevel.FATAL;
        String message = "Blablabla2 �";
        String contextId = "TEST-CONTEXT-ID-543";
        Throwable causeException = new Error("some m�ss�ge");
        Throwable exception = new RuntimeException("5ome �th�r messa9e", causeException);
        LogBridge logBridge = getLogBridge();
        logBridge.putContextId(contextId);

        String stackTrace = stackTraceToString(exception);
        assertNotNull(stackTrace);
        assertTrue(stackTrace.length() > 0);

        String expectedComposedMessage = level.name() + " [" + contextId + "] " + domain + "." + groupId + '.' + entryId + ' ' + message + System.getProperty("line.separator") + stackTrace;
        try {
            logBridge.logOneMessage(fqcn, domain, groupId, entryId, level, message, exception);
            String outputString = stringWriter.toString();
            assertEquals("Actual message is: " + outputString, expectedComposedMessage, outputString);
        } finally {
            logBridge.unputContextId();
        }
    }

    private String stackTraceToString(Throwable exception) {
        final StringWriter stringWriter = new StringWriter();
        final PrintWriter printWriter = new PrintWriter(stringWriter);
        exception.printStackTrace(printWriter);
        printWriter.flush();
        return stringWriter.toString();
    }
}
