package pl.poznan.put.roughset;

class Condition {
    final UnionType unionType;
    final Criterion criterion;

    Condition(Criterion criterion, UnionType unionType) {
        this.criterion = criterion;
        this.unionType = unionType;
    }

    String getCardinalOperator() {
        if (unionType == UnionType.AT_LEAST) {
            if (criterion.preferenceDirection == PreferenceDirection.GAIN)
                return "geq"; // >=
            else
                return "leq"; // <=
        } else {
            if (criterion.preferenceDirection == PreferenceDirection.GAIN)
                return "leq"; // <=
            else
                return "geq"; // >=
        }
    }

    String getFirstOperator() {
        if (unionType == UnionType.AT_LEAST) {
            if (criterion.preferenceDirection == PreferenceDirection.GAIN)
                return "geq"; // >=
            else
                return "leq"; // <=
        } else {
            if (criterion.preferenceDirection == PreferenceDirection.GAIN)
                return "leq"; // <=
            else
                return "geq"; // >=
        }
    }

    String getSecondOperator() {
        if (unionType == UnionType.AT_MOST) {
            if (criterion.preferenceDirection == PreferenceDirection.GAIN)
                return "geq"; // >=
            else
                return "leq"; // <=
        } else {
            if (criterion.preferenceDirection == PreferenceDirection.GAIN)
                return "leq"; // <=
            else
                return "geq"; // >=
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this)
            return true;
        if (!(obj instanceof Condition))
            return false;
        Condition c = (Condition) obj;
        return this.criterion.equals(c.criterion) && this.unionType.equals(c.unionType);
    }
}
