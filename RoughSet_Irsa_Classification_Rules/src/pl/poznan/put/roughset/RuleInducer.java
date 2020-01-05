package pl.poznan.put.roughset;

import pl.poznan.put.roughset.xmcda.InputsHandler;
import org.xmcda.*;

import java.util.*;

public class RuleInducer {
    //inverted alternatives assignments, each decision class has list of alternatives, that belongs to this class
    private final Map<String, List<Alternative>> classesWithAlternatives = new HashMap<>();

    //decision rules will be induced from lower and upper approximations of decision classes
    private final Map<String, List<Alternative>> approximations = new LinkedHashMap<>();

    //all alternatives (universe U)
    private final List<Alternative> allAlternatives = new ArrayList<>();
    //used for preparing approximations
    private final Map<String, Alternative> allAlternativesMap = new HashMap<>();

    private final List<Rule> rules = new ArrayList<>();

    private final InputsHandler.Inputs inputs;


    public RuleInducer(InputsHandler.Inputs inputs) {
        this.inputs = inputs;
        buildObjectsFromInputData(inputs);
    }

    public void induceRules() {
        buildApproximations(inputs);
        approximations.forEach((concept, approximation) -> {
            rules.addAll(new Lem2(allAlternatives, classesWithAlternatives.get(concept), approximation, concept, inputs.parameters.bestConditionChoiceMode, inputs.parameters.typeOfInducedRules).runAlgorithm());
        });
    }

    private QualifiedValues<Integer> getRuleStatisticsXMCDA(Rule rule) {
        QualifiedValues<Integer> ruleStatistics = new QualifiedValues<>();
        QualifiedValue<Integer> specificity = new QualifiedValue<>();
        specificity.setId("specificity");
        specificity.setValue(rule.specificity);

        QualifiedValue<Integer> strength = new QualifiedValue<>();
        strength.setId("strength");
        strength.setValue(rule.strength);

        QualifiedValue<Integer> numberOfCoveredObjects = new QualifiedValue<>();
        numberOfCoveredObjects.setId("coverageCardinality");
        numberOfCoveredObjects.setValue(rule.coverageCardinality);

        ruleStatistics.add(specificity);
        ruleStatistics.add(strength);
        ruleStatistics.add(numberOfCoveredObjects);

        return ruleStatistics;
    }

    public List<org.xmcda.Rule> getRulesInXmcdaFormat() {
        List<org.xmcda.Rule> x_rules = new ArrayList<>();
        for (Rule rule : rules) {
            org.xmcda.Rule x_rule = new org.xmcda.Rule();
            Conditions x_conditions = new Conditions();
            for (Attribute condition : rule.conditions) {
                Condition x_condition = new Condition();
                x_condition.setMcdaConcept("value");
                x_condition.setCriterionID(condition.getName());
                x_condition.setValue(new QualifiedValue<>(condition.getValue()));
                x_condition.setOperator("eq");
                x_conditions.getConditions().add(x_condition);
            }
            x_rule.setConditions(x_conditions);

            Decisions decisions = new Decisions();
            Decision decision = new Decision();
            decision.setCategoryID(rule.getConcept());
            decisions.add(decision);
            x_rule.setDecisions(decisions);
            x_rule.setMcdaConcept(rule.getRuleType());
            x_rule.setValues(getRuleStatisticsXMCDA(rule));

            x_rules.add(x_rule);
        }
        return x_rules;
    }

    private void buildObjectsFromInputData(InputsHandler.Inputs inputs) {
        //initialize classesWithObjects map
        for (String decisionClass : inputs.categoriesIds) {
            classesWithAlternatives.put(decisionClass, new ArrayList<>());
        }

        for (String alternativeId : inputs.alternativesIds) {
            List<Attribute> attributes = new ArrayList<>();
            Map<String, String> objectAttributesMap = inputs.performanceTable.get(alternativeId);
            objectAttributesMap.forEach((k, v) -> attributes.add(new Attribute(k, v)));
            Alternative object = new Alternative(attributes);
            String objectDecisionClass = inputs.assignments.get(alternativeId);
            allAlternatives.add(object);
            allAlternativesMap.put(alternativeId, object);
            classesWithAlternatives.get(objectDecisionClass).add(object);
        }
    }

    private void buildApproximations(InputsHandler.Inputs inputs) {
        inputs.approximations.forEach((categoryId, alternativesIds) -> {
            List<Alternative> objects = new ArrayList<>();
            alternativesIds.forEach(alternativeId -> objects.add(allAlternativesMap.get(alternativeId)));
            approximations.put(categoryId, objects);
        });
    }
}
