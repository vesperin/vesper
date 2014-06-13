package edu.ucsc.refactor;

import edu.ucsc.refactor.util.CommitHistory;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.*;
import static org.junit.Assert.assertNotNull;

/**
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public class NavigableRefactorerTest {
    static final String CONTENT = "import java.util.List; \n" +
            "class Preconditions {\n" +
            "\tstatic void check(\n" +
            "\t\tboolean cond, String message\n" +
            "\t) throws RuntimeException {\n" +
            "\t\tB bbb = new B(); cond = !cond;" +
            "\t\tif(!cond) throw new IllegalArgumentException();\n" +
            "\t}\n" +
            "\tstatic class B{}\n" +
            "}";

    static final String NAME = "Preconditions.java";
    static final Source SRC  = new Source(NAME, CONTENT);

    @Test public void testNavigableRefactorerCreation(){
        final NavigableRefactorer refactorer = NavigableVesper.createNavigableRefactorer(SRC);
        assertThat(refactorer, notNullValue());
    }

    @Test public void testMultipleNavigableRefactorers(){
        final Refactorer first  = NavigableVesper.createNavigableRefactorer(SRC);
        final Refactorer second = NavigableVesper.createNavigableRefactorer(SRC);
        assertNotSame(first, second);
    }



    @Test public void testNavigableRefactorerInternals(){
        final NavigableRefactorer enrichedRefactorer = NavigableVesper.createNavigableRefactorer(SRC);
        assertThat(enrichedRefactorer.getIssues(SRC).isEmpty(), is(false));
        assertThat(enrichedRefactorer.getSources().size(), is(1));

        assertThat(enrichedRefactorer.getSources().isEmpty(), is(false));
        assertThat(enrichedRefactorer.getSources().size(), is(1));

        assertSame(enrichedRefactorer.getSources().get(0), SRC);
        assertThat(enrichedRefactorer.getSources().contains(SRC), is(true));

        assertThat(enrichedRefactorer.hasIssues(SRC), is(true));
    }

    @Test public void testNavigableRefactorerCreateChanges() {
        // for issue and for edit
        final NavigableRefactorer enrichedRefactorer = NavigableVesper.createNavigableRefactorer(SRC);
        final List<Issue> issues = enrichedRefactorer.getIssues(SRC);

        testNavigableRefactorerGivenSomeDetectedIssues(enrichedRefactorer, issues, false);
    }

    @Test public void testNavigableRefactorerCommitHistory(){
        // for issue and for edit
        final NavigableRefactorer enrichedRefactorer = NavigableVesper.createNavigableRefactorer(SRC);
        final List<Issue> issues = enrichedRefactorer.getIssues(SRC);

        testNavigableRefactorerGivenSomeDetectedIssues(enrichedRefactorer, issues, true);

        final CommitHistory history = enrichedRefactorer.getCommitHistory(SRC);
        assertNotNull(history);
        assertThat(history.size(), is(2));
    }


    private void testNavigableRefactorerGivenSomeDetectedIssues(NavigableRefactorer refactorer, List<Issue> issues, boolean applyChanges){
        assertThat(issues.isEmpty(), is(false));

        for(Issue eachIssue : issues){
            final Change fix = refactorer.createChange(
                    ChangeRequest.forIssue(
                            eachIssue,
                            new HashMap<String, Parameter>()
                    )
            );

            assertNotNull(fix);
            assertThat(fix.isValid(), is(true));
            if(applyChanges){
                refactorer.apply(fix);
                break;
            }
        }


        final Change amendment = refactorer.createChange(
                ChangeRequest.reformatSource(SRC)
        );

        assertNotNull(amendment);
        assertThat(amendment.isValid(), is(true));
        if(applyChanges){
            refactorer.apply(amendment);
        }
    }

}
