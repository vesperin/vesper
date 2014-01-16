package edu.ucsc.refactor.util;

import java.util.UUID;

/**
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public class UniqueIdentifierGenerator {
    private UniqueIdentifierGenerator(){}

    /**
     * Generate random UUIDs...
     *
     * @return The unique identifier
     */
    public static String generateUniqueIdentifier(){
       return String.valueOf(UUID.randomUUID());
    }
}
