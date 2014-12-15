package edu.ucsc.refactor.util.graph;

import com.google.common.collect.Sets;

import java.util.*;

/**
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public class Graph<T> {

    /** Vector<Vertex> of graph vertices */
    private final List<Vertex<T>> vertices;

    /** Vector<Edge> of edges in the graph */
    private final List<Edge<T>> edges;

    /** The vertex identified as the root of the graph */
    private Vertex<T> rootVertex;

    /**
     * Construct a new graph without any vertices or edges
     */
    public Graph() {
        vertices = new ArrayList<Vertex<T>>();
        edges    = new ArrayList<Edge<T>>();
    }

    /**
     * Add a vertex to the graph
     *
     * @param v
     *          the Vertex to add
     * @return true if the vertex was added, false if it was already in the graph.
     */
    public boolean addVertex(Vertex<T> v) {
        boolean added = false;
        if (!containsVertex(v)) {
            added = vertices.add(v);
        }

        return added;
    }


    /**
     * Insert a directed, weighted Edge<T> into the graph.
     *
     * @param from -
     *          the Edge<T> starting vertex
     * @param to -
     *          the Edge<T> ending vertex
     * @return true if the Edge<T> was added, false if from already has this Edge<T>
     * @throws IllegalArgumentException
     *           if from/to are not vertices in the graph
     */
    public boolean addEdge(Vertex<T> from, Vertex<T> to) throws
            IllegalArgumentException {
       return addEdge(from, to, 0);
    }


    /**
     * Insert a directed, weighted Edge<T> into the graph.
     *
     * @param from -
     *          the Edge<T> starting vertex
     * @param to -
     *          the Edge<T> ending vertex
     * @param cost -
     *          the Edge<T> weight/cost
     * @return true if the Edge<T> was added, false if from already has this Edge<T>
     * @throws IllegalArgumentException
     *           if from/to are not vertices in the graph
     */
    public boolean addEdge(Vertex<T> from, Vertex<T> to, int cost) throws IllegalArgumentException {
        if (!containsVertex(from))
            throw new IllegalArgumentException("from is not in graph");
        if (!containsVertex(to))
            throw new IllegalArgumentException("to is not in graph");

        Edge<T> e = new Edge<T>(from, to, cost);
        if (containsEdge(from, to))
            return false;
        else {
            from.addEdge(e);
            to.addEdge(e);
            edges.add(e);
            return true;
        }
    }


    /**
     * Set a root vertex. If root does no exist in the graph it is added.
     *
     * @param root -
     *          the vertex to set as the root and optionally add if it does not
     *          exist in the graph.
     */
    public void addRootVertex(Vertex<T> root) {
        this.rootVertex = root;
        if(!containsVertex(root)) {
          this.addVertex(root);
        }
    }


    /**
     * Clear the mark state of all vertices in the graph by calling clearMark()
     * on all vertices.
     *
     * @see Vertex#clearMark()
     */
    public void clearMark() {
        for (Vertex<T> w : vertices) {
          w.clearMark();
        }
    }

    /**
     * Clear the mark state of all edges in the graph by calling clearMark() on
     * all edges.
     */
    public void clearEdges() {
        for (Edge<T> e : edges) {
          e.clearMark();
        }
    }


    /**
     *   @param vertex node that we want to know if the graph contains.
     *   @return  true if the graph contains the node.
     *            false otherwise.
     */
    public boolean containsVertex(Vertex<T> vertex){
        return vertices.contains(vertex);
    }

    /**
     * @return true if the graph contains an edge the 2 nodes
     *           false otherwise.
     */
    public boolean containsEdge(Vertex<T> from, Vertex<T> to){
        return from.findEdge(to) != null;
    }


    /**
     * Search the vertices for one with name.
     *
     * @param name -
     *          the vertex name
     * @return the first vertex with a matching name, null if no matches are found
     */
    public Vertex<T> findVertexByName(String name) {
        Vertex<T> match = null;
        for (Vertex<T> v : vertices) {
            if (name.equals(v.getName())) {
                match = v;
                break;
            }
        }
        return match;
    }

    /**
     * Search the vertices for one with data.
     *
     * @param data -
     *          the vertex data to match
     * @param compare -
     *          the comparator to perform the match
     * @return the first vertex with a matching data, null if no matches are found
     */
    public Vertex<T> findVertexByData(T data, Comparator<T> compare) {
        Vertex<T> match = null;
        for (Vertex<T> v : vertices) {
            if (compare.compare(data, v.getData()) == 0) {
                match = v;
                break;
            }
        }
        return match;
    }


    /**
     * Get the root vertex
     *
     * @return the root vertex if one is set, null if no vertex has been set as
     *         the root.
     */
    public Vertex<T> getRootVertex() {
        return rootVertex;
    }

    /**
     * Get the given Vertex.
     *
     * @param n
     *          the index [0, size()-1] of the Vertex to access
     * @return the nth Vertex
     */
    public Vertex<T> getVertex(int n) {
        return vertices.get(n);
    }

    /**
     * Get the graph vertices
     *
     * @return the graph vertices
     */
    public List<Vertex<T>> getVertices() {
        return this.vertices;
    }

    /**
     * Get the graph edges
     *
     * @return the graph edges
     */
    public List<Edge<T>> getEdges() {
        return this.edges;
    }

    /**
     * Are there any vertices in the graph
     *
     * @return true if there are no vertices in the graph
     */
    public boolean isEmpty() {
        return vertices.size() == 0;
    }

    /**
     * Returns whether this vertex is a root vertex.
     *
     * @param vertex the vertex to be inspected.
     * @return true if this is a root, false otherwise.
     */
    public boolean isRootVertex(Vertex<T> vertex){
        return getRootVertex().equals(vertex);
    }

    /**
     * Insert a bidirectional Edge<T> in the graph
     *
     * @param from -
     *          the Edge<T> starting vertex
     * @param to -
     *          the Edge<T> ending vertex
     * @param cost -
     *          the Edge<T> weight/cost
     * @return true if edges between both nodes were added, false otherwise
     * @throws IllegalArgumentException
     *           if from/to are not vertices in the graph
     */
    public boolean insertBiEdge(Vertex<T> from, Vertex<T> to, int cost)
            throws IllegalArgumentException {
        return addEdge(from, to, cost) && addEdge(to, from, cost);
    }

    /**
     * Check whether a child vertex is a descendant (immediate or distant) of a
     * parent vertex.
     *
     * @param child The child vertex
     * @param parent The parent vertex
     * @return true if the child is a descendant of the parent; false otherwise.
     */
    public boolean isDescendantOf(Vertex<T> child, Vertex<T> parent){
        final Deque<Vertex<T>> stack = new LinkedList<Vertex<T>>();
        stack.push(child);

        final Set<Vertex<T>> visited = Sets.newLinkedHashSet();

        while(!stack.isEmpty()){
            final Vertex<T> current = stack.pop();
            visited.add(current);

            for( Edge<T> each : current.getIncomingEdges()){
                if(each.getFrom().equals(parent)) return true;
                if(!visited.contains(each.getFrom())){
                    visited.add(each.getFrom());
                }
            }
        }

        return false;
    }

    /**
     * Remove a vertex from the graph
     *
     * @param v
     *          the Vertex to remove
     * @return true if the Vertex was removed
     */
    public boolean removeVertex(Vertex<T> v) {
        if (!containsVertex(v))
            return false;

        vertices.remove(v);
        if (v == rootVertex)
            rootVertex = null;

        // Remove the edges associated with v
        for (int n = 0; n < v.getOutgoingEdgeCount(); n++) {
            Edge<T> e = v.getOutgoingEdge(n);
            v.remove(e);
            Vertex<T> to = e.getTo();
            to.remove(e);
            edges.remove(e);
        }
        for (int n = 0; n < v.getIncomingEdgeCount(); n++) {
            Edge<T> e = v.getIncomingEdge(n);
            v.remove(e);
            Vertex<T> predecessor = e.getFrom();
            predecessor.remove(e);
        }
        return true;
    }

    /**
     * Remove an Edge<T> from the graph
     *
     * @param from -
     *          the Edge<T> starting vertex
     * @param to -
     *          the Edge<T> ending vertex
     * @return true if the Edge<T> exists, false otherwise
     */
    public boolean removeEdge(Vertex<T> from, Vertex<T> to) {
        Edge<T> e = from.findEdge(to);
        if (e == null)
            return false;
        else {
            from.remove(e);
            to.remove(e);
            edges.remove(e);
            return true;
        }
    }

    /**
     * Get the vertex count.
     *
     * @return the number of vertices in the graph.
     */
    public int size() {
        return vertices.size();
    }


    @Override public String toString() {
        StringBuilder tmp = new StringBuilder("Graph[");

        for (Vertex<T> v : vertices) {
          tmp.append(v);
        }

        tmp.append(']');
        return tmp.toString();
    }

}
