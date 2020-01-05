package pl.poznan.put.roughset.xmcda;

import org.xmcda.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OutputsHandler {

    /**
     * Returns the xmcda v3 tag for a given output
     *
     * @param outputName the output's name
     * @return the associated XMCDA v2 tag
     * @throws NullPointerException     if outputName is null
     * @throws IllegalArgumentException if outputName is not known
     */
    public static final String xmcdaV3Tag(String outputName) {
        switch (outputName) {
            case "rules":
                return "rules";
            case "messages":
                return "programExecutionResult";
            default:
                throw new IllegalArgumentException(String.format("Unknown output name '%s'", outputName));
        }
    }

    /**
     * Returns the xmcda v2 tag for a given output
     *
     * @param outputName the output's name
     * @return the associated XMCDA v2 tag
     * @throws NullPointerException     if outputName is null
     * @throws IllegalArgumentException if outputName is not known
     */
    public static final String xmcdaV2Tag(String outputName) {
        switch (outputName) {
            case "rules":
                return "rules";
            case "messages":
                return "methodMessages";
            default:
                throw new IllegalArgumentException(String.format("Unknown output name '%s'", outputName));
        }
    }

    /**
     * Converts the results of the computation step into XMCDA objects.
     *
     * @param rules
     * @param executionResult
     * @return a map with keys being xmcda objects' names and values their corresponding XMCDA object
     */
    public static Map<String, XMCDA> convert(List<Rule> rules, ProgramExecutionResult executionResult) {
        final HashMap<String, XMCDA> x_results = new HashMap<>();

        XMCDA xmcda_rules = new XMCDA();
        xmcda_rules.rules.addAll(rules);
        x_results.put("rules", xmcda_rules);
        return x_results;
    }
}
