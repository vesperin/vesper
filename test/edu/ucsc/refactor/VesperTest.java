package edu.ucsc.refactor;

import edu.ucsc.refactor.internal.InternalUtil;
import edu.ucsc.refactor.util.Commit;
import edu.ucsc.refactor.util.Recommender;
import edu.ucsc.refactor.util.graph.DirectedAcyclicGraph;
import edu.ucsc.refactor.util.graph.DirectedGraph;
import edu.ucsc.refactor.util.graph.Edge;
import edu.ucsc.refactor.util.graph.GraphUtils;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.junit.Test;

import java.util.List;
import java.util.Set;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

/**
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public class VesperTest {
    static final String UPDATED = "class Name {\n"
            + "\tvoid boom(String msg){}\n"
            + "}";

    static final String CONTENT = "import java.util.List; \n"
            + "class Name {\n"
            + "\tvoid boom(String msg){}\n"
            + "}";

    static final Source CODE = new Source("Name.java", CONTENT);

    @Test public void testCommitBrandNewChange() throws Exception {
        final Refactorer refactorer = Vesper.createRefactorer();

        final Introspector introspector = refactorer.getIntrospector(CODE);
        final Set<Issue> issues = introspector.detectIssues(CODE);
        assertThat(!issues.isEmpty(), is(true));

        final List<Change> suggestedChanges = Recommender.recommendChanges(refactorer, CODE, issues);
        assertThat(suggestedChanges.isEmpty(), is(false));

        final Change first = suggestedChanges.get(0);

        final Commit applied = refactorer.apply(first);
        assertNotNull(applied);

    }


    @Test public void testDirectedAcyclicGraphCreation() throws Exception {
        final Source            src     = InternalUtil.createQuickSortSource();
        final Context           context = CodeIntrospector.makeContext(src);
        final MethodDeclaration method  = CodeIntrospector.getMethod("main", context);

        final CodeIntrospector.BlockVisitor visitor = new CodeIntrospector.BlockVisitor();
        method.accept(visitor);

        final DirectedGraph<CodeIntrospector.Item> graph = visitor.graph();

        Edge<CodeIntrospector.Item>[] edges = GraphUtils.findCycles(graph);
        assertThat(edges.length, is(0));

    }
}
