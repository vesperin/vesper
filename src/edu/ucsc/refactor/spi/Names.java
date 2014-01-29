package edu.ucsc.refactor.spi;

import com.google.common.collect.Maps;

import java.util.Map;
import java.util.NoSuchElementException;

/**
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public class Names {
    private static final Map<Smell, Refactoring>  WARNING_RESPONSE_MAP  = Maps.newEnumMap(Smell.class);

    static {
        WARNING_RESPONSE_MAP.put(Smell.UNFORMATTED_CODE, Refactoring.REFORMAT_CODE);
        WARNING_RESPONSE_MAP.put(Smell.DUPLICATED_CODE, Refactoring.DEDUPLICATE);
        WARNING_RESPONSE_MAP.put(Smell.UNUSED_FIELD, Refactoring.DELETE_FIELD);
        WARNING_RESPONSE_MAP.put(Smell.UNUSED_PARAMETER, Refactoring.DELETE_PARAMETER);
        WARNING_RESPONSE_MAP.put(Smell.UNUSED_METHOD, Refactoring.DELETE_METHOD);
        WARNING_RESPONSE_MAP.put(Smell.UNUSED_IMPORTS, Refactoring.DELETE_UNUSED_IMPORTS);
        WARNING_RESPONSE_MAP.put(Smell.UNUSED_TYPE, Refactoring.DELETE_TYPE);
    }

    private Names(){}

    /**
     * @return THe response to this warning.
     * @throws NoSuchElementException if response is not found!. Try
     *      calling the {@link Names#hasAvailableResponse(Smell)} before calling this method; otherwise
     *      we will make you pay for any laziness {@code ;-)} by throwing this exception.
     */
    public static Refactoring from(Smell warning){
        if(hasAvailableResponse(warning)){
            return Refactoring.class.cast(WARNING_RESPONSE_MAP.get(warning));
        }

        throw new NoSuchElementException("No available response for warning!");
    }

    /**
     * Checks whether this warning has any available response.
     *
     * @param warning The {@code Smell}
     * @return {@code true} if there is any available response.
     */
    public static boolean hasAvailableResponse(Smell warning){
        return WARNING_RESPONSE_MAP.containsKey(warning);
    }

}
