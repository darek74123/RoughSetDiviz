package pl.poznan.put.roughset;

import java.util.ArrayList;
import java.util.List;

public class Rule {
    final List<Attribute> conditions = new ArrayList<>();

    public String getConcept() {
        return concept;
    }

    public String getRuleType() {
        return ruleType.toString().toLowerCase();
    }

    private final String concept;
    private final RuleType ruleType;

    public enum RuleType {
        CERTAIN,
        POSSIBLE
    }

    //rule statistics
    //total number of attribute-value pairs on the left-hand side of the rule
    int specificity;
    //the total number of cases correctly classified by the rule during training
    int strength;
    //total number of training cases matching the left-hand side of the rule
    int coverageCardinality;


    Rule(String concept, RuleType ruleType) {
        this.concept = concept;
        this.ruleType = ruleType;
    }

    void addCondition(Attribute newCondition) {
        conditions.add(newCondition);
    }
}
