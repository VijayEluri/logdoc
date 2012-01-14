<?xml version="1.0" encoding="UTF-8" ?>
<!-- See the COPYRIGHT file for redistribution and use restrictions. -->

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

  <xsl:param name="package_name" />
  <xsl:param name="accesslevel"  />

  <xsl:include href="shared.xslt" />

  <xsl:output method="text" />
  
  <xsl:variable name="domain" select="/log/@domain" />

  <xsl:template match="log">
    <xsl:text>// This file is generated by Logdoc. Do not edit.
package </xsl:text>
    <xsl:value-of select="$package_name" />
    <xsl:text><![CDATA[;

/**
 * Logger for the <em>]]></xsl:text>
    <xsl:value-of select="$domain" />
    <xsl:text><![CDATA[</em> domain.]]></xsl:text>
      <xsl:if test="string-length(@since) &gt; 0">
         <xsl:text>
 *
 * @since </xsl:text>
         <xsl:value-of select="@since" />
      </xsl:if>
      <xsl:text>
 */
</xsl:text>
    <xsl:if test="$accesslevel = 'public'">public </xsl:if>
    <xsl:text>final class Log {

    private static final String FQCN = "</xsl:text>
    <xsl:value-of select="$package_name" />
    <xsl:text><![CDATA[.Log";
    private static final java.util.HashMap<String,TranslationBundle> TRANSLATION_BUNDLES_BY_NAME = createTranslationBundlesMap();
    private static TranslationBundle CURRENT_TRANSLATION_BUNDLE = TranslationBundle_]]></xsl:text>
    <xsl:value-of select="translation-bundle[position() = 1]/@locale" />
    <xsl:text><![CDATA[.SINGLETON;
    @SuppressWarnings("unused") private static final Controller CONTROLLER = new Controller();
   
    private Log() {
    }
    
    private static java.util.HashMap<String,TranslationBundle> createTranslationBundlesMap() {
        java.util.HashMap<String,TranslationBundle> map = new java.util.HashMap<String,TranslationBundle>();]]></xsl:text>
      <xsl:for-each select="translation-bundle">
        <xsl:text>
          map.put("</xsl:text>
        <xsl:value-of select="@locale" />
        <xsl:text>", TranslationBundle_</xsl:text>
        <xsl:value-of select="@locale" />
        <xsl:text>.SINGLETON);</xsl:text>
      </xsl:for-each>
      <xsl:text><![CDATA[
        return map;
    }


    /**
     * Sets the diagnostic context identifier for this thread.
     *
     * @param newContextId the new diagnostic context identifier for this thread, cannot be <code>null</code>.
     */
    public static void putContextId(String newContextId) {
        org.znerd.logdoc.LogFacade.putContextId(newContextId);
    }

    /**
     * Unsets the diagnostic context identifier for this thread.
     */
    public static void unputContextId() {
        org.znerd.logdoc.LogFacade.unputContextId();
    }

    /**
     * Retrieves the current diagnostic context identifier for this thread.
     *
     * @return the context ID for this thread, or <code>null</code> if none.
     */
    public static String getContextId() {
        return org.znerd.logdoc.LogFacade.getContextId();
    }

    /**
     * Retrieves the active translation bundle.
     *
     * @return the translation bundle that is currently in use, never <code>null</code>.
     */
    public static TranslationBundle getTranslationBundle() {
        return CURRENT_TRANSLATION_BUNDLE;
    }]]></xsl:text>

    <xsl:apply-templates select="group/entry" />

    <xsl:text><![CDATA[

   /**
    * Controller for this <code>Log</code> class.
    */
   private static final class Controller extends org.znerd.logdoc.internal.LogController {

      /**
       * Constructs a new <code>Controller</code> for this log.
       *
       * @throws org.znerd.logdoc.UnsupportedLocaleException if the current locale is unsupported.
       */
      public Controller() throws org.znerd.logdoc.UnsupportedLocaleException {
         super();
      }

      @Override
      public String toString() {
         return getClass().getName();
      }

      @Override
      public boolean isLocaleSupported(String locale) {
         return TRANSLATION_BUNDLES_BY_NAME.containsKey(locale);
      }

      @Override
      public void setLocale(String newLocale) {
         CURRENT_TRANSLATION_BUNDLE = TRANSLATION_BUNDLES_BY_NAME.get(newLocale);
      }
   }
}
]]></xsl:text>
  </xsl:template>

  <xsl:template match="group/entry">
    <xsl:variable name="category" select="concat($package_name, '.', ../@id, '.', @id)" />
    <xsl:variable name="exception" select="@exception = 'true'" />
    <xsl:variable name="exceptionClass">
      <xsl:choose>
        <xsl:when test="string-length(@exceptionClass) &gt; 0">
          <xsl:value-of select="@exceptionClass" />
        </xsl:when>
        <xsl:otherwise>
          <xsl:text>java.lang.Throwable</xsl:text>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>

    <xsl:text>

   /**
    * Logs message </xsl:text>
    <xsl:value-of select="@id" />
    <xsl:text><![CDATA[, in the log entry group <em>]]></xsl:text>
    <xsl:value-of select="../@name" />
    <xsl:text><![CDATA[</em>. The description for this log entry is:
    * <blockquote><em>]]></xsl:text>
    <xsl:apply-templates select="description" />
    <xsl:text><![CDATA[</em></blockquote>
    */
   public static final void ]]></xsl:text>
    <xsl:choose>
      <xsl:when test="string-length(@methodName) &gt; 0">
        <xsl:value-of select="@methodName" />
      </xsl:when>
      <xsl:otherwise>
        <xsl:text>log_</xsl:text>
        <xsl:value-of select="@id" />
      </xsl:otherwise>
    </xsl:choose>
    <xsl:text>(</xsl:text>
    <xsl:if test="$exception">
      <xsl:value-of select="$exceptionClass" />
      <xsl:text> _exception</xsl:text>
      <xsl:if test="count(param) &gt; 0">
        <xsl:text>, </xsl:text>
      </xsl:if>
    </xsl:if>
    <xsl:apply-templates select="param" mode="methodArgument" />
    <xsl:for-each select="param[@filter='true']">
      <xsl:text>
      </xsl:text>
      <xsl:value-of select="@name" />
      <xsl:text> = org.znerd.logdoc.Library.getLogFilter().filter("</xsl:text>
      <xsl:value-of select="$category" />
      <xsl:text>", "</xsl:text>
      <xsl:value-of select="@name" />
      <xsl:text>", </xsl:text>
      <xsl:value-of select="@name" />
      <xsl:text>);</xsl:text>
    </xsl:for-each>
    <xsl:text>) {
      if (org.znerd.logdoc.LogFacade.shouldLog("</xsl:text>
    <xsl:value-of select="$domain" />
    <xsl:text>", "</xsl:text>
    <xsl:value-of select="../@id" />
    <xsl:text>", "</xsl:text>
    <xsl:value-of select="@id" />
    <xsl:text>", org.znerd.util.log.LogLevel.</xsl:text>
    <xsl:value-of select="@level" />
    <xsl:text>)) {
         String _translation = CURRENT_TRANSLATION_BUNDLE.translation_</xsl:text>
    <xsl:value-of select="@id" />
    <xsl:text>(</xsl:text>
    <xsl:if test="$exception">
      <xsl:text>_exception</xsl:text>
    </xsl:if>
    <xsl:for-each select="param">
      <xsl:if test="$exception or (position() &gt; 1)">
        <xsl:text>, </xsl:text>
      </xsl:if>
      <xsl:value-of select="@name" />
    </xsl:for-each>
    <xsl:text>);
         org.znerd.logdoc.LogFacade.log(</xsl:text>
    <xsl:text>FQCN, "</xsl:text>
    <xsl:value-of select="$domain" />
    <xsl:text>", "</xsl:text>
    <xsl:value-of select="../@id" />
    <xsl:text>", "</xsl:text>
    <xsl:value-of select="@id" />
    <xsl:text>", org.znerd.util.log.LogLevel.</xsl:text>
    <xsl:value-of select="@level" />
    <xsl:text>, _translation</xsl:text>
        <xsl:if test="$exception">
      <xsl:text>, _exception</xsl:text>
    </xsl:if>
    <xsl:text>);
      }
   }</xsl:text>
  </xsl:template>
</xsl:stylesheet>
