package pl.poznan.put.roughset;

import java.util.Map;

class OrdinalCriterion extends Criterion {
    private final Map<String, Integer> ordinalMap;
    private final String value;

    public String getValue() {
        return value;
    }

    OrdinalCriterion(String name, PreferenceDirection preferenceDirection, String value, Map<String, Integer> ordinalMap) {
        super(name, preferenceDirection);
        this.value = value;
        this.ordinalMap = ordinalMap;
    }

    @Override
    boolean meetCondition(Criterion criterion, UnionType unionType) {
        OrdinalCriterion ordinalCriterion = (OrdinalCriterion) criterion;

        Integer thisCriterionImportance = ordinalMap.get(value);
        Integer expectedCriterionImportance = ordinalMap.get(ordinalCriterion.value);

        if (unionType == UnionType.AT_LEAST) {
            if (preferenceDirection == PreferenceDirection.GAIN) {
                return thisCriterionImportance >= expectedCriterionImportance;
            } else {
                return thisCriterionImportance <= expectedCriterionImportance;
            }
        } else {
            if (preferenceDirection == PreferenceDirection.GAIN) {
                return thisCriterionImportance <= expectedCriterionImportance;
            } else {
                return thisCriterionImportance >= expectedCriterionImportance;
            }
        }
    }

    @Override
    Criterion calculateDifference(Criterion criterion) {
        OrdinalCriterion ordinalCriterion = (OrdinalCriterion) criterion;
        return new OrdinalCriterionPair(this.name, this.preferenceDirection, this, ordinalCriterion);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this)
            return true;
        if (!(obj instanceof OrdinalCriterion))
            return false;
        OrdinalCriterion a = (OrdinalCriterion) obj;
        return this.name.equals(a.name) && this.preferenceDirection.equals(a.preferenceDirection) && this.value.equals(a.value);
    }
}
