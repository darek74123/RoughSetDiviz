package pl.poznan.put.roughset;

import java.util.ArrayList;
import java.util.List;

public class Rule {
    final List<Condition> conditions = new ArrayList<>();
    private final UnionType unionType;
    private final String concept;
    private final Integer conceptRank;
    private final RuleType ruleType;

    UnionType getUnionType() {
        return unionType;
    }

    String getConcept() {
        return concept;
    }

    String getRuleType() {
        return ruleType.toString().toLowerCase();
    }

    public enum RuleType {
        CERTAIN,
        POSSIBLE
    }

    Rule(UnionType unionType, String concept, Integer conceptRank, RuleType ruleType) {
        this.unionType = unionType;
        this.concept = concept;
        this.conceptRank = conceptRank;
        this.ruleType = ruleType;
    }

    void addCondition(Condition newCondition) {
        conditions.add(newCondition);
    }

    private Condition findCondition(String conditionName, Rule rule) {
        for (Condition condition : rule.conditions) {
            if (condition.criterion.name.equals(conditionName))
                return condition;
        }
        return null;
    }

    boolean hasNotLessGeneralConditionsThan(Rule potentiallyUnnecessaryRule) {
        if (!this.unionType.equals(potentiallyUnnecessaryRule.unionType))
            return false; // consider only the same direction of rules (at_least, at_most)
        if (this.conditions.size() > potentiallyUnnecessaryRule.conditions.size())
            return false; // rule with more conditions cannot be "not less general"
        for (Condition condition : this.conditions) {
            Condition potentiallyUnnecessaryRuleCondition = findCondition(condition.criterion.name, potentiallyUnnecessaryRule);
            if (potentiallyUnnecessaryRuleCondition == null) {
                return false; // rule cannot be "not less general", if contains condition that other rule does not
            } else {
                if (!potentiallyUnnecessaryRuleCondition.criterion.meetCondition(condition.criterion, condition.unionType)) {
                    return false;  // here condition is more strict, so rule cannot be "not less general"
                }
            }
        }
        return true;
    }

    boolean hasNotLessSpecificDecisionThan(Rule potentiallyUnnecessaryRule) {
        if (unionType.equals(UnionType.AT_MOST)) {
            return conceptRank <= potentiallyUnnecessaryRule.conceptRank;
        } else {
            return conceptRank >= potentiallyUnnecessaryRule.conceptRank;
        }
    }
}
