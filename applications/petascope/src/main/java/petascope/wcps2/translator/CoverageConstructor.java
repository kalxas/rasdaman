package petascope.wcps2.translator;

import org.apache.commons.lang3.StringUtils;
import petascope.core.CoverageMetadata;
import petascope.exceptions.PetascopeException;
import petascope.exceptions.SecoreException;
import petascope.exceptions.WCPSException;
import petascope.util.CrsUtil;
import petascope.util.Pair;
import petascope.util.WcpsConstants;
import petascope.util.XMLSymbols;
import petascope.wcps.metadata.CellDomainElement;
import petascope.wcps.metadata.CoverageInfo;
import petascope.wcps.metadata.DomainElement;
import petascope.wcps.server.core.*;
import petascope.wcps2.error.managed.processing.CoverageMetadataException;
import petascope.wcps2.metadata.Coverage;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;

/**
 * Translation node from wcps coverage list to rasql for the coverage constructor
 * Example:
 * <code>
 * COVERAGE myCoverage
 * OVER x x(0:100)
 * VALUES 200
 * </code>
 * translates to
 * <code>
 * MARRAY x in [0:100]
 * VALUES 200
 * </code>
 *
 * @author <a href="mailto:alex@flanche.net">Alex Dumitru</a>
 * @author <a href="mailto:vlad@flanche.net">Vlad Merticariu</a>
 */
public class CoverageConstructor extends CoverageBuilder {

    /**
     * Constructor for the class
     *
     * @param coverageName  the coverage name
     * @param axisIterators the iterators to be applied to the coverage
     * @param values        the values to build the coverage
     */
    public CoverageConstructor(String coverageName, ArrayList<AxisIterator> axisIterators, IParseTreeNode values) {
        this.coverageName = coverageName;
        this.axisIterators = axisIterators;
        this.values = values;
        addChild(values);
        constructCoverageMetadata();
    }

    @Override
    public String toRasql() {
        List<String> axes = new ArrayList<String>();
        for (IParseTreeNode i : axisIterators) {
            axes.add(i.toRasql());
        }
        String intervals = StringUtils.join(axes, INTERVAL_SEPARATOR);
        String template = TEMPLATE.replace("$intervals", intervals).replace("$values", values.toRasql());
        return template;
    }

    @Override
    protected String nodeInformation() {
        return "(" + coverageName + ")";
    }


    private IParseTreeNode values;
    private final String TEMPLATE = "MARRAY $intervals VALUES $values";
    private final String INTERVAL_SEPARATOR = ",";
}
