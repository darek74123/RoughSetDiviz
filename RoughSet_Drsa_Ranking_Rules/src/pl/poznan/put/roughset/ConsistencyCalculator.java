package pl.poznan.put.roughset;

import java.util.ArrayList;
import java.util.List;

public class ConsistencyCalculator {
    final Measure measure;

    public enum Measure {
        ROUGH_MEMBERSHIP,
        EPSILON,
        EPSILON_PRIME
    }

    ConsistencyCalculator(Measure measure) {
        this.measure = measure;
    }

    ConsistencyMeasure calculateMeasure(List<Alternative> allAlternatives, List<Alternative> approximation, List<Condition> conditions) {
        switch (measure) {
            case ROUGH_MEMBERSHIP:
                return roughMembershipConsistencyMeasure(allAlternatives, approximation, conditions);
            case EPSILON:
                return epsilonConsistencyMeasure(allAlternatives, approximation, conditions);
            case EPSILON_PRIME:
                return epsilonPrimeConsistencyMeasure(allAlternatives, approximation, conditions);
            default:
                throw new IllegalArgumentException("Illegal measure type");
        }
    }

    private int numberOfCovered(List<Alternative> alternatives, List<Condition> conditions) {
        int covered = 0;
        for (Alternative alternative : alternatives) {
            if (alternative.matches(conditions))
                covered++;
        }
        return covered;
    }

    private List<Alternative> getApproximationComplement(List<Alternative> allAlternatives, List<Alternative> approximation) {
        List<Alternative> approximationComplement = new ArrayList<>(allAlternatives);
        approximationComplement.removeAll(approximation);
        return approximationComplement;
    }

    private ConsistencyMeasure roughMembershipConsistencyMeasure(List<Alternative> allAlternatives, List<Alternative> approximation, List<Condition> conditions) {
        int numberOfPositiveObjectsCoveredByConditions = numberOfCovered(approximation, conditions);
        int numberOfAllObjectsCoveredByConditions = numberOfCovered(allAlternatives, conditions);

        double measureValue = (double) numberOfPositiveObjectsCoveredByConditions / (double) numberOfAllObjectsCoveredByConditions;
        return new ConsistencyMeasure(measureValue, ConsistencyMeasure.Type.GAIN);
    }

    private ConsistencyMeasure epsilonConsistencyMeasure(List<Alternative> allAlternatives, List<Alternative> approximation, List<Condition> conditions) {
        List<Alternative> approximationComplement = getApproximationComplement(allAlternatives, approximation);
        int numberOfNegativeObjectsCoveredByConditions = numberOfCovered(approximationComplement, conditions);
        int numberOfNegativeObjects = approximationComplement.size();

        double measureValue = (double) numberOfNegativeObjectsCoveredByConditions / (double) numberOfNegativeObjects;
        return new ConsistencyMeasure(measureValue, ConsistencyMeasure.Type.COST);
    }

    private ConsistencyMeasure epsilonPrimeConsistencyMeasure(List<Alternative> allAlternatives, List<Alternative> approximation, List<Condition> conditions) {
        List<Alternative> approximationComplement = getApproximationComplement(allAlternatives, approximation);
        int numberOfNegativeObjectsCoveredByConditions = numberOfCovered(approximationComplement, conditions);
        int numberOfPositiveObjects = approximation.size();

        double measureValue = (double) numberOfNegativeObjectsCoveredByConditions / (double) numberOfPositiveObjects;
        return new ConsistencyMeasure(measureValue, ConsistencyMeasure.Type.COST);
    }
}
