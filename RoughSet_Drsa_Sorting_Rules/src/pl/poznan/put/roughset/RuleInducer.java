package pl.poznan.put.roughset;

import pl.poznan.put.roughset.xmcda.InputsHandler;
import org.xmcda.*;

import java.util.*;

public class RuleInducer {
    //decision rules will be induced from lower and upper approximations of unions of decision classes
    private final List<Union> unionsApproximations = new ArrayList<>();

    //inverted alternatives assignments, each decision class has list of objects, that belongs to this class
    private final Map<String, List<Alternative>> classesWithAlternatives = new HashMap<>();
    //all objects (universe U)
    private final List<Alternative> allAlternatives = new ArrayList<>();
    //used for preparing approximations
    private final Map<String, Alternative> allAlternativesMap = new HashMap<>();

    private List<Rule> rules;

    private final InputsHandler.Inputs inputs;

    public RuleInducer(InputsHandler.Inputs inputs) {
        this.inputs = inputs;
        buildObjectsFromInputData();
    }

    public void induceRules() {
        buildApproximations();
        rules = new VC_DomLEM(allAlternatives, unionsApproximations, inputs.parameters.allowedObjectsType, inputs.parameters.consistencyMeasure, inputs.parameters.consistencyThreshold, inputs.parameters.typeOfInducedRules).runAlgorithm();
    }

    public List<org.xmcda.Rule> getRulesInXmcdaFormat() {
        List<org.xmcda.Rule> x_rules = new ArrayList<>();
        for (Rule rule : rules) {
            org.xmcda.Rule x_rule = new org.xmcda.Rule();
            Conditions x_conditions = new Conditions();
            for (Condition condition : rule.conditions) {
                org.xmcda.Condition x_condition = new org.xmcda.Condition();
                x_condition.setCriterionID(condition.criterion.name);
                x_condition.setOperator(condition.getOperator());
                x_condition.setMcdaConcept("value");
                x_conditions.getConditions().add(x_condition);
                if (condition.criterion.getClass().equals(CardinalCriterion.class)) {
                    CardinalCriterion conditionValue = (CardinalCriterion) condition.criterion;
                    x_condition.setValue(new QualifiedValue<>(conditionValue.getValue()));
                } else {
                    OrdinalCriterion conditionValue = (OrdinalCriterion) condition.criterion;
                    x_condition.setValue(new QualifiedValue<>(conditionValue.getValue()));
                }
            }
            x_rule.setConditions(x_conditions);

            Decisions decisions = new Decisions();
            Decision decision = new Decision();
            CategoriesInterval categoriesInterval = new CategoriesInterval();
            if (rule.getUnionType().equals(UnionType.AT_LEAST))
                categoriesInterval.setLowerBound(new Category(rule.getConcept()));
            else
                categoriesInterval.setUpperBound(new Category(rule.getConcept()));
            decision.setCategoriesInterval(categoriesInterval);
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
        //initialize classesWithObjects map
        for (String decisionClass : inputs.categoriesIds) {
            classesWithAlternatives.put(decisionClass, new ArrayList<>());
        }

        for (String alternativeId : inputs.alternativesIds) {
            Map<String, Criterion> criteria = new LinkedHashMap<>();
            Map<String, QualifiedValue<?>> objectCriteriaMap = inputs.performanceTable.get(alternativeId);
            objectCriteriaMap.forEach((criterionId, qualifiedValue) -> criteria.put(criterionId, prepareCriterion(inputs, criterionId, qualifiedValue)));
            Alternative object = new Alternative(criteria);
            String alternativeDecisionClass = inputs.assignments.get(alternativeId);
            allAlternatives.add(object);
            allAlternativesMap.put(alternativeId, object);
            classesWithAlternatives.get(alternativeDecisionClass).add(object);
        }
    }

    private void buildApproximations() {
        inputs.approximations.forEach((union) -> {
            List<Alternative> approximation = new ArrayList<>();
            union.approximation.forEach(alternativeId -> approximation.add(allAlternativesMap.get(alternativeId)));
            unionsApproximations.add(new Union(approximation, classesWithAlternatives.get(union.categoryID), UnionType.valueOf(union.type.toUpperCase()), union.categoryID, inputs.categoriesRanks.get(union.categoryID)));
        });
    }
}
