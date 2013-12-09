package edu.ucsc.refactor;

import edu.ucsc.refactor.util.StringUtil;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

/**
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public class SourceTest {
    static final String NAME    = "Source.java";
    static final String CONTENT = "public class Source {}";

    @Test public void testSourceCreationUnknownUrl(){
        final Source a = new Source(NAME, CONTENT);
        assertNotNull(a);
        assertThat(a.getName(), equalTo(StringUtil.extractName(NAME)));
        assertThat(a.getContents(), equalTo(CONTENT));
        assertNotNull(a.toDocument());

    }


    @Test public void testSourceCreationKnownUrl(){
        final Source b = new Source(NAME, CONTENT);
        assertThat(b.getName(), equalTo(StringUtil.extractName(NAME)));
        assertThat(b.getContents(), equalTo(CONTENT));
        assertNotNull(b.toDocument());
        assertThat(b.getName(), equalTo(b.toString()));
    }
}
