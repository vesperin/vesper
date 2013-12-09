package edu.ucsc.refactor;

import edu.ucsc.refactor.internal.HostImpl;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.*;

/**
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public class ContextTest {
    static final Host  HOST  = new HostImpl(){{
        install(new Vesper.DefaultConfiguration());
    }};

    static final Source TEST_CLASS = new Source(
            "Test.java",
            "import java.util.List;\n class Test{\n public void test(int a){\n}\n}\n"
    );


    @Test
    public void testJavaContextCreation(){
        final Context context = new Context(TEST_CLASS);
        assertNull(context.getCompilationUnit()); // it has been compiled, therefore it is null
        assertNotNull(context.getContents());
        assertThat(context.getContents(), equalTo(TEST_CLASS.getContents()));
        assertThat(context.getSource(), equalTo(TEST_CLASS));
    }


    @Test public void testCompiledJavaContextCreation(){
        final Context context = HOST.createContext(TEST_CLASS);
        assertNotNull(context);
        assertNotNull(context.getCompilationUnit());
        assertNotNull(context.getContents());
        assertThat(context.getContents(), equalTo(TEST_CLASS.getContents()));
        assertThat(context.getSource(), equalTo(TEST_CLASS));
    }

    @Test public void testJavaContextWithSourceSelection(){
        final Context       context = HOST.createContext(TEST_CLASS);
        final SourceSelection   entry   = new SourceSelection(TEST_CLASS, 37, 62);

        context.setScope(entry);
        assertNotNull(context.getScope());
    }

}
