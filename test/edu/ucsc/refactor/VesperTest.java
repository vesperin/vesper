package edu.ucsc.refactor;

import edu.ucsc.refactor.util.Commit;
import edu.ucsc.refactor.util.Recommender;
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
}
