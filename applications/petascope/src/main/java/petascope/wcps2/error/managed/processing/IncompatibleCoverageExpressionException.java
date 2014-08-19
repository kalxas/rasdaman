package petascope.wcps2.error.managed.processing;

import petascope.wcps2.metadata.Coverage;

/**
 * Error that is thrown when an operation between two incompatible coverages is performed
 *
 * @author <a href="mailto:alex@flanche.net">Alex Dumitru</a>
 * @author <a href="mailto:vlad@flanche.net">Vlad Merticariu</a>
 */
public class IncompatibleCoverageExpressionException extends WCPSProcessingError {

    /**
     * Constructor for the class
     *
     * @param firstCov  the first incompatible coverage
     * @param secondCov the second incompatible coverage
     */
    public IncompatibleCoverageExpressionException(Coverage firstCov, Coverage secondCov) {
        this.firstCov = firstCov;
        this.secondCov = secondCov;
    }

    /**
     * Returns the first offending coverage
     *
     * @return
     */
    public Coverage getFirstCov() {
        return firstCov;
    }

    /**
     * Returns the second offending coverage
     *
     * @return
     */
    public Coverage getSecondCov() {
        return secondCov;
    }

    private final Coverage firstCov;
    private final Coverage secondCov;
}
