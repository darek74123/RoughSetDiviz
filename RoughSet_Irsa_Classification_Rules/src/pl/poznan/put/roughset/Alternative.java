package pl.poznan.put.roughset;

import java.util.List;

class Alternative {
    final List<Attribute> attributes;

    Alternative(List<Attribute> attributes) {
        this.attributes = attributes;
    }

    boolean matches(List<Attribute> conditions) {
        return attributes.containsAll(conditions);
    }
}
