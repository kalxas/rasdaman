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
 * Copyright 2003 - 2017 Peter Baumann / rasdaman GmbH.
 *
 * For more information please see <http://www.rasdaman.org>
 * or contact Peter Baumann via <baumann@rasdaman.com>.
 */
package petascope.wcps.handler;

import java.util.Arrays;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;
import petascope.exceptions.ExceptionCode;
import petascope.exceptions.PetascopeException;
import petascope.exceptions.WCPSException;
import petascope.wcps.result.WcpsResult;
import petascope.util.ras.CastDataTypeConverter;

/**
 * Translation node from wcps to rasql for the case expression. Example:  <code>
 * (char) $c1
 * </code> translates to  <code>
 * (char) c1
 * </code>
 *
 * @author <a href="mailto:alex@flanche.net">Alex Dumitru</a>
 * @author <a href="mailto:vlad@flanche.net">Vlad Merticariu</a>
 */
@Service
@Scope(value = "prototype", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class CastExpressionHandler extends Handler {
    
    public CastExpressionHandler() {
        
    }
    
    public CastExpressionHandler create(StringScalarHandler rangeTypeHandler, Handler coverageExpressionHandler) {
        CastExpressionHandler result = new CastExpressionHandler();
        result.setChildren(Arrays.asList(rangeTypeHandler, coverageExpressionHandler));
        
        return result;
    }
    
    @Override
    public WcpsResult handle() throws PetascopeException {
        String rangeType = ((WcpsResult)this.getFirstChild().handle()).getRasql();
        WcpsResult coverageExpression = (WcpsResult) this.getSecondChild().handle();
        
        return this.handle(rangeType, coverageExpression);
    }

    private WcpsResult handle(String rangeType, WcpsResult coverageExpression) throws WCPSException {
        String rasdamanType = null;
        try {
            rasdamanType = CastDataTypeConverter.convert(rangeType);
        } catch (PetascopeException ex) {
            throw new WCPSException(ExceptionCode.NoApplicableCode, 
                                    "Cannot convert WCPS base type to rasdaman base type. Reason: " + ex.getExceptionText(), ex);
        }
        String template = TEMPLATE.replace("$rangeType", rasdamanType)
                                  .replace("$coverageExp", coverageExpression.getRasql());
        return new WcpsResult(coverageExpression.getMetadata(), template);
    }

    private final String TEMPLATE = "($rangeType) $coverageExp"; 
}
