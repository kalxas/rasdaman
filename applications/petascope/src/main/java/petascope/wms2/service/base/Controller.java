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

package petascope.wms2.service.base;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import petascope.wms2.service.exception.error.WMSException;
import petascope.wms2.service.exception.error.WMSInternalException;
import petascope.wms2.service.exception.response.ExceptionResponseFactory;
import petascope.wms2.servlet.WMSGetRequest;

import java.util.List;

/**
 * The controller class manages the workflow from receiving a request to returning a response. Each raw request (a request
 * coming from the front-end, be it a servlet or a command line app) is parsed, validated and handled to finally be transformed
 * into a response that can be passed back to the front-end.
 * <p/>
 * To define a new request type for this service you will need to extend all the classes needed by a controller, and then
 * extend this class implementing the respective getters
 *
 * @author <a href="mailto:dumitru@rasdaman.com">Alex Dumitru</a>
 */
public abstract class Controller<RST extends Request, PT extends Parser<RST>, HT extends Handler<RST, RPT>, RPT extends Response> {

    /**
     * Constructor for the class
     *
     * @param parser     the parser for this controller
     * @param validators the validators for this controller
     * @param handler    the handler for this controller
     */
    public Controller(@NotNull PT parser, @NotNull List<Validator> validators, @NotNull HT handler) {
        this.parser = parser;
        this.validators = validators;
        this.handler = handler;
    }

    /**
     * Returns true if this controller is fit to handle this raw request or false otherwise
     *
     * @param rawRequest the raw request coming from a front-end
     * @return true if the controller supports the request, false otherwise
     */
    public boolean supports(@NotNull WMSGetRequest rawRequest) {
        PT parser = getParser();
        return parser.canParse(rawRequest);
    }

    /**
     * Returns the response to a request
     *
     * @param rawRequest the raw request to be handled
     * @return the response to the given request
     */
    @NotNull
    @SuppressWarnings("unchecked")
    public Response getResponse(@NotNull WMSGetRequest rawRequest) {
        try {
            RST request = getParser().parse(rawRequest);
            for (Validator validator : getValidators()) {
                validator.validate(request);
            }
            return getHandler().handle(request);
        } catch (WMSException exception) {
            return ExceptionResponseFactory.getExceptionResponse(exception, getExceptionFormat(rawRequest));
        } catch (Exception e) {
            return ExceptionResponseFactory.getExceptionResponse(new WMSInternalException(e), getExceptionFormat(rawRequest));
        }
    }

    /**
     * Returns the parser for this controller
     *
     * @return the parser
     */
    @NotNull
    private PT getParser() {
        return parser;
    }

    /**
     * Returns the validator for this request
     *
     * @return the validator
     */
    @NotNull
    private List<Validator> getValidators() {
        return validators;
    }

    /**
     * Returns the handler for this request
     *
     * @return the handler
     */
    @NotNull
    private HT getHandler() {
        return handler;
    }

    /**
     * Returns the exception format from the raw request
     *
     * @param rawRequest the raw request
     * @return the exception format
     */
    @Nullable
    private String getExceptionFormat(WMSGetRequest rawRequest) {
        if (rawRequest.hasGetValue(Request.getRequestParameterExceptionFormat())) {
            return rawRequest.getGetValueByKey(Request.getRequestParameterExceptionFormat());
        }
        return null;
    }

    @NotNull
    private final PT parser;
    @NotNull
    private final List<Validator> validators;
    @NotNull
    private final HT handler;

}
