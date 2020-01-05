package pl.poznan.put.roughset;

import java.util.List;
import java.util.Map;

class Alternative {
    final Map<String, Criterion> criteria;

    Alternative(Map<String, Criterion> criteria) {
        this.criteria = criteria;
    }

    boolean matches(List<Condition> conditions) {
        for (Condition condition : conditions) {
            if (!criteria.get(condition.criterion.name).meetCondition(condition.criterion, condition.unionType)) {
                return false;
            }
        }
        return true;
    }

    boolean belongsToPositiveRegionOf(Alternative alternative, UnionType unionType) {
        for (Criterion criterion : alternative.criteria.values()) {
            Criterion thisAlternativeCriterion = criteria.get(criterion.name);
            if (!thisAlternativeCriterion.meetCondition(criterion, unionType)) {
                return false;
            }
        }
        return true;
    }
}
