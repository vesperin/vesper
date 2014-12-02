package edu.ucsc.refactor.internal.util;

import edu.ucsc.refactor.AbstractConfiguration;
import edu.ucsc.refactor.Context;
import edu.ucsc.refactor.Host;
import edu.ucsc.refactor.Source;
import edu.ucsc.refactor.internal.EclipseJavaParser;
import edu.ucsc.refactor.internal.EclipseJavaSnippetParser;
import edu.ucsc.refactor.internal.HostImpl;
import edu.ucsc.refactor.internal.visitors.MethodDeclarationVisitor;
import edu.ucsc.refactor.spi.JavaParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

/**
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public class ASTUtilTest {
    static final String NAME    = "Name.java";
    static final String CONTENT = "import java.util.List; \n"
            + "class Name {\n"
            + "\tvoid boom(String msg){}\n"
            + "\t@Test void test(String msg){ return; }\n"
            + "}";

    static Context context = null;
    static MethodDeclarationVisitor visitor;

    @Before public void setUp() throws Exception {
        final Host host = new HostImpl();
        host.install(new AbstractConfiguration() {
            @Override
            protected void configure() {
                addJavaParser(new EclipseJavaSnippetParser());
            }
        });


        final JavaParser parser  = new EclipseJavaParser();
        context = host.createContext(new Source(NAME, CONTENT));

        visitor = new MethodDeclarationVisitor();
        context.accept(visitor);

        assert parser.parseJava(context)  != null;
    }

    @Test public void testFindingParent(){
        final MethodDeclaration first = visitor.getMethodDeclarations().get(0);
        final CompilationUnit   u     = AstUtil.parent(CompilationUnit.class, first);
        assertNotNull(u);
    }

    @Test public void testMethodHasAnnotations(){
        final MethodDeclaration second = visitor.getMethodDeclarations().get(1);
        assertThat(AstUtil.isAnnotated(second), is(true));
    }

    @Test public void testIsMainMethod(){
        final MethodDeclaration first  = visitor.getMethodDeclarations().get(0);
        final MethodDeclaration second = visitor.getMethodDeclarations().get(1);

        assertThat(AstUtil.isMainMethod(first), is(false));
        assertThat(AstUtil.isMainMethod(second), is(false));

    }

    @After public void tearDown() throws Exception {
        context = null;
        visitor = null;
    }

}
