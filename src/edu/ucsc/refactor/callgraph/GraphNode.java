package edu.ucsc.refactor.callgraph;

import edu.ucsc.refactor.Location;

/**
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public interface GraphNode {
    String getClassID();
    String getMethodID();
    Location getLocation();

}
