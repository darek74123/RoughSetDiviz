package pl.poznan.put.roughset;

import pl.poznan.put.roughset.xmcda.InputsHandler;
import pl.poznan.put.roughset.xmcda.Pair;
import org.xmcda.*;

import java.util.*;

public class RuleInducer {
    //decision rules will be induced from lower and upper approximations of relations S and Sc
    private final List<Union> unionsApproximations = new ArrayList<>();

    //used for preparing PCT
    private final Map<String, Alternative> allAlternativesMap = new HashMap<>();

    //Pairwise Comparison Table
    private final Map<String, List<Alternative>> classesWithAlternativesComparisons = new HashMap<>();
    private final List<Alternative> allAlternativesComparisons = new ArrayList<>();
    private final Map<Pair<String, String>, Alternative> allAlternativesComparisonsMap = new HashMap<>();

    private List<Rule> rules;

    private final InputsHandler.Inputs inputs;

    public RuleInducer(InputsHandler.Inputs inputs) {
        this.inputs = inputs;
        buildObjectsFromInputData();
        preparePCT();
    }

    public void induceRules() {
        buildApproximations();
        rules = new VC_DomLEM(allAlternativesComparisons, unionsApproximations, inputs.parameters.allowedObjectsType, inputs.parameters.consistencyMeasure, inputs.parameters.consistencyThreshold, inputs.parameters.typeOfInducedRules).runAlgorithm();
    }

    public List<org.xmcda.Rule> getRulesInXmcdaFormat() {
        List<org.xmcda.Rule> x_rules = new ArrayList<>();
        for (Rule rule : rules) {
            org.xmcda.Rule x_rule = new org.xmcda.Rule();
            Conditions x_conditions = new Conditions();
            for (Condition condition : rule.conditions) {
                if (condition.criterion.getClass().equals(CardinalCriterion.class)) {
                    org.xmcda.Condition x_condition = new org.xmcda.Condition();
                    x_condition.setCriterionID(condition.criterion.name);
                    x_condition.setOperator(condition.getCardinalOperator());
                    x_condition.setMcdaConcept("difference");
                    CardinalCriterion conditionValue = (CardinalCriterion) condition.criterion;
                    x_condition.setValue(new QualifiedValue<>(conditionValue.getValue()));
                    x_conditions.getConditions().add(x_condition);
                } else {
                    ConditionPair x_conditionPair = new ConditionPair();
                    FirstElement firstElement = new FirstElement();
                    org.xmcda.Condition firstCondition = new org.xmcda.Condition();
                    SecondElement secondElement = new SecondElement();
                    org.xmcda.Condition secondCondition = new org.xmcda.Condition();
                    OrdinalCriterionPair conditionValues = (OrdinalCriterionPair) condition.criterion;
                    firstCondition.setCriterionID(conditionValues.firstAlternativeValue.name);
                    firstCondition.setMcdaConcept("value");
                    firstCondition.setOperator(condition.getFirstOperator());
                    firstCondition.setValue(new QualifiedValue<>(conditionValues.firstAlternativeValue.getValue()));
                    firstElement.setCondition(firstCondition);

                    secondCondition.setCriterionID(conditionValues.secondAlternativeValue.name);
                    secondCondition.setMcdaConcept("value");
                    secondCondition.setOperator(condition.getSecondOperator());
                    secondCondition.setValue(new QualifiedValue<>(conditionValues.secondAlternativeValue.getValue()));
                    secondElement.setCondition(secondCondition);

                    x_conditionPair.setFirstElement(firstElement);
                    x_conditionPair.setSecondElement(secondElement);
                    x_conditions.getConditionsPairs().add(x_conditionPair);
                }
            }
            x_rule.setConditions(x_conditions);

            Decisions decisions = new Decisions();
            Decision decision = new Decision();
            decision.setCategoryID(rule.getConcept());
            decisions.add(decision);
            x_rule.setDecisions(decisions);
            x_rule.setMcdaConcept(rule.getRuleType());

            x_rules.add(x_rule);
        }
        return x_rules;
    }

