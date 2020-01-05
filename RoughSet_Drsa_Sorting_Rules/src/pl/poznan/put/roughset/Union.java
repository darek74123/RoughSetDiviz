package pl.poznan.put.roughset;

import java.util.List;

class Union {
    final List<Alternative> approximation;
    final List<Alternative> alternatives;
    final UnionType unionType;
    final Integer conceptRank;
    final String concept;

    Union(List<Alternative> approximation, List<Alternative> alternatives, UnionType unionType, String categoryID, Integer conceptRank) {
        this.approximation = approximation;
        this.alternatives = alternatives;
        this.unionType = unionType;
        this.conceptRank = conceptRank;
        this.concept = categoryID;
    }

    boolean isComplementaryTo(Union union) {
        int complementaryUnionConcept;
        UnionType complementaryUnionType;

        if (union.unionType == UnionType.AT_LEAST) {
            complementaryUnionConcept = union.conceptRank - 1;
            complementaryUnionType = UnionType.AT_MOST;
        } else {
            complementaryUnionConcept = union.conceptRank + 1;
            complementaryUnionType = UnionType.AT_LEAST;
        }

        return this.conceptRank.equals(complementaryUnionConcept) && this.unionType.equals(complementaryUnionType);
    }
}
