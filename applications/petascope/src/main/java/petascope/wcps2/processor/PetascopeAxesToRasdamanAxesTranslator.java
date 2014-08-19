package petascope.wcps2.processor;

import petascope.wcps.metadata.DomainElement;
import petascope.wcps2.metadata.Coverage;
import petascope.wcps2.metadata.CoverageRegistry;
import petascope.wcps2.metadata.Interval;
import petascope.wcps2.translator.DimensionIntervalList;
import petascope.wcps2.translator.IParseTreeNode;
import petascope.wcps2.translator.TrimDimensionInterval;
import petascope.wcps2.translator.TrimExpression;

import java.util.List;

/**
 * This processor translates any petascope axes in a subset to a rasdaman interval
 *
 * @author <a href="mailto:alex@flanche.net">Alex Dumitru</a>
 * @author <a href="mailto:vlad@flanche.net">Vlad Merticariu</a>
 */
public class PetascopeAxesToRasdamanAxesTranslator implements IProcessor {

    @Override
    public void process(IParseTreeNode translationTree, IParseTreeNode currentNode, CoverageRegistry coverageRegistry) {
        TrimExpression trim = (TrimExpression) currentNode;
        Coverage coverage = trim.getCoverageExpression().getCoverage();
        DimensionIntervalList dimensionIntervals = trim.getDimensionIntervalList();
        List<DomainElement> domains = coverage.getMetadata().getDomains();
        for (DomainElement domain : domains) {
            boolean found = false;
            System.out.println(domain.toString());
            for (TrimDimensionInterval trimInterval : dimensionIntervals.getIntervals()) {
                if (trimInterval.getAxisName().equalsIgnoreCase(domain.getLabel())) {
                    found = true;
                    trimInterval.setAxisPosition(domain.getOrder());
                }
            }
            if (!found) {
                TrimDimensionInterval fullInterval = new TrimDimensionInterval(domain.getLabel(), "", "*", "*");
                fullInterval.setTrimInterval(new Interval<Long>(Long.MIN_VALUE, Long.MIN_VALUE));
                fullInterval.setAxisPosition(domain.getOrder());
                dimensionIntervals.getIntervals().add(fullInterval);
            }
        }
    }

    @Override
    public boolean canProcess(IParseTreeNode currentNode) {
        if (currentNode instanceof TrimExpression) {
            return true;
        }
        return false;
    }
}
