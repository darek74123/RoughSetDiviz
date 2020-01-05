package pl.poznan.put.roughset;

class AttributeValuePair {
    final Attribute attribute;
    final int positiveCoverage; //number of covered alternatives (from positive, not covered yet set)
    final int allAlternativesCoverage; //number of all alternatives covered, including negative region

    AttributeValuePair(Attribute attribute, int positiveCoverage, int allAlternativesCoverage) {
        this.attribute = attribute;
        this.positiveCoverage = positiveCoverage;
        this.allAlternativesCoverage = allAlternativesCoverage;
    }
}
