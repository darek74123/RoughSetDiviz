package pl.poznan.put.roughset;

public class ConsistencyMeasure {
    private final Double value;
    private final Type type;

    ConsistencyMeasure(Double value, Type type) {
        this.value = value;
        this.type = type;
    }

    boolean isBetterThan(ConsistencyMeasure consistencyMeasure) {
        if (this.type != consistencyMeasure.type)
            throw new IllegalArgumentException("Cannot compare GAIN and COST type measures");
        int c = this.value.compareTo(consistencyMeasure.value);
        if (this.type == Type.GAIN) {
            return c > 0;
        } else {
            return -1 * c > 0;
        }
    }

    boolean equals(ConsistencyMeasure consistencyMeasure) {
        return this.value.equals(consistencyMeasure.value);
    }

    boolean satisfiesThreshold(double threshold) {
        if (this.type == Type.GAIN) {
            return value >= threshold;
        } else {
            return value <= threshold;
        }
    }

    enum Type {
        COST,
        GAIN
    }
}
