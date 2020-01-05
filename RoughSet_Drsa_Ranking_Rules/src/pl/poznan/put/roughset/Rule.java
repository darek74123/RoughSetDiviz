package pl.poznan.put.roughset;

import java.util.ArrayList;
import java.util.List;

public class Rule {
    final List<Condition> conditions = new ArrayList<>();
    private final String concept;
    private final RuleType ruleType;

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

    Rule(String concept, RuleType ruleType) {
        this.concept = concept;
        this.ruleType = ruleType;
    }

    void addCondition(Condition newCondition) {
        conditions.add(newCondition);
    }
}
