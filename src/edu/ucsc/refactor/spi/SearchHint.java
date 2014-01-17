package edu.ucsc.refactor.spi;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public enum SearchHint {
    /** **/
    METHOD("method"),
    /** **/
    PARAM("param"),
    /** **/
    FIELD("field"),
    /** **/
    INNER_CLASS("class"),
    /** none **/
    NONE("none");

    private static final Map<String, SearchHint> LOOK_UP = new HashMap<String, SearchHint>();

    static {
        // Populate the lookup table on loading time
        for(SearchHint each : EnumSet.allOf(SearchHint.class)){
            LOOK_UP.put(each.key, each);
        }
    }


    private final String key;

    SearchHint(String key){
        this.key = key;
    }

    public static SearchHint from(String key){
        if(LOOK_UP.containsKey(key)){
            return LOOK_UP.get(key);
        }

        throw new NoSuchElementException("Key not found!");
    }


    public boolean isSame(SearchHint that){
        return this == that;
    }

    @Override public String toString() {
        return key;
    }
}
