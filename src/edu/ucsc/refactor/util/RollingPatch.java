package edu.ucsc.refactor.util;

import edu.ucsc.refactor.Clip;
import edu.ucsc.refactor.Diff;
import edu.ucsc.refactor.Introspector;
import edu.ucsc.refactor.Source;

import java.util.List;
import java.util.logging.Logger;

/**
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public class RollingPatch {
    private static final Logger LOGGER = Logger.getLogger(RollingPatch.class.getName());
    /**
     * Resolves the differences in code found in a code example
     * that has been multi-staged.
     *
     *
     * @param stages the clip space.
     * @return the patched {@link Source} object.
     */
    // TODO(Huascar) turn this into DeliveryStrategy interface
    // one for direct string patching, and the other one for AST swapping.
    public static Source patch(Introspector introspector, List<Clip> stages){
       // [1, 2, 3, 4] => [[1, 2] & 3] & 4
       Source  result  = null;
       Clip    left    = null;
       for(Clip right : stages){
         if(left == null) { left = right; continue; }

         final Diff diff = introspector.differences(left.getSource(), right.getSource());

         try {
           result = diff.resolve();
           left   = Clip.makeClip(left.getLabel() + " U " + right.getLabel(), result);
         } catch (RuntimeException ex){
           LOGGER.warning("Unable to change " + diff.getOriginal().getName());
           LOGGER.warning("Unable to resolve changes found in " + diff.getRevised().getName());
           return result;
         }

       }

       return result;
    }

}
