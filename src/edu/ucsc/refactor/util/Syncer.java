package edu.ucsc.refactor.util;

import edu.ucsc.refactor.Clip;
import edu.ucsc.refactor.Diff;
import edu.ucsc.refactor.Introspector;
import edu.ucsc.refactor.Source;

import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

/**
 * It propagates changes made to a single clip that belongs to a clip space.
 *
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public class Syncer {
    private static final Logger LOGGER = Logger.getLogger(Syncer.class.getName());

    public static Clip patch(Introspector introspector, Clip original, Clip revised){
        try {
            final Diff diff = introspector.differences(original.getSource(), revised.getSource());
            return Clip.makeClip(revised.getMethodName(),
                    original.getLabel() + " U " + revised.getLabel(),
                    diff.resolve()
            );
        } catch (RuntimeException ex){
            LOGGER.warning("Unable to change " + original.getSource().getName());
            LOGGER.warning("Unable to resolve changes found in " + revised.getSource().getName());
            return null;
        }
    }

    /**
     * Syncs, in a forward fashion, the differences between code examples; starting
     * at the clip after the base.
     *
     * @param selectedClips the divided clip space.
     * @return the patched {@link Source} object.
     */
    public static Source sync(Introspector introspector, List<Clip> selectedClips){

       // [1, 2, 3, 4] => [[1, 2] & 3] & 4
       Source  result  = null;
       Clip    left    = null;
       final Iterator<Clip> itr = selectedClips.iterator();
       while(itr.hasNext()){
        final Clip right = itr.next();
        if(left == null) {
           if(!right.isBaseClip()){
               left = right;
           }

           if(itr.hasNext()){
             continue;
           }
        }

        final Clip patched = patch(introspector, left, right);
        if(patched == null) return result;

        result = patched.getSource();
        left   = patched;
       }

       return result;
    }

}