    private Criterion prepareCriterion(InputsHandler.Inputs inputs, String criterionId, QualifiedValue<?> qualifiedValue) {
        Criterion criterion;
        PreferenceDirection preferenceDirection = PreferenceDirection.valueOfLabel(inputs.criteriaPreferenceDirections.get(criterionId).name());

        if (inputs.criteriaScales.get(criterionId).equals("quantitative")) {
            Double value = (Double) qualifiedValue.getValue();
            criterion = new CardinalCriterion(criterionId, preferenceDirection, value);
        } else {
            String value = (String) qualifiedValue.getValue();
            criterion = new OrdinalCriterion(criterionId, preferenceDirection, value, inputs.criteriaMaps.get(criterionId));
        }
        return criterion;
    }

    private void buildObjectsFromInputData() {
        for (String alternativeId : inputs.alternativesIds) {
            Map<String, Criterion> criteria = new LinkedHashMap<>();
            Map<String, QualifiedValue<?>> objectCriteriaMap = inputs.performanceTable.get(alternativeId);
            objectCriteriaMap.forEach((criterionId, qualifiedValue) -> criteria.put(criterionId, prepareCriterion(inputs, criterionId, qualifiedValue)));
            Alternative object = new Alternative(criteria);
            allAlternativesMap.put(alternativeId, object);
        }
    }

    private void preparePCT() {
        //initialize classesWithObjectsComparisons map
        for (String decisionClass : inputs.categoriesIds) {
            classesWithAlternativesComparisons.put(decisionClass, new ArrayList<>());
        }

        for (Pair<String, String> pair : inputs.preferences_S) {
            Alternative firstObject = allAlternativesMap.get(pair.left);
            Alternative secondObject = allAlternativesMap.get(pair.right);
            Alternative objectComparison = new Alternative(calculateCriteriaComparisons(firstObject, secondObject));
            allAlternativesComparisons.add(objectComparison);
            allAlternativesComparisonsMap.put(pair, objectComparison);
            classesWithAlternativesComparisons.get("S").add(objectComparison);
        }
        for (Pair<String, String> pair : inputs.preferences_Sc) {
            Alternative firstObject = allAlternativesMap.get(pair.left);
            Alternative secondObject = allAlternativesMap.get(pair.right);
            Alternative objectComparison = new Alternative(calculateCriteriaComparisons(firstObject, secondObject));
            allAlternativesComparisons.add(objectComparison);
            allAlternativesComparisonsMap.put(pair, objectComparison);
            classesWithAlternativesComparisons.get("Sc").add(objectComparison);
        }
    }

    private Map<String, Criterion> calculateCriteriaComparisons(Alternative firstAlternative, Alternative secondAlternative) {
        Map<String, Criterion> criteriaComparisons = new LinkedHashMap<>(); //criteria differences or ordinal values pairs
        for (String criterionId : firstAlternative.criteria.keySet()) {
            Criterion firstAlternativeValue = firstAlternative.criteria.get(criterionId);
            Criterion secondAlternativeValue = secondAlternative.criteria.get(criterionId);
            Criterion difference = firstAlternativeValue.calculateDifference(secondAlternativeValue);
            criteriaComparisons.put(criterionId, difference);
        }
        return criteriaComparisons;
    }

    private void buildApproximations() {
        inputs.approximations.forEach((union) -> {
            List<Alternative> approximation = new ArrayList<>();
            union.approximation.forEach(pair -> approximation.add(allAlternativesComparisonsMap.get(pair)));
            unionsApproximations.add(new Union(approximation, classesWithAlternativesComparisons.get(union.categoryID), UnionType.valueOf(union.type.toUpperCase()), union.categoryID));
        });
    }
}
