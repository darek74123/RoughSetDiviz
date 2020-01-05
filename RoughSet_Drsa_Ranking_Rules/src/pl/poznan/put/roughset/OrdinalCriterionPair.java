package pl.poznan.put.roughset;

class OrdinalCriterionPair extends Criterion {
    final OrdinalCriterion firstAlternativeValue;
    final OrdinalCriterion secondAlternativeValue;

    OrdinalCriterionPair(String name, PreferenceDirection preferenceDirection, OrdinalCriterion firstAlternativeValue, OrdinalCriterion secondAlternativeValue) {
        super(name, preferenceDirection);
        this.firstAlternativeValue = firstAlternativeValue;
        this.secondAlternativeValue = secondAlternativeValue;
    }

    @Override
    boolean meetCondition(Criterion criterion, UnionType unionType) {
        OrdinalCriterionPair ordinalCriterionPair = (OrdinalCriterionPair) criterion;

        OrdinalCriterion firstAlternative = ordinalCriterionPair.firstAlternativeValue;
        OrdinalCriterion secondAlternative = ordinalCriterionPair.secondAlternativeValue;

        if (unionType == UnionType.AT_LEAST) {
            if (preferenceDirection == PreferenceDirection.GAIN) {
                return firstAlternativeValue.meetCondition(firstAlternative, UnionType.AT_LEAST) && secondAlternative.meetCondition(secondAlternativeValue, UnionType.AT_LEAST);
            } else {
                return firstAlternative.meetCondition(firstAlternativeValue, UnionType.AT_LEAST) && secondAlternativeValue.meetCondition(secondAlternative, UnionType.AT_LEAST);
            }
        } else {
            if (preferenceDirection == PreferenceDirection.GAIN) {
                return firstAlternative.meetCondition(firstAlternativeValue, UnionType.AT_LEAST) && secondAlternativeValue.meetCondition(secondAlternative, UnionType.AT_LEAST);

            } else {
                return firstAlternativeValue.meetCondition(firstAlternative, UnionType.AT_LEAST) && secondAlternative.meetCondition(secondAlternativeValue, UnionType.AT_LEAST);
            }
        }
    }

    @Override
    Criterion calculateDifference(Criterion criterion) {
        throw new UnsupportedOperationException("Method not applicable fro OrdinalCriterionPair");
    }
}
