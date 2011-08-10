// See the COPYRIGHT file for copyright and license information
package org.znerd.logdoc;

/**
 * Exception thrown if a specified locale is not supported by at least one <em>logdoc</em> <code>Log</code> class.
 */
public final class UnsupportedLocaleException extends RuntimeException {

    private static final long serialVersionUID = -991987123777189023L;

    /**
     * Constructs a new <code>UnsupportedLocaleException</code>.
     * 
     * @param locale the locale, cannot be <code>null</code>.
     * @throws IllegalArgumentException if <code>locale == null</code>.
     */
    public UnsupportedLocaleException(String locale) throws IllegalArgumentException {

        // Call superconstructor first
        super("Locale \"" + locale + "\" is not supported.");

        // Check preconditions
        if (locale == null) {
            throw new IllegalArgumentException("locale == null");
        }

        // Store locale?
        _locale = locale;
    }

    private final String _locale;

    /**
     * Retrieves the unsupported locale.
     * 
     * @return the unsupported locale, never <code>null</code>.
     */
    public String getLocale() {
        return _locale;
    }
}
