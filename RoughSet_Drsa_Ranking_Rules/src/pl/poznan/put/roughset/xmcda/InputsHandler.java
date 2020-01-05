package pl.poznan.put.roughset.xmcda;

import org.xmcda.*;
import org.xmcda.utils.Coord;
import org.xmcda.value.ValuedLabel;

import java.util.*;

public class InputsHandler {
    public static class Inputs {
        public List<String> alternativesIds = new ArrayList<>();
        public List<String> criteriaIds = new ArrayList<>();
        public Map<String, String> criteriaScales = new HashMap<>(); // Map<CriterionId, scale>
        public Map<String, Scale.PreferenceDirection> criteriaPreferenceDirections = new HashMap<>(); // Map<CriterionId, preferenceDirection>
        public Map<String, Map<String, Integer>> criteriaMaps = new HashMap<>(); // Map<CriterionId, Map<label, rank>>
        public List<String> categoriesIds = new ArrayList<>();
        public Map<String, Map<String, QualifiedValue<?>>> performanceTable = new LinkedHashMap<>(); // Map<alternativeId, Map<criterionId, value>
        public List<Union> approximations = new ArrayList<>();
        public Parameters parameters = new Parameters();
        public List<Pair<String, String>> preferences_S = new ArrayList<>();
        public List<Pair<String, String>> preferences_Sc = new ArrayList<>();
    }


    public static Inputs checkAndExtractInputs(XMCDA xmcda, ProgramExecutionResult xmcda_exec_result) {
        Inputs inputsDict = checkInputs(xmcda, xmcda_exec_result);

        if (xmcda_exec_result.isError()) {
            return null;
        }

        return extractInputs(inputsDict, xmcda, xmcda_exec_result);
    }

    private static Inputs checkInputs(XMCDA xmcda, ProgramExecutionResult errors) {
        Inputs inputs = new Inputs();

        checkPerformanceTable(xmcda, errors);
        checkAlternatives(xmcda, errors);
        checkCriteria(xmcda, errors);
        checkCriteriaScales(xmcda, errors);
        checkCategories(xmcda, errors);
        checkPreferences(xmcda, errors);
        checkApproximations(xmcda, errors);
        checkParameters(xmcda, errors);

        return inputs;
    }

    private static void checkPerformanceTable(XMCDA xmcda, ProgramExecutionResult errors) {
        if (xmcda.performanceTablesList.size() == 0) {
            errors.addError("No performance table has been supplied");
        } else if (xmcda.performanceTablesList.size() > 1) {
            errors.addError("More than one performance table has been supplied");
        } else {
            PerformanceTable p = xmcda.performanceTablesList.get(0);
            if (p.hasMissingValues()) {
                errors.addError("The performance table has missing values");
            }
        }
    }

    private static void checkAlternatives(XMCDA xmcda, ProgramExecutionResult errors) {
        if (xmcda.alternatives.isEmpty()) {
            errors.addError("No alternatives list has been supplied.");
        }
    }

    private static void checkCategories(XMCDA xmcda, ProgramExecutionResult errors) {
        if (xmcda.categories.isEmpty()) {
            errors.addError("No categories has been supplied.");
        } else if (xmcda.categories.getActiveCategories().isEmpty()) {
            errors.addError("All categories are inactive.");
        }
    }

    private static void checkCriteria(XMCDA xmcda, ProgramExecutionResult errors) {
        if (xmcda.criteria.isEmpty()) {
            errors.addError("No criteria list has been supplied.");
        } else if (xmcda.criteria.getActiveCriteria().isEmpty()) {
            errors.addError("All criteria are inactive.");
        }
    }

    private static void checkCriteriaScales(XMCDA xmcda, ProgramExecutionResult errors) {
        if (xmcda.criteriaScalesList.isEmpty()) {
            errors.addError("No criteria list has been supplied.");
        } else if (xmcda.criteriaScalesList.size() > 1) {
            errors.addError("More than one criteriaScales list has been supplied.");
        }
    }

    private static void checkApproximations(XMCDA xmcda, ProgramExecutionResult errors) {
        if (xmcda.approximations.size() == 0) {
            errors.addError("No approximations list has been supplied.");
        }
    }

    private static void checkParameters(XMCDA xmcda, ProgramExecutionResult errors) {
        if (xmcda.programParametersList.isEmpty()) {
            errors.addError("No parameters list has been supplied.");
        } else if (xmcda.programParametersList.size() > 1) {
            errors.addError("More than one parameters list has been supplied.");
        } else if (xmcda.programParametersList.get(0).size() != 4)
            errors.addError("Parameters list must contain four elements: " + Parameters.parametersNames);
    }

