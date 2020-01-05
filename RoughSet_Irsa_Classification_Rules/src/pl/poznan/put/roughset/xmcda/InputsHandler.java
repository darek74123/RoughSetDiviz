package pl.poznan.put.roughset.xmcda;

import org.xmcda.*;
import org.xmcda.utils.ValueConverters;

import java.util.*;

public class InputsHandler {
    public static class Inputs {
        public List<String> alternativesIds = new ArrayList<>();
        public List<String> criteriaIds = new ArrayList<>();
        public Map<String, List<String>> criteriaDomains = new HashMap<>(); // Map<CriterionId, listOfAllowedValues>
        public List<String> categoriesIds = new ArrayList<>();
        public Map<String, String> assignments = new HashMap<>(); // Map<alternativeId, categoryId>
        public Map<String, Map<String, String>> performanceTable = new LinkedHashMap<>(); // Map<alternativeId, Map<criterionId, value>
        public Map<String, List<String>> approximations = new HashMap<>(); // Map<categoryId,  listOfAlternativesIds>
        public Parameters parameters = new Parameters();
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
        checkCategories(xmcda, errors);
        checkAssignments(xmcda, errors);
        checkParameters(xmcda, errors);
        checkApproximations(xmcda, errors);

        return inputs;
    }

    private static void checkPerformanceTable(XMCDA xmcda, ProgramExecutionResult errors) {
        if (xmcda.performanceTablesList.size() == 0) {
            errors.addError("No performance table has been supplied");
        } else if (xmcda.performanceTablesList.size() > 1) {
            errors.addError("More than one performance table has been supplied");
        } else {
            @SuppressWarnings("rawtypes")
            PerformanceTable p = xmcda.performanceTablesList.get(0);
            if (p.hasMissingValues()) {
                errors.addError("The performance table has missing values");
            }
            try {
                @SuppressWarnings("unchecked")
                PerformanceTable<String> perfTable = p.convertTo(String.class);
                xmcda.performanceTablesList.set(0, perfTable);
            } catch (ValueConverters.ConversionException e) {
                //Performance table should contain only <label> type values
                final String msg = "Error when converting the performance table's value to String, reason:";
                errors.addError(Utils.getMessage(msg, e));
            }
        }
    }

    private static void checkAlternatives(XMCDA xmcda, ProgramExecutionResult errors) {
        if (xmcda.alternatives.isEmpty()) {
            errors.addError("No alternatives list has been supplied.");
        }
    }

    private static void checkCriteria(XMCDA xmcda, ProgramExecutionResult errors) {
        if (xmcda.criteria.isEmpty()) {
            errors.addError("No criteria list has been supplied.");
        } else if (xmcda.criteria.getActiveCriteria().isEmpty()) {
            errors.addError("All criteria are inactive.");
        }
    }

    private static void checkCategories(XMCDA xmcda, ProgramExecutionResult errors) {
        if (xmcda.categories.isEmpty()) {
            errors.addError("No categories have been supplied.");
        } else if (xmcda.categories.getActiveCategories().isEmpty()) {
            errors.addError("All categories are inactive.");
        }
    }

    private static void checkAssignments(XMCDA xmcda, ProgramExecutionResult errors) {
        if (xmcda.alternativesAssignmentsList.size() == 0) {
            errors.addError("No assignments list has been supplied.");
        }
    }

    private static void checkParameters(XMCDA xmcda, ProgramExecutionResult errors) {
        if (xmcda.programParametersList.isEmpty()) {
            errors.addError("No parameters list has been supplied.");
        } else if (xmcda.programParametersList.size() > 1) {
            errors.addError("More than one parameters list has been supplied.");
        }
    }

    private static void checkApproximations(XMCDA xmcda, ProgramExecutionResult errors) {
        if (xmcda.approximations.size() == 0) {
            errors.addError("No approximations list has been supplied.");
        }
    }


