package edu.ucsc.refactor;

import edu.ucsc.refactor.util.StringUtil;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

/**
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public class SourceTest {
    static final String NAME            = "Source.java";
    static final String CONTENT         = "public class Source {}";
    static final String DESC            = "Java: Source";
    static final String VERBOSE_DESC    = "Java: Source code abstraction";

    @Test public void testSourceCreationNoDescription(){
        final Source a = new Source(NAME, CONTENT);
        assertNotNull(a);
        assertThat(StringUtil.extractName(a.getName()), equalTo(StringUtil.extractName(NAME)));
        assertThat(a.getContents(), equalTo(CONTENT));
        assertNotNull(a.toDocument());
        assertEquals(a.getDescription(), DESC);
        assertThat(a.getNotes().isEmpty(), is(true));
    }


    @Test public void testSourceCreationWithDescription(){
        final Source b = new Source(NAME, CONTENT, VERBOSE_DESC);
        assertThat(StringUtil.extractName(b.getName()), equalTo(StringUtil.extractName(NAME)));
        assertThat(b.getContents(), equalTo(CONTENT));
        assertNotNull(b.toDocument());
        assertThat("Source{name=" + StringUtil.extractName(b.getName()) + "}",
                equalTo(b.toString()));
        assertEquals(b.getDescription(), VERBOSE_DESC);
        assertThat(b.getNotes().isEmpty(), is(true));
    }

    @Test public void testSourceComments(){
        final Source    code        = new Source(NAME, CONTENT, VERBOSE_DESC);

        final Note[]    notes       = {
                new Note("a"), new Note("b"), new Note("c")
        };


        for(Note eachNote : notes){
            code.addNote(eachNote);
        }

        assertThat(code.getNotes().isEmpty(), is(false));
        assertThat(code.getNotes().size(), is(3));
        assertNotNull(code.getGeneralNote());
        assertThat(code.getGeneralNote().getContent(), is("a"));

        for(Note eachNote : notes){
            code.removeNote(eachNote);
        }

        assertThat(code.getNotes().isEmpty(), is(true));
        assertThat(code.getNotes().size(), is(0));
    }

    @Test public void testGivingSomeIdToSource(){
        final Source a = new Source(NAME, CONTENT);
        assertNull(a.getId());

        a.setId("123456789");
        assertNotNull(a.getId());
    }
}
