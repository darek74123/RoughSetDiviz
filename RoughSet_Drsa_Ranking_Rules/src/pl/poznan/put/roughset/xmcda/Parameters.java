package pl.poznan.put.roughset.xmcda;

import pl.poznan.put.roughset.AllowedObjectsType;
import pl.poznan.put.roughset.ConsistencyCalculator;
import pl.poznan.put.roughset.Rule;
import org.xmcda.ProgramExecutionResult;

import java.util.ArrayList;
import java.util.Arrays;

public class Parameters {
    public static final ArrayList<String> parametersNames = new ArrayList<>(Arrays.asList("consistencyMeasure", "consistencyThreshold", "allowedObjectsType", "typeOfInducedRules"));
    public ConsistencyCalculator.Measure consistencyMeasure;
    public Double consistencyThreshold;
    public AllowedObjectsType allowedObjectsType;
    public Rule.RuleType typeOfInducedRules;

    void parseConsistencyMeasure(Object parameterValue, ProgramExecutionResult errors) {
        if (parameterValue.getClass().equals(String.class)) {
            try {
                consistencyMeasure = ConsistencyCalculator.Measure.valueOf(((String) parameterValue).toUpperCase());
            } catch (IllegalArgumentException e) {
                errors.addError("Invalid consistencyMeasure, expected one of: epsilon, epsilon_prime, rough_membership");
            }
        } else {
            errors.addError("consistencyMeasure value type must be label");
        }
    }

    void parseConsistencyThreshold(Object parameterValue, ProgramExecutionResult errors) {
        if (parameterValue.getClass().equals(Double.class)) {
            Double consistencyThreshold = (Double) parameterValue;
            if (consistencyThreshold >= 0.0 && consistencyThreshold <= 1.0) {
                this.consistencyThreshold = consistencyThreshold;
            } else {
                errors.addError("consistencyThreshold value must be between 0.0 and 1.0");
            }
        } else {
            errors.addError("consistencyThreshold value type must be real");
        }
    }

    void parseAllowedObjectsType(Object parameterValue, ProgramExecutionResult errors) {
        if (parameterValue.getClass().equals(String.class)) {
            try {
                allowedObjectsType = AllowedObjectsType.valueOf(((String) parameterValue).toUpperCase());
            } catch (IllegalArgumentException e) {
                errors.addError("Invalid allowedObjectsType, expected one of: positive_region, positive_and_boundary_regions, whole_dataset");
            }
        } else {
            errors.addError("allowedObjectsType value type must be label");
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
