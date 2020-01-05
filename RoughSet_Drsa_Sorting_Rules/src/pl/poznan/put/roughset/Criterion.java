package pl.poznan.put.roughset;

abstract class Criterion {
    final String name;
    final PreferenceDirection preferenceDirection;

    abstract boolean meetCondition(Criterion criterion, UnionType unionType);

    Criterion(String name, PreferenceDirection preferenceDirection) {
        this.name = name;
        this.preferenceDirection = preferenceDirection;
    }
}
