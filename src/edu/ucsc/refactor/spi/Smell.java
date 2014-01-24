package edu.ucsc.refactor.spi;

import java.util.*;

/**
 * <strong>PLEASE REMEMBER THIS</strong> THis is how amendments will be triggered.
 * Issue delete command containing a selection:
 * 1. we use the selection to determine what we need to delete
 * 2. if found, then trigger the appropriate strategy.
 *
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public enum Smell implements Name {
    UNFORMATTED_CODE("Unformatted code", "Vesper has detected unformatted code!"),
    UNUSED_TYPE("Unused type", "Vesper has detected one or more unused type declarations!"),
    UNUSED_METHOD("Unused method", "Vesper has detected one or more unused methods!"),
    UNUSED_PARAMETER("Unused parameter", "Vesper has detected unused parameters in one or more methods"),
    UNUSED_FIELD("Unused field", "Vesper has detected one or more unused fields in code!"),
    UNUSED_IMPORTS("Unused imports", "Vesper has detected one or more unused imports"),
    MAGIC_NUMBER("Magic Number", "Vesper has detected the use of literals in conditional " +
            "statements");

    private static final Map<String, Smell> LOOK_UP = new HashMap<String, Smell>();


    static {
        // Populate the lookup table on loading time
        for(Smell each : EnumSet.allOf(Smell.class)){
            LOOK_UP.put(each.getKey(), each);
        }
    }

    private final String key;
    private final String summary;

    /**
     * Constructs a {@code Name} with {@code key}
     * and {@code summary} as values.
     * @param key The name key
     * @param summary A short description of this name.
     */
    Smell(String key, String summary){
        this.key            = key;
        this.summary        = summary;
    }


    @Override public String getKey(){
        return key;
    }

    @Override public String getSummary(){
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

    /**
     * Returns the {@code Name} object matching a given {@code key}.
     * @param key THe {@code Name}'s key
     * @return The desired {@code Name}
     * @throws NoSuchElementException if {@code Name} not found!
     */
    public static Smell from(String key){
        if(LOOK_UP.containsKey(key)){
            return LOOK_UP.get(key);
        }

        throw new NoSuchElementException("Name not found!");
    }


    @Override public String toString() {
        return getKey() + ": " + getSummary() + ".";
    }
}
