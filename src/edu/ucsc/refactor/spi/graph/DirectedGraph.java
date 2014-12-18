package edu.ucsc.refactor.spi.graph;

import java.util.List;

/**
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public interface DirectedGraph <T> {
    /**
     * Add a vertex to the graph
     *
     * @param v the Vertex to add
     * @return true if the vertex was added, false if it was already in the graph.
     */
    boolean addVertex(Vertex<T> v);

    /**
     * Set a root vertex. If root does no exist in the graph it is added.
     *
     * @param root -
     *          the vertex to set as the root and optionally add if it does not
     *          exist in the graph.
     */
    public void addRootVertex(Vertex<T> root);

    /**
     * Insert a directed Edge<T> into the graph.
     *
     * @param from the Edge<T> starting vertex
     * @param to the Edge<T> ending vertex
     * @return true if the Edge<T> was added, false if from already has this Edge<T>
     * @throws IllegalArgumentException
     *           if from/to are not vertices in the graph
     */
    boolean addEdge(Vertex<T> from, Vertex<T> to) throws IllegalArgumentException;

    /**
     * Insert a directed, weighted Edge<T> into the graph.
     *
     * @param from the Edge<T> starting vertex
     * @param to the Edge<T> ending vertex
     * @param cost the Edge<T> weight/cost
     * @return true if the Edge<T> was added, false if from already has this Edge<T>
     * @throws IllegalArgumentException
     *           if from/to are not vertices in the graph
     */
    boolean addEdge(Vertex<T> from, Vertex<T> to, int cost) throws IllegalArgumentException;

    /**
     * @return true if the graph contains an edge between 2 nodes; false otherwise.
     */
    boolean containsEdge(Vertex<T> from, Vertex<T> to);

    /**
     *   @param vertex node that we want to know if the graph contains.
     *   @return  true if the graph contains the node; false otherwise.
     */
    boolean containsVertex(Vertex<T> vertex);

    /**
     * Get the root vertex
     *
     * @return the root vertex if one is set, null if no vertex has been set as
     *         the root.
     */
    Vertex<T> getRootVertex();

    /**
     * Get the given Vertex.
     *
     * @param n the index [0, size()-1] of the Vertex to access
     * @return the nth Vertex
     */
    Vertex<T> getVertex(int n);


    /**
     * Get the given Vertex.
     *
     * @param label the label of the Vertex to access
     * @return the Vertex containing the data
     */
    Vertex<T> getVertex(String label);

    /**
     * Get the graph vertices
     *
     * @return the graph vertices
     */
    List<Vertex<T>> getVertices();

    /**
     * Get the graph edges
     *
     * @return the graph edges
     */
    List<Edge<T>> getEdges();

    /**
     * Returns whether this vertex is a root vertex.
     *
     * @param vertex the vertex to be inspected.
     * @return true if this is a root, false otherwise.
     */
    boolean isRootVertex(Vertex<T> vertex);

    /**
     * Are there any vertices in the graph?
     *
     * @return true if there are no vertices in the graph; false otherwise.
     */
    boolean isEmpty();

    /**
     * Remove a vertex from the graph
     *
     * @param v the Vertex to remove
     * @return true if the Vertex was removed
     */
    boolean removeVertex(Vertex<T> v);

    /**
     * Remove an Edge<T> from the graph
     *
     * @param from the Edge<T> starting vertex
     * @param to the Edge<T> ending vertex
     * @return true if the Edge<T> exists, false otherwise
     */
    boolean removeEdge(Vertex<T> from, Vertex<T> to);


    /**
     * Get the vertex count.
     *
     * @return the number of vertices in the graph.
     */
    int size();

    @Override public String toString();
}
