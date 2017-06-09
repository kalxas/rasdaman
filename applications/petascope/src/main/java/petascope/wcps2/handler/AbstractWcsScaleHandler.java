/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package petascope.wcps2.handler;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import petascope.wcps2.exception.processing.DuplcateAxisNameInScaleException;
import petascope.wcps2.exception.processing.ScaleValueLessThanZeroException;
import petascope.wcps2.metadata.model.WcpsCoverageMetadata;
import petascope.wcps2.result.WcpsResult;
import petascope.wcps2.subset_axis.model.AbstractWcpsScaleDimension;
import petascope.wcps2.subset_axis.model.WcpsScaleDimensionIntevalList;
import petascope.wcps2.subset_axis.model.WcpsSliceScaleDimension;
import petascope.wcps2.subset_axis.model.WcpsTrimScaleDimension;

/**
 * Abstract class for all WCPS scaling handlers
 @author <a href="mailto:bphamhuu@jacobs-university.net">Bang Pham Huu</a>
 */
public class AbstractWcsScaleHandler {
    /**
     * This scale is made up from WCS request (scaleaxes, scalesize, scaleextent)
     * @param coverageExpression
     * @param wcpsScaleDimensionIntevalList 
     */
    protected void validateScalingDimensionInterval(WcpsResult coverageExpression, WcpsScaleDimensionIntevalList wcpsScaleDimensionIntevalList) {
        // NOTE: no accept duplicate axis (e.g: scalesize=Lat(10)&scalesize=Lat(20)
        List<String> axisNames = new ArrayList<>();
        for (AbstractWcpsScaleDimension abstractWcpsScaleDimension : wcpsScaleDimensionIntevalList.getIntervals()) {
            String axisName = abstractWcpsScaleDimension.getAxisName();
            WcpsCoverageMetadata metadata = coverageExpression.getMetadata();
            // Check axis does exists from the coverage
            metadata.getAxisByName(axisName);        
            // Check if axis does not exist in scaling expression
            if (axisNames.contains(axisName)) {
                throw new DuplcateAxisNameInScaleException(axisName);
            } else {
                axisNames.add(axisName);
            }
            
            BigDecimal lowerBound = null, upperBound = null;
            if (abstractWcpsScaleDimension instanceof WcpsSliceScaleDimension) {
                lowerBound = new BigDecimal(((WcpsSliceScaleDimension)abstractWcpsScaleDimension).getBound());
                upperBound = lowerBound;
            } else if (abstractWcpsScaleDimension instanceof WcpsTrimScaleDimension) {
                lowerBound = new BigDecimal(((WcpsTrimScaleDimension)abstractWcpsScaleDimension).getLowerBound());
                upperBound = new BigDecimal(((WcpsTrimScaleDimension)abstractWcpsScaleDimension).getUpperBound());
            }
            
            // No scale value is less than 0
            if (lowerBound.compareTo(BigDecimal.ZERO) <= 0) {
                throw new ScaleValueLessThanZeroException(axisName, lowerBound.toPlainString());
            } else if (upperBound.compareTo(BigDecimal.ZERO) <= 0) {
                throw new ScaleValueLessThanZeroException(axisName, upperBound.toPlainString());
            }            
        }
    }
}
