/*
 * This file is part of rasdaman community.
 *
 * Rasdaman community is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Rasdaman community is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU  General Public License for more details.
 *
 * You should have received a copy of the GNU  General Public License
 * along with rasdaman community.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright 2003 - 2014 Peter Baumann / rasdaman GmbH.
 *
 * For more information please see <http://www.rasdaman.org>
 * or contact Peter Baumann via <baumann@rasdaman.com>.
 */
package petascope.exceptions;

import org.rasdaman.config.ConfigManager;

/**
 * This exception can return an error report, that can be marshalled into a
 * standard compliant XML structure describing the error that has happened.
 *
 * @author Dimitar Misev
 */
public class SecoreException extends Exception {

    private ExceptionCode exceptionCode;
    private String exceptionText;

    private SecoreException() {
    }

    /**
     * Construct a new SECORE exception
     *
     * @param exceptionCode exception code, if it's null then
     * {@link ExceptionCode#UnknownError} is used.
     */
    public SecoreException(ExceptionCode exceptionCode) {
        this(exceptionCode, null, null);
    }

    /**
     * Construct a new SECORE exception
     *
     * @param exceptionCode exception code, if it's null then
     * {@link ExceptionCode#UnknownError} is used.
     * @param exceptionText exception message
     */
    public SecoreException(ExceptionCode exceptionCode, String exceptionText) {
        this(exceptionCode, exceptionText, null);
    }

    /**
     * Construct a new SECORE exception
     *
     * @param exceptionCode exception code, if it's null then
     * {@link ExceptionCode#UnknownError} is used.
     * @param causeEx original exception
     */
    public SecoreException(ExceptionCode exceptionCode, Exception causeEx) {
        this(exceptionCode, causeEx.getLocalizedMessage(), causeEx);
    }

    /**
     * Construct a new SECORE exception
     *
     * @param exceptionCode exception code (must not be null)
     * @param exceptionText exception message
     * @param causeEx original exception
     */
    public SecoreException(ExceptionCode exceptionCode, String exceptionText, Exception causeEx) {
        this(exceptionCode, exceptionText, causeEx, ConfigManager.RASDAMAN_VERSION, ConfigManager.LANGUAGE);
    }

    /**
     * Construct a new SECORE exception
     *
     * @param exceptionCode exception code, if it's null then
     * {@link ExceptionCode#UnknownError} is used.
     * @param exceptionText exception message
     * @param ex
     * @param version
     * @param language
     */
    protected SecoreException(ExceptionCode exceptionCode, String exceptionText, Exception ex, String version, String language) {
        super(exceptionText, ex);
        if (exceptionCode == null) {
            this.exceptionCode = ExceptionCode.UnknownError;
        } else {
            this.exceptionCode = exceptionCode;
        }

        if (exceptionText != null) {
            this.exceptionText = exceptionText;
        } else {
            this.exceptionText = exceptionCode.getDescription();
        }
    }

    public ExceptionCode getExceptionCode() {
        return exceptionCode;
    }

    public String getExceptionText() {
        return exceptionText;
    }
}
