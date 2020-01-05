package pl.poznan.put.roughset;

public enum PreferenceDirection {
    COST("min"),
    GAIN("max");

    private final String label;

    PreferenceDirection(String label) {
        this.label = label;
    }

    public static PreferenceDirection valueOfLabel(String label) {
        for (PreferenceDirection e : PreferenceDirection.values()) {
            if (e.label.equalsIgnoreCase(label)) {
                return e;
            }
        }
        return null;
    }
}
