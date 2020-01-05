package pl.poznan.put.roughset.xmcda;

import pl.poznan.put.roughset.Lem2;
import pl.poznan.put.roughset.Rule;
import org.xmcda.ProgramExecutionResult;

import java.util.ArrayList;
import java.util.Arrays;

public class Parameters {
    public static final ArrayList<String> parametersNames = new ArrayList<>(Arrays.asList("bestConditionChoiceMode", "typeOfInducedRules"));
    public Lem2.BestConditionChoiceMode bestConditionChoiceMode;
    public Rule.RuleType typeOfInducedRules;

    void parseBestConditionChoiceMode(Object parameterValue, ProgramExecutionResult errors) {
        if (parameterValue.getClass().equals(String.class)) {
            try {
                bestConditionChoiceMode = Lem2.BestConditionChoiceMode.valueOf(((String) parameterValue).toUpperCase());
            } catch (IllegalArgumentException e) {
                errors.addError("Invalid bestConditionChoiceMode, expected 'original' or 'modified'");
            }
        } else {
            errors.addError("consistencyMeasure value type must be label");
        }
    }

    void parseTypeOfInducedRules(Object parameterValue, ProgramExecutionResult errors) {
        if (parameterValue.getClass().equals(String.class)) {
            try {
                typeOfInducedRules = Rule.RuleType.valueOf(((String) parameterValue).toUpperCase());
            } catch (IllegalArgumentException e) {
                errors.addError("Invalid typeOfInducedRules, expected 'certain' or 'possible'");
            }
        } else {
            errors.addError("typeOfInducedRules value type must be label");
        }
    }
}
