package edu.ucsc.refactor;

import edu.ucsc.refactor.internal.ClassUnit;
import edu.ucsc.refactor.internal.FieldUnit;
import edu.ucsc.refactor.internal.MethodUnit;
import edu.ucsc.refactor.internal.ParameterUnit;
import edu.ucsc.refactor.spi.UnitLocator;
import edu.ucsc.refactor.util.CommitHistory;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.*;

/**
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public class RefactorersTest {
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

    @Test public void testRefactorerCreation(){
        final Refactorer refactorer = Vesper.createRefactorer();
        assertThat(refactorer, notNullValue());
    }

    @Test public void testCheckpointedRefactorerCreation(){
        final CheckpointedRefactorer refactorer = Vesper.createCheckpointedRefactorer(SRC);
        assertThat(refactorer, notNullValue());
    }

    @Test public void testMultipleRefactorers(){
        final Refactorer first  = Vesper.createRefactorer();
        final Refactorer second = Vesper.createRefactorer();
        assertNotSame(first, second);
    }

    @Test public void testMultipleCheckpointedRefactorers(){
        final Refactorer first  = Vesper.createCheckpointedRefactorer(SRC);
        final Refactorer second = Vesper.createCheckpointedRefactorer(SRC);
        assertNotSame(first, second);
    }

    @Test public void testRefactorerIssueDetection(){
        final Refactorer refactorer = Vesper.createRefactorer();

        final Set<Issue> issues = refactorer.detectIssues(SRC);
        assertThat(issues.isEmpty(), is(false));
    }


    @Test public void testCheckpointedRefactorerInternals(){
        final CheckpointedRefactorer enrichedRefactorer = Vesper.createCheckpointedRefactorer(SRC);
        assertThat(enrichedRefactorer.getIssues(SRC).isEmpty(), is(false));
        assertThat(enrichedRefactorer.getTrackedSources().size(), is(1));

        assertThat(enrichedRefactorer.getTrackedSources().isEmpty(), is(false));
        assertThat(enrichedRefactorer.getTrackedSources().size(), is(1));

        assertSame(enrichedRefactorer.getTrackedSources().get(0), SRC);
        assertThat(enrichedRefactorer.getTrackedSources().contains(SRC), is(true));

        assertThat(enrichedRefactorer.hasIssues(SRC), is(true));
    }

    @Test public void testRefactorerCreateChanges() {
        // for issue and for edit
        final Refactorer refactorer = Vesper.createRefactorer();
        final Set<Issue> issues     = refactorer.detectIssues(SRC);

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
        }


        final Change amendment = refactorer.createChange(
                ChangeRequest.reformatSource(SRC)
        );

        assertNotNull(amendment);
        assertThat(amendment.isValid(), is(true));

    }

    @Test public void testCheckpointedRefactorerCreateChanges() {
        // for issue and for edit
        final CheckpointedRefactorer enrichedRefactorer = Vesper.createCheckpointedRefactorer(SRC);
        final List<Issue> issues = enrichedRefactorer.getIssues(SRC);

        testCheckpointedRefactorerGivenSomeDetectedIssues(enrichedRefactorer, issues, false);
    }

    @Test public void testCheckpointedRefactorerCommitHistory(){
        // for issue and for edit
        final CheckpointedRefactorer enrichedRefactorer = Vesper.createCheckpointedRefactorer(SRC);
        final List<Issue> issues = enrichedRefactorer.getIssues(SRC);

        testCheckpointedRefactorerGivenSomeDetectedIssues(enrichedRefactorer, issues, true);

        final CommitHistory history = enrichedRefactorer.getCommitHistory(SRC);
        assertNotNull(history);
        assertThat(history.size(), is(2));
    }


    private void testCheckpointedRefactorerGivenSomeDetectedIssues(CheckpointedRefactorer refactorer, List<Issue> issues, boolean applyChanges){
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

    @Test public void testRefactorerRecommendChange(){
        // for any issues found on the SRC
        final Refactorer refactorer = Vesper.createRefactorer();
        final List<Change> changes = refactorer.recommendChanges(SRC, refactorer.detectIssues(SRC));

        assertThat(changes.isEmpty(), is(false));
        assertThat(changes.size(), is(2));
    }

    @Test public void testRefactorerUnitLocator() {
        final Refactorer    refactorer  = Vesper.createRefactorer();
        final UnitLocator   locator     = refactorer.getLocator(SRC);

        final List<NamedLocation> params = locator.locate(new ParameterUnit("message"));
        assertThat(params.isEmpty(), is(false));

        final List<NamedLocation> methods = locator.locate(new MethodUnit("check"));
        assertThat(methods.isEmpty(), is(false));

        final List<NamedLocation> classes = locator.locate(new ClassUnit("Preconditions"));
        assertThat(classes.isEmpty(), is(false));

        final List<NamedLocation> fields = locator.locate(new FieldUnit("something"));
        assertThat(fields.isEmpty(), is(true));

    }


}
