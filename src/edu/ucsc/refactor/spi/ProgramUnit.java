package edu.ucsc.refactor.spi;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public enum ProgramUnit {
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

    private static final Map<String, ProgramUnit> LOOK_UP = new HashMap<String, ProgramUnit>();

    static {
        // Populate the lookup table on loading time
        for(ProgramUnit each : EnumSet.allOf(ProgramUnit.class)){
            LOOK_UP.put(each.key, each);
        }
    }


    private final String key;

    ProgramUnit(String key){
        this.key = key;
    }

    public static ProgramUnit from(String key){
        if(LOOK_UP.containsKey(key)){
            return LOOK_UP.get(key);
        }

        throw new NoSuchElementException("Key not found!");
    }


    public boolean isSame(ProgramUnit that){
        return this == that;
    }

    @Override public String toString() {
        return key;
    }
}
