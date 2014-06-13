package edu.ucsc.refactor.spi;

import edu.ucsc.refactor.callgraph.GraphNode;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;

import java.util.Map;

/**
 * A type that specify how to measure centrality.
 *
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public interface CentralityMeasure {
    /**
     * Calculates the centrality given a {@code CallGraph}.
     *
     * @return A map of {@code nodes} and their centrality scores.
     */
    Map<GraphNode, Double> calculateCentrality(Graph<GraphNode, DefaultEdge> callGraph);
}
