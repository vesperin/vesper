package edu.ucsc.refactor.util.graph;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public class Traversals {

    /** Color used to mark unvisited nodes */
    public static final int VISIT_COLOR_WHITE = 1;

    /** Color used to mark nodes as they are first visited in DFS order */
    public static final int VISIT_COLOR_GREY = 2;

    /** Color used to mark nodes after descendants are completely visited */
    public static final int VISIT_COLOR_BLACK = 3;

    /** Private constructor **/
    private Traversals(){}


    /**
     * Perform a breadth first search of this graph, starting at v.
     *
     * @param graph - the graph to be searched.
     * @param v - the search starting point
     * @param visitor - the visitor whose visit method is called prior to visiting a vertex.
     */
    public <T> void breadthFirstSearch(Graph<T> graph, Vertex<T> v, final Visitor<T> visitor) {
        VisitorThrow<T, RuntimeException> wrapper = new VisitorThrow<T, RuntimeException>() {
            public void visit(Graph<T> g, Vertex<T> v) throws RuntimeException {
                if (visitor != null)
                    visitor.visit(g, v);
            }
        };

        breadthFirstSearch(graph, v, wrapper);
    }

    /**
     * Perform a breadth first search of this graph, starting at v. The visit may
     * be cut short if visitor throws an exception during a visit callback.
     *
     * @param <E> the exception type
     * @param v - the search starting point
     * @param visitor - the visitor whose visit method is called prior to visiting a vertex.
     * @throws E if visitor.visit throws an exception
     */
    public static <E extends Exception, T> void breadthFirstSearch(Graph<T> graph,
                  Vertex<T> v, VisitorThrow<T, E> visitor) throws E {

        LinkedList<Vertex<T>> q = new LinkedList<Vertex<T>>();

        q.add(v);
        if (visitor != null)
            visitor.visit(graph, v);
        v.visit();
        while (!q.isEmpty()) {
            v = q.removeFirst();
            for (int i = 0; i < v.getOutgoingEdgeCount(); i++) {
                Edge<T> e = v.getOutgoingEdge(i);
                Vertex<T> to = e.getTo();
                if (!to.visited()) {
                    q.add(to);
                    if (visitor != null)
                        visitor.visit(graph, to);
                    to.visit();
                }
            }
        }
    }

    /**
     * Perform a depth first search using recursion.
     *
     * @param graph - the graph to be searched.
     * @param v - the Vertex to start the search from
     * @param visitor - the visitor to inform prior to
     * @see Visitor#visit(Graph, Vertex)
     */
    public static <T extends Exception> void depthFirstSearch(Graph<T> graph,
                  Vertex<T> v, final Visitor<T> visitor) {

        VisitorThrow<T, RuntimeException> wrapper = new VisitorThrow<T, RuntimeException>() {
            public void visit(Graph<T> g, Vertex<T> v) throws RuntimeException {
                if (visitor != null)
                    visitor.visit(g, v);
            }
        };

        depthFirstSearch(graph, v, wrapper);
    }

    /**
     * Perform a depth first search using recursion. The search may be cut short
     * if the visitor throws an exception.
     *
     * @param <E> the exception type
     * @param v - the Vertex to start the search from
     * @param visitor - the visitor to inform prior to
     * @see Visitor#visit(Graph, Vertex)
     * @throws E if visitor.visit throws an exception
     */
    public static <E extends Exception, T> void depthFirstSearch(Graph<T> graph,
                  Vertex<T> v, VisitorThrow<T, E> visitor) throws E {

        if (visitor != null)
            visitor.visit(graph, v);
        v.visit();
        for (int i = 0; i < v.getOutgoingEdgeCount(); i++) {
            Edge<T> e = v.getOutgoingEdge(i);
            if (!e.getTo().visited()) {
                depthFirstSearch(graph, e.getTo(), visitor);
            }
        }
    }

    /**
     * Returns the depth of a vertex in a graph.
     *
     * @param depth the incrementing depth
     * @param vertex the vertex to be searched
     * @param nodesAtCurrentDepth the vertices at the current depth
     * @param <T> type of data stored in a vertex
     * @return the depth of the searched vertex; -1 if not found.
     */
    public static <T> int depth(int depth, Vertex<T> vertex, List<Vertex<T>> nodesAtCurrentDepth){
        List<Vertex<T>> nodesAtNextLevel = new ArrayList<Vertex<T>>();
        for( Vertex<T> each : nodesAtCurrentDepth){
          if(each.equals(vertex)){
              return depth;
          }

          if(each.getOutgoingEdgeCount() > 0){
             for (Edge<T> child : each.getOutgoingEdges()){
                 nodesAtNextLevel.add(child.getTo());
             }
          }
        }

        if(!nodesAtNextLevel.isEmpty()){
          depth(depth + 1, vertex, nodesAtNextLevel);
        }

        return -1; // nothing was found
    }



    /**
     * Search the graph for cycles. In order to detect cycles, we use a modified
     * depth first search called a colored DFS. All nodes are initially marked
     * white. When a node is encountered, it is marked grey, and when its
     * descendants are completely visited, it is marked black. If a grey node is
     * ever encountered, then there is a cycle.
     *
     * @return the edges that form cycles in the graph. The array will be empty if
     *         there are no cycles.
     */
    public static <T> Edge<T>[] findCycles(Graph<T> graph) {
        ArrayList<Edge<T>> cycleEdges = new ArrayList<Edge<T>>();
        // Mark all vertices as white
        for (int n = 0; n < graph.getVertices().size(); n++) {
            Vertex<T> v = graph.getVertex(n);
            v.setMarkState(VISIT_COLOR_WHITE);
        }

        for (int n = 0; n < graph.getVertices().size(); n++) {
            Vertex<T> v = graph.getVertex(n);
            visit(v, cycleEdges);
        }

        @SuppressWarnings("unchecked") Edge<T>[] cycles = new Edge[cycleEdges.size()];
        cycleEdges.toArray(cycles);
        return cycles;
    }


    private static <T> void visit(Vertex<T> v, ArrayList<Edge<T>> cycleEdges) {
        v.setMarkState(VISIT_COLOR_GREY);
        int count = v.getOutgoingEdgeCount();
        for (int n = 0; n < count; n++) {
            Edge<T> e = v.getOutgoingEdge(n);
            Vertex<T> u = e.getTo();
            if (u.getMarkState() == VISIT_COLOR_GREY) {
                // A cycle Edge<T>
                cycleEdges.add(e);
            } else if (u.getMarkState() == VISIT_COLOR_WHITE) {
                visit(u, cycleEdges);
            }
        }

        v.setMarkState(VISIT_COLOR_BLACK);
    }

}