    private static void checkPreferences(XMCDA xmcda, ProgramExecutionResult errors) {
        if (xmcda.alternativesMatricesList.size() != 2) {
            errors.addError("Two preferences lists required");
        }
    }


    private static Inputs extractInputs(Inputs inputs, XMCDA xmcda, ProgramExecutionResult xmcda_execution_result) {
        extractParameters(inputs, xmcda, xmcda_execution_result);
        if (xmcda_execution_result.isError())
            return null; // cannot perform calculation if required parameters wasn't extracted properly
        extractAlternatives(inputs, xmcda);
        extractCategories(inputs, xmcda);
        extractPreferences(inputs, xmcda);
        extractCriteria(inputs, xmcda);
        extractCriteriaScalesAndDomains(inputs, xmcda, xmcda_execution_result);
        extractPerformanceTable(inputs, xmcda, xmcda_execution_result);
        extractApproximations(inputs, xmcda, xmcda_execution_result);

        return inputs;
    }

    //extracts active alternatives Ids declared in alternatives file
    private static void extractAlternatives(Inputs inputs, XMCDA xmcda) {
        for (Alternative alternative : xmcda.alternatives.getActiveAlternatives()) {
            inputs.alternativesIds.add(alternative.id());
        }
    }

    //extracts active categories Ids declared in categories file
    private static void extractCategories(Inputs inputs, XMCDA xmcda) {
        for (Category category : xmcda.categories.getActiveCategories()) {
            inputs.categoriesIds.add(category.id());
        }
    }

    //extracts active criteria Ids declared in criteria file
    private static void extractCriteria(Inputs inputs, XMCDA xmcda) {
        for (Criterion criterion : xmcda.criteria.getActiveCriteria()) {
            inputs.criteriaIds.add(criterion.id());
        }
    }

    //checks if all criteria are quantitative or qualitative, extracts criteria scales, preferenceDirections and domains for qualitative criteria
    private static void extractCriteriaScalesAndDomains(Inputs inputs, XMCDA xmcda, ProgramExecutionResult errors) {
        CriteriaScales criteriaScales = xmcda.criteriaScalesList.get(0);
        for (Criterion criterion : xmcda.criteria.getActiveCriteria()) {
            CriterionScales criterionScales = criteriaScales.get(criterion);
            Scale scale = criterionScales.get(0);
            if (scale.getClass() == QuantitativeScale.class) {
                QuantitativeScale quantitativeScale = (QuantitativeScale) scale;
                inputs.criteriaScales.put(criterion.id(), "quantitative");
                inputs.criteriaPreferenceDirections.put(criterion.id(), quantitativeScale.getPreferenceDirection());
            } else if (scale.getClass() == QualitativeScale.class) {
                QualitativeScale qualitativeScale = (QualitativeScale) scale;
                inputs.criteriaScales.put(criterion.id(), "qualitative");
                inputs.criteriaPreferenceDirections.put(criterion.id(), qualitativeScale.getPreferenceDirection());
                inputs.criteriaMaps.putIfAbsent(criterion.id(), new LinkedHashMap<>());
                for (Object o : qualitativeScale) {
                    ValuedLabel valuedLabel = (ValuedLabel) o;
                    String label = valuedLabel.getLabel();
                    Integer rank = (Integer) valuedLabel.getValue().getValue();
                    if (inputs.criteriaMaps.get(criterion.id()).containsValue(rank)) {
                        errors.addError("labels on qualitative scale must have unique ranks");
                        return;
                    }
                    inputs.criteriaMaps.get(criterion.id()).putIfAbsent(label, rank);
                }
            } else {
                errors.addError("Only Quantitative and Qualitative scales are supported");
                return;
            }
        }
    }

    private static void extractPerformanceTable(Inputs inputs, XMCDA xmcda, ProgramExecutionResult errors) {
        PerformanceTable<?> xmcda_perf_table = xmcda.performanceTablesList.get(0);

        //build performance table
        //skip any alternatives or criteria not declared as active in alternatives and criteria files
        for (Alternative alternative : xmcda_perf_table.getAlternatives()) {
            if (!inputs.alternativesIds.contains(alternative.id())) {
                continue;
            }
            for (Criterion criterion : xmcda_perf_table.getCriteria()) {
                if (!inputs.criteriaIds.contains(criterion.id())) {
                    continue;
                }
                QualifiedValue<?> qualifiedValue = xmcda_perf_table.getQValue(alternative, criterion);
                //check if value belongs to domain, declared in criteria file
                if (inputs.criteriaScales.get(criterion.id()).equals("qualitative")) {
                    String label = (String) qualifiedValue.getValue();
                    if (!inputs.criteriaMaps.get(criterion.id()).containsKey(label)) {
                        errors.addError(label + " was not declared in criteria file");
                        return;
                    }
                } else if (!qualifiedValue.isNumeric()) {
                    errors.addError(qualifiedValue.getValue() + " is not numeric (criterion " + criterion.id() + " was declared on quantitative scale)");
                    return;
                }
                inputs.performanceTable.putIfAbsent(alternative.id(), new HashMap<>());
                inputs.performanceTable.get(alternative.id()).put(criterion.id(), qualifiedValue);
            }
        }
    }

