package pl.poznan.put.roughset;

class Attribute {
    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

    private final String name;
    private final String value;

    Attribute(String name, String value) {
        this.name = name;
        this.value = value;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this)
            return true;
        if (!(obj instanceof Attribute))
            return false;
        Attribute a = (Attribute) obj;
        return this.name.equals(a.name) && this.value.equals(a.value);
    }
}
