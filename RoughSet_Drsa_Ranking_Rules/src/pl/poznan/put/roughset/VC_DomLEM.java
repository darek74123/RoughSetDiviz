package pl.poznan.put.roughset;

import java.util.*;

public class VC_DomLEM {
    private final List<Alternative> alternatives;
    private final List<Union> unions;
    private final List<Rule> allUnionsRules;
    private final AllowedObjectsType allowedObjectsType;
    private final ConsistencyCalculator consistencyCalculator;
    private final double consistencyThreshold;
    private final Rule.RuleType ruleType;

    VC_DomLEM(List<Alternative> alternatives, List<Union> unions, AllowedObjectsType allowedObjectsType, ConsistencyCalculator.Measure measure, double consistencyThreshold, Rule.RuleType ruleType) {
        this.alternatives = alternatives;
        this.unions = unions;
        this.allUnionsRules = new ArrayList<>();
        this.allowedObjectsType = allowedObjectsType;
        this.consistencyCalculator = new ConsistencyCalculator(measure);
        this.consistencyThreshold = consistencyThreshold;
        this.ruleType = ruleType;
    }

    private double calculateConsistencyThresholdForUnion(Union union) {
        switch (consistencyCalculator.measure) {
            case EPSILON:
                List<Alternative> unionComplementAlternatives = new ArrayList<>(alternatives);
                unionComplementAlternatives.removeAll(union.alternatives);
                int unionComplementCardinality = unionComplementAlternatives.size();

                List<Alternative> unionApproximationComplementAlternatives = new ArrayList<>(alternatives);
                unionApproximationComplementAlternatives.removeAll(union.approximation);
                int unionApproximationComplementCardinality = unionApproximationComplementAlternatives.size();

                return (double) unionComplementCardinality / (double) unionApproximationComplementCardinality * consistencyThreshold;
            case EPSILON_PRIME:
                int unionCardinality = union.alternatives.size();
                int unionApproximationCardinality = union.approximation.size();
                return (double) unionCardinality / (double) unionApproximationCardinality * consistencyThreshold;
            case ROUGH_MEMBERSHIP:
                return consistencyThreshold;
            default:
                throw new IllegalArgumentException("Illegal measure type");
        }
    }

    List<Rule> runAlgorithm() {
        for (Union union : unions) {
            List<Alternative> allowedObjects = getAllowedObjects(union);
            List<Rule> thisUnionRules = VC_SequentialCovering(union, allowedObjects, calculateConsistencyThresholdForUnion(union));
            allUnionsRules.addAll(thisUnionRules);
        }
        return allUnionsRules;
    }

    private List<Alternative> getPositiveRegion(Union union) {
        List<Alternative> positiveRegion = new ArrayList<>();
        for (Alternative alternativeFromApproximation : union.approximation) {
            for (Alternative alternative : alternatives) {
                if (alternative.belongsToPositiveRegionOf(alternativeFromApproximation, union.unionType) && !positiveRegion.contains(alternative))
                    positiveRegion.add(alternative);
            }
        }
        return positiveRegion;
    }

    private Union findComplementUnion(Union union) {
        for (Union u : unions) {
            if (u.isComplementaryTo(union)) {
                return u;
            }
        }
        throw new NoSuchElementException("Complementary union not found in unions, if allowedObjectsType=positive_and_boundary_regions you must provide approximations for all unions");
    }