    private static Inputs extractInputs(Inputs inputs, XMCDA xmcda, ProgramExecutionResult xmcda_execution_result) {
        extractParameters(inputs, xmcda, xmcda_execution_result);
        if (xmcda_execution_result.isError())
            return null; // cannot perform calculation if required parameters wasn't extracted properly
        extractAlternatives(inputs, xmcda);
        extractCategories(inputs, xmcda);
        extractAssignments(inputs, xmcda, xmcda_execution_result);
        extractCriteria(inputs, xmcda);
        extractCriteriaDomains(inputs, xmcda, xmcda_execution_result);
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

    //extracts alternative-->category assignments
    private static void extractAssignments(Inputs inputs, XMCDA xmcda, ProgramExecutionResult errors) {
        for (AlternativeAssignment assignment : xmcda.alternativesAssignmentsList.get(0)) {
            String alternativeId = assignment.getAlternative().id();
            String categoryId = assignment.getCategory().id();
            if (!inputs.alternativesIds.contains(alternativeId)) {
                continue; //skip assignment for alternatives not declared in alternatives file
            } else {
                if (!inputs.categoriesIds.contains(categoryId)) {
                    errors.addError("Category: " + categoryId + " was not declared in criteria file");
                    return;
                } else if (inputs.assignments.containsKey(alternativeId)) {
                    errors.addError("Only one assignment for each alternative allowed");
                    return;
                } else {
                    inputs.assignments.put(alternativeId, categoryId);
                }
            }
        }
        // check if all alternatives are assigned to category
        for (String alternativeId : inputs.alternativesIds) {
            if (!inputs.assignments.containsKey(alternativeId)) {
                errors.addError("Missing assignment for alternative: " + alternativeId);
            }
        }
    }

    //extracts active criteria Ids declared in criteria file
    private static void extractCriteria(Inputs inputs, XMCDA xmcda) {
        for (Criterion criterion : xmcda.criteria.getActiveCriteria()) {
            inputs.criteriaIds.add(criterion.id());
        }
    }

    //checks if all criteria are nominal, extracts criteria domains
    private static void extractCriteriaDomains(Inputs inputs, XMCDA xmcda, ProgramExecutionResult errors) {
        CriteriaScales criteriaScales = xmcda.criteriaScalesList.get(0);
        for (Criterion criterion : xmcda.criteria.getActiveCriteria()) {
            CriterionScales criterionScales = criteriaScales.get(criterion);
            Scale scale = criterionScales.get(0);
            if (scale.getClass() == NominalScale.class) {
                NominalScale nominalScale = (NominalScale) scale;
                inputs.criteriaDomains.putIfAbsent(criterion.id(), new ArrayList<>());
                for (String nominalValue : nominalScale) {
                    inputs.criteriaDomains.get(criterion.id()).add(nominalValue);
                }
            } else {
                errors.addError("All criteria scales must be nominal");
                return;
            }
        }
    }

    private static void extractPerformanceTable(Inputs inputs, XMCDA xmcda, ProgramExecutionResult errors) {
        // already converted to String in checkInputs()
        @SuppressWarnings("unchecked")
        PerformanceTable<String> xmcda_perf_table = (PerformanceTable<String>) xmcda.performanceTablesList.get(0);

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
                String value = xmcda_perf_table.getValue(alternative, criterion);
                //check if value belongs to domain, declared in criteria file
                if (!inputs.criteriaDomains.get(criterion.id()).contains(value)) {
                    errors.addError(value + " was not declared in criteria file");
                    return;
                }
                inputs.performanceTable.putIfAbsent(alternative.id(), new HashMap<>());
                inputs.performanceTable.get(alternative.id()).put(criterion.id(), value);
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
                    case "bestConditionChoiceMode":
                        inputs.parameters.parseBestConditionChoiceMode(parameterValue, errors);
                        parametersToExtract.remove("bestConditionChoiceMode");
                        break;
                    case "typeOfInducedRules":
                        inputs.parameters.parseTypeOfInducedRules(parameterValue, errors);
                        parametersToExtract.remove("typeOfInducedRules");
                        break;
                }
            }
        }
        if (parametersToExtract.contains("typeOfInducedRules")) {
            errors.addError("Required parameter 'typeOfInducedRules' is missing");
        }
    }


    private static void extractApproximations(Inputs inputs, XMCDA xmcda, ProgramExecutionResult errors) {
        for (Approximation approximation : xmcda.approximations) {
            List<String> alternativesIds = new ArrayList<>();
            @SuppressWarnings("unchecked")
            Set<Alternative> alternativeSet = approximation.getAlternativesSet().getElements();
            for (Alternative alternative : alternativeSet) {
                alternativesIds.add(alternative.id());
            }
            String categoryId = approximation.getDecisionsClasses().get(0).getCategoryID();
            String approximationType = approximation.mcdaConcept();
            if (approximationType.equals("lower") || approximationType.equals("upper")) {
                if (inputs.parameters.typeOfInducedRules.equals(pl.poznan.put.roughset.Rule.RuleType.CERTAIN) && approximationType.equals("lower")) {
                    inputs.approximations.put(categoryId, alternativesIds);
                } else if (inputs.parameters.typeOfInducedRules.equals(pl.poznan.put.roughset.Rule.RuleType.POSSIBLE) && approximationType.equals("upper")) {
                    inputs.approximations.put(categoryId, alternativesIds);
                } else {
                    errors.addInfo("Skipping '" + approximationType + "' approximation, because typeOfInducedRules=" + inputs.parameters.typeOfInducedRules.toString().toLowerCase());
                }
            } else {
                errors.addError("Approximation mcdaConcept value should be either 'lower' or 'upper'");
                return;
            }
        }
    }
}
