package pl.poznan.put.roughset.xmcda;

import java.util.List;

public class Union {
    public final String categoryID;
    public final String type;
    public final List<Pair<String, String>> approximation; //alternativesIds

    Union(String categoryID, String type, List<Pair<String, String>> approximation) {
        this.categoryID = categoryID;
        this.type = type;
        this.approximation = approximation;
    }
}
