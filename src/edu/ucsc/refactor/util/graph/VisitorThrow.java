package edu.ucsc.refactor.util.graph;

/**
 * A graph visitor interface that can throw an exception during a visit
 * callback.
 *
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 * @param <T>
 * @param <E>
 */
public interface VisitorThrow<T, E extends Exception> {
    /**
     * Called by the graph traversal methods when a vertex is first visited.
     *
     * @param g -
     *          the graph
     * @param v -
     *          the vertex being visited.
     * @throws E
     *           exception for any error
     */
    void visit(Graph<T> g, Vertex<T> v) throws E;
}
