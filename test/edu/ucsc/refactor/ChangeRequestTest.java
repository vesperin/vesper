package edu.ucsc.refactor;

import edu.ucsc.refactor.internal.detectors.UnusedMethods;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThat;


/**
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public class ChangeRequestTest {

    @Test public void testChangeRequestForIssueCreation(){
        final Issue cause   = new Issue(new UnusedMethods());
        final ChangeRequest request = ChangeRequest.forIssue(
                cause,
                new Source("Test.java", "class Test {}")
        );

        assertNotNull(request);
        assertSame(request.getCauseOfChange(), cause);
        assertThat(request.getParameters().isEmpty(), is(true));
        assertNotNull(request.getSelection());
    }


    @Test public void testChangeRequestForEditCreation(){
        final Source code = new Source("Test.java", "class Test {}");
        final SingleEdit cause = SingleEdit.reformatCode(new SourceSelection(code, 0, code.getLength()));
        final ChangeRequest request = ChangeRequest.forEdit(cause);

        assertNotNull(request);
        assertSame(request.getCauseOfChange(), cause);
        assertThat(request.getParameters().isEmpty(), is(true));
        assertNotNull(request.getSelection());
    }
}
