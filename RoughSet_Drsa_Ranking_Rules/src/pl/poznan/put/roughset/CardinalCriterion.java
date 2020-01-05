package pl.poznan.put.roughset;

class CardinalCriterion extends Criterion {
    private final Double value;

    public Double getValue() {
        return value;
    }

    CardinalCriterion(String name, PreferenceDirection preferenceDirection, Double value) {
        super(name, preferenceDirection);
        this.value = value;
    }

    boolean meetCondition(Criterion criterion, UnionType unionType) {
        CardinalCriterion cardinalCriterion = (CardinalCriterion) criterion;
        if (unionType == UnionType.AT_LEAST) {
            if (preferenceDirection == PreferenceDirection.GAIN) {
                return value >= cardinalCriterion.value;
            } else {
                return value <= cardinalCriterion.value;
            }
        } else {
            if (preferenceDirection == PreferenceDirection.GAIN) {
                return value <= cardinalCriterion.value;
            } else {
                return value >= cardinalCriterion.value;
            }
        }
    }

    @Override
    Criterion calculateDifference(Criterion criterion) {
        CardinalCriterion cardinalCriterion = (CardinalCriterion) criterion;
        Double valueDifference = this.value - cardinalCriterion.value;
        return new CardinalCriterion(this.name, this.preferenceDirection, valueDifference);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this)
            return true;
        if (!(obj instanceof CardinalCriterion))
            return false;
        CardinalCriterion a = (CardinalCriterion) obj;
        return this.name.equals(a.name) && this.preferenceDirection.equals(a.preferenceDirection) && this.value.equals(a.value);
    }
}
