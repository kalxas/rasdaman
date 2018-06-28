package petascope.wcps.exception.processing;

import petascope.exceptions.ExceptionCode;
import petascope.exceptions.WCPSException;
import petascope.util.CrsUtil;
import petascope.wcps.subset_axis.model.WcpsSubsetDimension;

public class InvalidExpressionSubsetException extends WCPSException {
    
    public InvalidExpressionSubsetException(WcpsSubsetDimension subset) {
        super(ExceptionCode.WcpsError, EXCEPTION_TEXT.replace("$subset", subset.toString())
                .replace("$hint", computeHint(subset)));
    }

    /**
     * Shows an example of the subset being applied at grid level, by changing its crs to CRS:1.
     * @param subset: the user input subset.
     * @return: the same subset with adjusted CRS.
     */
    private static String computeHint(WcpsSubsetDimension subset){
        subset.setCrs(CrsUtil.GRID_CRS);
        return subset.toString();
    }

    private static final String EXCEPTION_TEXT = "Invalid subset expression: $subset. Expressions inside subsets are only allowed on grid axes." +
            "\nHINT: Try a grid subset instead. E.g. $hint subsets directly on the grid.";
}