    private List<Alternative> getAllowedObjects(Union union) {
        List<Alternative> allowedObjects;
        switch (allowedObjectsType) {
            case POSITIVE_REGION:
                allowedObjects = getPositiveRegion(union);
                break;
            case POSITIVE_AND_BOUNDARY_REGIONS:
                List<Alternative> positiveRegion = getPositiveRegion(union);

                List<Alternative> negativeRegion = getPositiveRegion(findComplementUnion(union));
                negativeRegion.removeAll(positiveRegion);

                List<Alternative> boundaryRegion = new ArrayList<>(alternatives);
                boundaryRegion.removeAll(positiveRegion);
                boundaryRegion.removeAll(negativeRegion);

                allowedObjects = positiveRegion;
                allowedObjects.addAll(boundaryRegion);
                break;
            case WHOLE_DATASET:
                allowedObjects = alternatives;
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + allowedObjectsType);
        }
        return allowedObjects;
    }


    private List<Rule> VC_SequentialCovering(Union union, List<Alternative> allowedObjects, double consistencyThreshold) {
        List<Alternative> notCoveredYet = new ArrayList<>(union.approximation); //set B := set of positive objects (approximation)
        List<Alternative> alreadyCovered = new ArrayList<>();
        List<Rule> rules = new ArrayList<>(); // R_x = empty set


        while (notCoveredYet.size() > 0) {
            Rule currentRule = new Rule(union.concept, ruleType);
            List<Alternative> currentlyCoveredPositiveObjects = new ArrayList<>(notCoveredYet);

            while (currentRule.conditions.isEmpty() || !consistencyThresholdSatisfied(union.approximation, currentRule.conditions, consistencyThreshold) || !ruleCoversOnlyAllowedObjects(allowedObjects, currentRule.conditions)) {
                Condition newCondition = chooseBestCondition(union, currentlyCoveredPositiveObjects, currentRule.conditions);
                currentRule.addCondition(newCondition);
                currentlyCoveredPositiveObjects.removeIf(obj -> (!obj.matches(Collections.singletonList(newCondition))));
            }

            Iterator<Condition> i = currentRule.conditions.iterator();
            while (i.hasNext()) {
                Condition potentiallyUnnecessaryCondition = i.next();
                List<Condition> otherConditions = new ArrayList<>(currentRule.conditions);
                otherConditions.remove(potentiallyUnnecessaryCondition);
                if (consistencyThresholdSatisfied(union.approximation, otherConditions, consistencyThreshold) && ruleCoversOnlyAllowedObjects(allowedObjects, otherConditions)) {
                    i.remove();
                }
            }


            alreadyCovered.addAll(getCoveredAlternatives(union.approximation, currentRule.conditions));
            rules.add(currentRule);
            notCoveredYet = new ArrayList<>(union.approximation);
            notCoveredYet.removeAll(alreadyCovered);
        }

        List<Rule> removableRules = getRemovableRules(rules, union.approximation);
        while (removableRules.size() > 0) {
            Rule worstRule = getWorstRule(removableRules, union.approximation);
            rules.remove(worstRule);
            removableRules = getRemovableRules(rules, union.approximation);
        }

        return rules;
    }

    private Rule getWorstRule(List<Rule> removableRules, List<Alternative> approximation) {
        Rule worstRule = removableRules.get(0);
        int worstNumberOfCovered = numberOfCovered(approximation, worstRule.conditions);
        ConsistencyMeasure worstConsistencyMeasure = consistencyCalculator.calculateMeasure(alternatives, approximation, worstRule.conditions);

        for (int i = 1; i < removableRules.size(); i++) {
            int numberOfCovered = numberOfCovered(approximation, removableRules.get(i).conditions);
            ConsistencyMeasure consistencyMeasure = consistencyCalculator.calculateMeasure(alternatives, approximation, removableRules.get(i).conditions);
            if (worstNumberOfCovered > numberOfCovered || worstNumberOfCovered == numberOfCovered && worstConsistencyMeasure.isBetterThan(consistencyMeasure)) {
                worstRule = removableRules.get(i);
                worstNumberOfCovered = numberOfCovered;
                worstConsistencyMeasure = consistencyMeasure;
            }
        }

        return worstRule;
    }

    private List<Rule> getRemovableRules(List<Rule> rules, List<Alternative> objectsThatShouldBeCovered) {
        List<Rule> removableRules = new ArrayList<>();

        for (Rule potentiallyUnnecessaryRule : rules) {
            List<Rule> otherRules = new ArrayList<>(rules);
            otherRules.remove(potentiallyUnnecessaryRule);
            if ((getAllAlternativesCoveredByRules(alternatives, otherRules).containsAll(objectsThatShouldBeCovered))) {
                removableRules.add(potentiallyUnnecessaryRule);
            }
        }
        return removableRules;
    }


    private List<Alternative> getCoveredAlternatives(List<Alternative> alternatives, List<Condition> conditions) {
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

    private int numberOfCovered(List<Alternative> alternatives, List<Condition> conditions) {
        int numOfCovered = 0;
        for (Alternative alternative : alternatives) {
            if (alternative.matches(conditions))
                numOfCovered++;
        }
        return numOfCovered;
    }

    private boolean consistencyThresholdSatisfied(List<Alternative> approximation, List<Condition> conditions, double consistencyThreshold) {
        ConsistencyMeasure measure = consistencyCalculator.calculateMeasure(alternatives, approximation, conditions);
        return measure.satisfiesThreshold(consistencyThreshold);
    }

    private boolean ruleCoversOnlyAllowedObjects(List<Alternative> allowedObjects, List<Condition> conditions) {
        return allowedObjects.containsAll(getCoveredAlternatives(alternatives, conditions));
    }

    private List<Condition> getUniqueConditions(UnionType unionType, List<Alternative> currentlyCoveredObjects, List<Condition> conditions) {
        List<Condition> uniqueConditions = new ArrayList<>();
        for (Alternative alternative : currentlyCoveredObjects) {
            for (Criterion criterion : alternative.criteria.values()) {
                Condition condition = new Condition(criterion, unionType);
                if (!uniqueConditions.contains(condition) && !conditions.contains(condition))
                    uniqueConditions.add(condition);
            }
        }
        return uniqueConditions;
    }

    private Condition chooseBestCondition(Union union, List<Alternative> currentlyCoveredObjects, List<Condition> conditions) {
        List<Condition> uniqueConditions = getUniqueConditions(union.unionType, currentlyCoveredObjects, conditions);

        Condition best = uniqueConditions.get(0);
        List<Condition> newConditions = new ArrayList<>(conditions);
        newConditions.add(best);

        ConsistencyMeasure bestConsistencyMeasure = consistencyCalculator.calculateMeasure(alternatives, union.approximation, newConditions);
        int bestNumOfCovered = numberOfCovered(union.approximation, newConditions);

        for (int i = 1; i < uniqueConditions.size(); i++) {
            newConditions = new ArrayList<>(conditions);
            Condition conditionCandidate = uniqueConditions.get(i);
            newConditions.add(conditionCandidate);
            ConsistencyMeasure consistencyMeasure = consistencyCalculator.calculateMeasure(alternatives, union.approximation, newConditions);
            int numOfCovered = numberOfCovered(union.approximation, newConditions);
            if (consistencyMeasure.isBetterThan(bestConsistencyMeasure) || consistencyMeasure.equals(bestConsistencyMeasure) && numOfCovered > bestNumOfCovered) {
                best = conditionCandidate;
                bestConsistencyMeasure = consistencyMeasure;
                bestNumOfCovered = numOfCovered;
            }
        }
        return best;
    }
}
