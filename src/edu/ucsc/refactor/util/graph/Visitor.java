package edu.ucsc.refactor.util.graph;

/**
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
/**
 * A graph visitor interface.
 * @param <T>
 */
public interface Visitor<T> {
    /**
     * Called by the graph traversal methods when a vertex is first visited.
     *
     * @param g -
     *          the graph
     * @param v -
     *          the vertex being visited.
     */
    void visit(Graph<T> g, Vertex<T> v);
}