    private static void extractApproximations(Inputs inputs, XMCDA xmcda, ProgramExecutionResult errors) {
        for (Approximation approximation : xmcda.approximations) {
            List<Pair<String, String>> alternativesPairs = new ArrayList<>();
            for (Coord<Alternative, Alternative> coord : ((AlternativesMatrix<?>) approximation.getAlternativesMatrix()).keySet()) {
                alternativesPairs.add(new Pair<>(coord.x.id(), coord.y.id()));
            }

            String categoryId = approximation.getDecisionsClasses().get(0).getCategoryID();
            String unionType;
            if (categoryId.equals("S")) {
                unionType = "at_least";
            } else if (categoryId.equals("Sc")) {
                unionType = "at_most";
            } else {
                errors.addError("Wrong categoryId in approximation decision, only 'S' or 'Sc' categories allowed");
                return;
            }
            String approximationType = approximation.mcdaConcept();
            Union union = new Union(categoryId, unionType, alternativesPairs);
            if (approximationType.equals("lower") || approximationType.equals("upper")) {
                if (inputs.parameters.typeOfInducedRules.equals(pl.poznan.put.roughset.Rule.RuleType.CERTAIN) && approximationType.equals("lower")) {
                    inputs.approximations.add(union);
                } else if (inputs.parameters.typeOfInducedRules.equals(pl.poznan.put.roughset.Rule.RuleType.POSSIBLE) && approximationType.equals("upper")) {
                    inputs.approximations.add(union);
                } else {
                    errors.addInfo("Skipping '" + approximationType + "' approximation, because typeOfInducedRules=" + inputs.parameters.typeOfInducedRules.toString().toLowerCase());
                }
            } else {
                errors.addError("Approximation mcdaConcept value should be either 'lower' or 'upper'");
                return;
            }
        }
    }

    private static void extractParameters(Inputs inputs, XMCDA xmcda, ProgramExecutionResult errors) {
        List<String> parametersToExtract = new ArrayList<>(Parameters.parametersNames);
        for (ProgramParameter parameter : xmcda.programParametersList.get(0)) {
            if (!Parameters.parametersNames.contains(parameter.id())) {
                errors.addError("Unexpected parameter: " + parameter.id());
                //no return statement, in order to find more errors
            } else if (!parametersToExtract.contains(parameter.id())) {
                errors.addError("Parameter: " + parameter.id() + " declared more than once");
                //no return statement, in order to find more errors
            } else if (parameter.getValues().size() != 1) {
                errors.addError("Parameter: " + parameter.id() + " must have exactly one value");
                return;
            } else {
                Object parameterValue = ((QualifiedValue) parameter.getValues().get(0)).getValue();
                switch (parameter.id()) {
                    case "consistencyMeasure":
                        inputs.parameters.parseConsistencyMeasure(parameterValue, errors);
                        parametersToExtract.remove("consistencyMeasure");
                        break;
                    case "consistencyThreshold":
                        inputs.parameters.parseConsistencyThreshold(parameterValue, errors);
                        parametersToExtract.remove("consistencyThreshold");
                        break;
                    case "allowedObjectsType":
                        inputs.parameters.parseAllowedObjectsType(parameterValue, errors);
                        parametersToExtract.remove("allowedObjectsType");
                        break;
                    case "typeOfInducedRules":
                        inputs.parameters.parseTypeOfInducedRules(parameterValue, errors);
                        parametersToExtract.remove("typeOfInducedRules");
                        break;
                }
            }
        }

        if (!parametersToExtract.isEmpty())
            errors.addError("Missing parameters: " + parametersToExtract);
    }

    private static void extractPreferences(Inputs inputs, XMCDA xmcda) {
        //extract preferences_S
        for (Coord<Alternative, Alternative> coord : xmcda.alternativesMatricesList.get(0).keySet()) {
            inputs.preferences_S.add(new Pair<>(coord.x.id(), coord.y.id()));
        }
        //extract preferences_Sc
        for (Coord<Alternative, Alternative> coord : xmcda.alternativesMatricesList.get(1).keySet()) {
            inputs.preferences_Sc.add(new Pair<>(coord.x.id(), coord.y.id()));
        }
    }
}
