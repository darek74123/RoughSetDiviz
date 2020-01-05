package pl.poznan.put.roughset.xmcda;

import pl.poznan.put.roughset.RuleInducer;
import org.xmcda.ProgramExecutionResult;
import org.xmcda.XMCDA;
import org.xmcda.parsers.xml.xmcda_v3.XMCDAParser;

import java.io.File;
import java.util.Map;

public class RoughSet_Irsa_Classification_RulesCLI_XMCDAv3 {
    public static void main(String[] args) throws Utils.InvalidCommandLineException {

        final Utils.Arguments params = Utils.parseCmdLineArguments(args);
        final String indir = params.inputDirectory;
        final String outdir = params.outputDirectory;
        final File prgExecResultsFile = new File(outdir, "messages.xml");
        final ProgramExecutionResult executionResult = new ProgramExecutionResult();

        // The idea of the following code wrt. errors is to collect as many errors as possible before the
        // computation takes place, to the user's benefit since he/she then gets all of them after a single call.

        // this object is where the XMCDA objects will be put into.
        final XMCDA xmcda = new XMCDA();

        Utils.loadXMCDAv3(xmcda, new File(indir, "alternatives.xml"), true,
                executionResult, "alternatives");
        Utils.loadXMCDAv3(xmcda, new File(indir, "categories.xml"), true,
                executionResult, "categories");
        Utils.loadXMCDAv3(xmcda, new File(indir, "assignments.xml"), true,
                executionResult, "alternativesAssignments");
        Utils.loadXMCDAv3(xmcda, new File(indir, "criteria.xml"), true,
                executionResult, "criteria");
        Utils.loadXMCDAv3(xmcda, new File(indir, "criteria_scales.xml"), true,
                executionResult, "criteriaScales");
        Utils.loadXMCDAv3(xmcda, new File(indir, "performance_table.xml"), true,
                executionResult, "performanceTable");
        Utils.loadXMCDAv3(xmcda, new File(indir, "approximations.xml"), true,
                executionResult, "approximations");
        Utils.loadXMCDAv3(xmcda, new File(indir, "parameters.xml"), false,
                executionResult, "programParameters");

        // We have problems with the inputs, its time to stop
        if (!(executionResult.isOk() || executionResult.isWarning())) {
            Utils.writeProgramExecutionResultsAndExit(prgExecResultsFile, executionResult, Utils.XMCDA_VERSION.v3);
            // previous statement terminates the execution
        }

        // Let's check the inputs and convert them into our own structures
        final InputsHandler.Inputs inputs = InputsHandler.checkAndExtractInputs(xmcda, executionResult);

        if (!(executionResult.isOk() || executionResult.isWarning()) || inputs == null) {
            Utils.writeProgramExecutionResultsAndExit(prgExecResultsFile, executionResult, Utils.XMCDA_VERSION.v3);
            // previous statement terminates the execution
        }


        // Here we know that everything was loaded as expected
        RuleInducer ruleInducer = new RuleInducer(inputs);
        try {
            ruleInducer.induceRules();
        } catch (Throwable t) {
            executionResult.addError(Utils.getMessage("The calculation could not be performed, reason: ", t));
            Utils.writeProgramExecutionResultsAndExit(prgExecResultsFile, executionResult, Utils.XMCDA_VERSION.v3);
            // previous statement terminates the execution
            return; // just to make the compiler happy about results being final and potentially not initialized below
        }

        // Fine, now let's put the rules into XMCDA structures
        Map<String, XMCDA> x_results = OutputsHandler.convert(ruleInducer.getRulesInXmcdaFormat(), executionResult);

        // and last, write them onto the disk
        final XMCDAParser parser = new XMCDAParser();

        for (String key : x_results.keySet()) {
            File outputFile = new File(outdir, String.format("%s.xml", key));
            try {
                parser.writeXMCDA(x_results.get(key), outputFile, OutputsHandler.xmcdaV3Tag(key));
            } catch (Throwable throwable) {
                final String err = String.format("Error while writing %s.xml, reason: ", key);
                executionResult.addError(Utils.getMessage(err, throwable));
                // Whatever the error is, clean up the file: we do not want to leave an empty or partially-written file
                outputFile.delete();
            }
        }

        // Let's write the file 'messages.xml' as well
        Utils.writeProgramExecutionResultsAndExit(prgExecResultsFile, executionResult, Utils.XMCDA_VERSION.v3);
        // previous statement terminates the execution
    }
}
