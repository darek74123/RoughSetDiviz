package pl.poznan.put.roughset;

import java.util.*;

public class Lem2 {
    private final List<Alternative> allAlternatives;
    private final List<Alternative> thisClassAlternatives;
    private final List<Alternative> approximation;
    private final String concept;
    private final Rule.RuleType ruleType;
    private final BestConditionChoiceMode bestConditionChoiceMode;

    Lem2(List<Alternative> allAlternatives, List<Alternative> thisClassAlternatives, List<Alternative> approximation, String concept, BestConditionChoiceMode bestConditionChoiceMode, Rule.RuleType ruleType) {
        this.allAlternatives = allAlternatives;
        this.thisClassAlternatives = thisClassAlternatives;
        this.approximation = approximation;
        this.concept = concept;
        this.ruleType = ruleType;
        this.bestConditionChoiceMode = bestConditionChoiceMode;
    }

    public enum BestConditionChoiceMode {
        ORIGINAL,
        MODIFIED
    }

    List<Rule> runAlgorithm() {
        List<Alternative> notCoveredYet = new ArrayList<>(approximation);      //G := B
        List<Alternative> alreadyCovered = new ArrayList<>();
        List<Rule> rules = new ArrayList<>();                               //T := emptySet

        while (notCoveredYet.size() > 0) {
            Rule currentRule = new Rule(concept, ruleType);

            while (currentRule.conditions.isEmpty() || !approximation.containsAll(getCoveredAlternatives(allAlternatives, currentRule.conditions))) {
                Attribute newCondition = chooseBestCondition(notCoveredYet, currentRule.conditions);
                currentRule.addCondition(newCondition);

                notCoveredYet.removeIf(obj -> (!obj.matches(Collections.singletonList(newCondition))));
            }

            Iterator<Attribute> i = currentRule.conditions.iterator();
            while (i.hasNext() && currentRule.conditions.size() > 1) //rules with empty condition set are not allowed
            {
                Attribute potentiallyUnnecessaryCondition = i.next();
                List<Attribute> otherConditions = new ArrayList<>(currentRule.conditions);
                otherConditions.remove(potentiallyUnnecessaryCondition);
                if (approximation.containsAll(getCoveredAlternatives(allAlternatives, otherConditions))) {
                    i.remove();
                }
            }

            alreadyCovered.addAll(getCoveredAlternatives(approximation, currentRule.conditions));
            rules.add(currentRule);
            notCoveredYet = new ArrayList<>(approximation);
            notCoveredYet.removeAll(alreadyCovered);
        }

        Iterator<Rule> i = rules.iterator();
        while (i.hasNext()) {
            Rule potentiallyUnnecessaryRule = i.next();
            List<Rule> otherRules = new ArrayList<>(rules);
            otherRules.remove(potentiallyUnnecessaryRule);
            if ((getAllAlternativesCoveredByRules(allAlternatives, otherRules).containsAll(approximation))) {
                i.remove();
            }
        }

        //calculate rule statistics
        for (Rule rule : rules) {
            rule.specificity = rule.conditions.size();
            rule.strength = numberOfCovered(thisClassAlternatives, rule.conditions);
            rule.coverageCardinality = numberOfCovered(allAlternatives, rule.conditions);
        }

        return rules;
    }

    private List<Alternative> getCoveredAlternatives(List<Alternative> alternatives, List<Attribute> conditions) {
        List<Alternative> coveredAlternatives = new ArrayList<>();
        for (Alternative alternative : alternatives) {
            if (alternative.matches(conditions))
                coveredAlternatives.add(alternative);
        }
        return coveredAlternatives;
    }

    private List<Alternative> getAllAlternativesCoveredByRules(List<Alternative> alternatives, List<Rule> rules) {
        List<Alternative> coveredAlternatives = new ArrayList<>();
        for (Rule rule : rules) {
            coveredAlternatives.addAll(getCoveredAlternatives(alternatives, rule.conditions));
        }
        return coveredAlternatives;
    }

    private int numberOfCovered(List<Alternative> alternatives, List<Attribute> conditions) {
        int covered = 0;
        for (Alternative alternative : alternatives) {
            if (alternative.matches(conditions))
                covered++;
        }
        return covered;
    }

    private List<Attribute> getUniqueConditions(List<Alternative> notCovered, List<Attribute> conditions) {
        List<Attribute> uniqueConditions = new ArrayList<>();
        for (Alternative alternative : notCovered) {
            for (Attribute attribute : alternative.attributes) {
                if (!uniqueConditions.contains(attribute) && !conditions.contains(attribute))
                    uniqueConditions.add(attribute);
            }
        }
        return uniqueConditions;
    }

    private List<AttributeValuePair> evaluateConditions(List<Attribute> uniqueConditionCandidates, List<Attribute> currentConditions, List<Alternative> notCovered) {
        List<AttributeValuePair> attributeValuePairs = new ArrayList<>();
        for (Attribute conditionCandidate : uniqueConditionCandidates) {
            switch (bestConditionChoiceMode) {
                case ORIGINAL:
                    attributeValuePairs.add(new AttributeValuePair(conditionCandidate, numberOfCovered(notCovered, Collections.singletonList(conditionCandidate)), numberOfCovered(allAlternatives, Collections.singletonList(conditionCandidate))));
                    break;
                case MODIFIED:
                    List<Attribute> newConditions = new ArrayList<>(currentConditions);
                    newConditions.add(conditionCandidate);
                    attributeValuePairs.add(new AttributeValuePair(conditionCandidate, numberOfCovered(notCovered, newConditions), numberOfCovered(allAlternatives, newConditions)));
                    break;
            }
        }
        return attributeValuePairs;
    }


    private Attribute chooseBestCondition(List<Alternative> notCovered, List<Attribute> currentConditions) {
        List<Attribute> uniqueConditions = getUniqueConditions(notCovered, currentConditions);
        List<AttributeValuePair> attributeValuePairs = evaluateConditions(uniqueConditions, currentConditions, notCovered);

        AttributeValuePair bestAttributeValuePair = Collections.min(attributeValuePairs, (o1, o2) -> {
            int comp = Integer.compare(o1.positiveCoverage, o2.positiveCoverage);
            if (comp != 0) {
                return -1 * comp;
            }
            return Integer.compare(o1.allAlternativesCoverage, o2.allAlternativesCoverage);
        });
        return bestAttributeValuePair.attribute;
    }
}
