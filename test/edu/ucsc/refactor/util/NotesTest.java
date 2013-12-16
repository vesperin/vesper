package edu.ucsc.refactor.util;

import edu.ucsc.refactor.Note;
import org.junit.Test;

import java.util.NoSuchElementException;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

/**
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public class NotesTest {
    @Test public void testEmptyNotes(){
        final Notes notes = new Notes();
        assertThat(notes.isEmpty(), is(true));
    }

    @Test(expected = NoSuchElementException.class)
    public void testNoGeneralNote(){
        final Notes notes = new Notes();
        final Note  note  = notes.first();
        fail("if we got here because note=" + note + " is not null.");
    }


    @Test public void testNotesMembership(){
        final Note a = new Note("Hello World!");
        final Notes notes = new Notes(a);
        assertThat(notes.contains(a), is(true));
    }

    @Test public void testUnionOfNotes(){
        final Note  a   = new Note("Hello World!");
        final Notes aa  = new Notes(a);
        final Note  b   = new Note("Bye World!");
        final Notes bb  = new Notes(b);

        final Notes c   = aa.union(bb);

        assertThat(c.size(), is(2));
        assertThat(c.contains(a), is(true));
        assertThat(c.contains(b), is(true));
    }

    @Test public void testAddRemoveNotes(){
        final Note  a       = new Note("Hello World!");
        final Notes notes   = new Notes();
        notes.add(a);
        assertThat(notes.contains(a), is(true));
        notes.delete(a);
        assertThat(notes.contains(a), is(false));
    }

    @Test public void testEqualityOfNotes(){
        final Note  a       = new Note("Hello World!");
        final Note  b       = new Note("Hello World!");
        final Notes notes   = new Notes();
        notes.add(a);
        notes.add(b);

        assertThat(notes.size(), is(1));
        assertThat(notes.contains(a), is(true));
        assertThat(notes.contains(b), is(true));
    }


    @Test public void testDifferenceOfNotes(){
        final Note  a       = new Note("Hello World!");
        final Note  b       = new Note("Hello All!");
        final Notes notes   = new Notes();
        notes.add(a);
        notes.add(b);

        final Note  c       = new Note("Hello World!");
        final Notes that    = new Notes();
        that.add(c);

        final Notes diff    = notes.difference(that);
        assertThat(diff.size(), is(1));
        assertThat(diff.contains(b), is(true));
    }


}
