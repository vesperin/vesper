package edu.ucsc.refactor.spi;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public enum Refactoring implements Name {
    REFORMAT_CODE("Reformat Code", "Vesper will automatically reformat a selected code block"),
    DEDUPLICATE("Deduplicate", "Vesper will automatically deduplicate existing code"),
    DELETE_TYPE("Delete Type", "Vesper will delete a selected type declaration"),
    DELETE_METHOD("Delete Method", "Vesper will delete a selected method"),
    DELETE_PARAMETER("Delete Parameter", "Vesper will delete the selected method parameter"),
    DELETE_FIELD("Delete Field", "Vesper will delete the selected field"),
    DELETE_REGION("Delete Region", "Vesper will delete the selected region"),
    DELETE_UNUSED_IMPORTS("Delete Unused Imports", "Vesper will optimize your imports"),
    RENAME_SELECTION("Rename selection", "Vesper will rename the selected member"),
    RENAME_METHOD("Rename Method", "Vesper will rename the name of the selected method"),
    RENAME_PARAMETER("Rename Parameter", "Vesper will rename the name of the selected method's parameter"),
    RENAME_FIELD("Rename field", "Vesper will rename the name of a class's selected field"),
    RENAME_TYPE("Rename Type", "Vesper will rename the name of a class or interface");

    private static final Map<String, Refactoring> LOOK_UP = new HashMap<String, Refactoring>();

    static {
        // Populate the lookup table on loading time
        for(Refactoring each : EnumSet.allOf(Refactoring.class)){
            LOOK_UP.put(each.getKey(), each);
        }
    }

    private final String key;
    private final String summary;

    /**
     * Constructs {@code Operations} enum with {@code key}
     * and {@code summary} as values.
     * @param key The name key
     * @param summary A short description of this name.
     */
    Refactoring(String key, String summary){
        this.key            = key;
        this.summary        = summary;
    }

    /**
     * Returns the Operation's {@code Name} matching a given {@code key}.
     * @param key The Operation's key {@code Name}
     * @return The desired Operation's {@code Name}
     * @throws NoSuchElementException if {@code Name} not found!
     */
    public static Refactoring from(String key){
        if(LOOK_UP.containsKey(key)){
            return LOOK_UP.get(key);
        }

        throw new NoSuchElementException("Name not found!");
    }

    @Override public String getKey() {
        return key;
    }

    @Override public String getSummary() {
        return summary;
    }

    /**
     * Checks whether <tt>this</tt> {@code Name} is same as the other given
     * {@code Name}.
     *
     * @param other The other {@code Name}
     * @return {@code true} if they are the same. {@code false} otherwise.
     */
    public boolean isSame(Name other){
        return this == other;
    }


    @Override public String toString() {
        return getKey() + ": " + getSummary() + ".";
    }
}
