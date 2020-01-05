package pl.poznan.put.roughset;

import java.util.List;

class Union {
    final List<Alternative> approximation;
    final List<Alternative> alternatives;
    final UnionType unionType;
    final String concept;

    Union(List<Alternative> approximation, List<Alternative> alternatives, UnionType unionType, String categoryID) {
        this.approximation = approximation;
        this.alternatives = alternatives;
        this.unionType = unionType;
        this.concept = categoryID;
    }

    boolean isComplementaryTo(Union union) {
        return !this.concept.equals(union.concept);
    }
}
