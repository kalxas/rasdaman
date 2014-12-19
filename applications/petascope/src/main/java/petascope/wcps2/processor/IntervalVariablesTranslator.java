package petascope.wcps2.processor;

import petascope.wcps.metadata.DomainElement;
import petascope.wcps2.metadata.Coverage;
import petascope.wcps2.metadata.CoverageRegistry;
import petascope.wcps2.metadata.Interval;
import petascope.wcps2.translator.*;

import java.util.ArrayList;
import java.util.List;

/**
 * This processor translates a variable appearing in an interval into rasql.
 * <p/>
 * e.g. over x in i(0:100) using x => x in [0:100] using x[0]
 *
 * @author <a href="mailto:alex@flanche.net">Alex Dumitru</a>
 * @author <a href="mailto:vlad@flanche.net">Vlad Merticariu</a>
 */
public class IntervalVariablesTranslator implements IProcessor {

    @Override
    public void process(IParseTreeNode translationTree, IParseTreeNode currentNode, CoverageRegistry coverageRegistry) {
        if (currentNode instanceof CoverageConstructor) {
            CoverageConstructor coverageConstructor = (CoverageConstructor) currentNode;
            processVariables(coverageConstructor.getAxisIteratorVariableNames(), coverageConstructor.getValues());
        } else if (currentNode instanceof GeneralCondenser) {
            GeneralCondenser generalCondenser = (GeneralCondenser) currentNode;
            processVariables(generalCondenser.getAxisIteratorVariableNames(), generalCondenser.getValues());
        }
    }

    @Override
    public boolean canProcess(IParseTreeNode currentNode) {
        if (currentNode instanceof GeneralCondenser
                || currentNode instanceof CoverageConstructor) {
            return true;
        }
        return false;
    }

    /**
     * Adds suffix [$index] to variable names addressed as iterators, and replaces all of them with the name of the
     * first variable.
     * @param variableNames
     * @param values
     */
    private void processVariables(ArrayList<String> variableNames, IParseTreeNode values) {
        String firstVariableName = variableNames.get(0);
        //iterate through the children of values
        for (IParseTreeNode node : values.getSubTree()) {
            if (node instanceof CoverageExpressionVariableName) {
                //check if the name appears in the list of axis iterators
                String variableName = ((CoverageExpressionVariableName) node).getCoverageVariableName();
                if (variableNames.contains(variableName)) {
                    Integer index = variableNames.indexOf(variableName);
                    String replacedVariableName = firstVariableName + VARIABLE_SUFFIX.replace("$index", index.toString());
                    ((CoverageExpressionVariableName) node).setCoverageVariableName(replacedVariableName);
                }
            }
        }
    }

    private final static String VARIABLE_SUFFIX = "[$index]";
}
