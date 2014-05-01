package edu.ucsc.refactor.internal.changers;

import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import edu.ucsc.refactor.*;
import edu.ucsc.refactor.internal.*;
import edu.ucsc.refactor.internal.detectors.*;
import edu.ucsc.refactor.internal.util.AstUtil;
import edu.ucsc.refactor.internal.util.Edits;
import edu.ucsc.refactor.spi.JavaParser;
import edu.ucsc.refactor.spi.SourceChanger;
import edu.ucsc.refactor.util.Commit;
import edu.ucsc.refactor.util.Locations;
import edu.ucsc.refactor.util.Parameters;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.Set;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

/**
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public class ChangersTest {

    private JavaParser parser;

    @Before public void setUp() throws Exception {
        parser  = new EclipseJavaParser();
    }

    @Test public void testChangerForUnusedTypeDeclaration() throws Exception {

        final Context context = new Context(
                InternalUtil.createSourceWithOneUnusedStaticNestedClass()
        );

        parser.parseJava(context);

        final UnusedTypes unusedClass = new UnusedTypes();
        final Set<Issue> issues      = unusedClass.detectIssues(context);

        assertThat(issues.size(), is(1));

        final RemoveUnusedTypes remove = new RemoveUnusedTypes();
        for(Issue each : issues){
            Change change = remove.createChange(each, Maps.<String, Parameter>newHashMap());
            assertNotNull(change);
        }

    }


    @Test public void testChangerForUsedTypeDeclaration() throws Exception {

        final Context context = new Context(
                InternalUtil.createSourceWithOneUsedStaticNestedClass()
        );

        parser.parseJava(context);


        final ProgramUnitLocator    locator    = new ProgramUnitLocator(context);
        final List<NamedLocation>   locations  = locator.locate(new ClassUnit("B"));

        assertThat(locations.isEmpty(), is(false));

        for(Location each : locations){
            ProgramUnitLocation     pul         = (ProgramUnitLocation) each;
            final TypeDeclaration   declaration = (TypeDeclaration) pul.getNode();

            final Location          loc         = Locations.locate(declaration);
            final RemoveUnusedTypes remove      = new RemoveUnusedTypes();
            final SingleEdit        edit        = SingleEdit.deleteClass(new SourceSelection(loc));
            edit.addNode(declaration);
            final Change            change      = remove.createChange(edit, Maps.<String, Parameter>newHashMap());

            assertThat(change.isValid(), is(false));
        }
    }


    @Test public void testTheStackoverflowWebExample() {
        final Context context = new Context(
                InternalUtil.createSourceUsingStackoverflowExample()
        );

        parser.parseJava(context);

        final SourceSelection     selection = new SourceSelection(
                SourceLocation.createLocation(
                        context.getSource(),
                        context.getSource().getContents(),
                        394, 403
                )
        );

        final ProgramUnitLocator    locator    = new ProgramUnitLocator(context);
        final List<NamedLocation>   locations  = locator.locate(new SelectedUnit(selection));


        final SingleEdit       edit   = SingleEdit.renameSelectedMember(selection);
        assertThat(locations.isEmpty(), is(false));

        for(NamedLocation eachLocation : locations){
            final ProgramUnitLocation target  = (ProgramUnitLocation)eachLocation;
            edit.addNode(target.getNode());
        }


        final RenameMethod  rename      = new RenameMethod();
        final SingleEdit    resolved    = Edits.resolve(edit);

        checkChangeCreation(rename, resolved);
    }


    @Test public void testRenameFieldBySelectingOneOfItsUsages() {
        final Context context = new Context(
                InternalUtil.createSourceWithSomeUsedFieldAndLocalVariable()
        );

        parser.parseJava(context);

        final List<Location>    spots     = Locations.locateWord(context.getSource(), "a");
        for(Location spot : spots){
            final SourceSelection   selection = new SourceSelection(spot);


            final ProgramUnitLocator    locator    = new ProgramUnitLocator(context);
            final List<NamedLocation>   locations  = locator.locate(new SelectedUnit(selection));


            final SingleEdit       edit   = SingleEdit.renameSelectedMember(selection);
            assertThat(locations.isEmpty(), is(false));

            for(NamedLocation eachLocation : locations){
                final ProgramUnitLocation target  = (ProgramUnitLocation)eachLocation;
                edit.addNode(target.getNode());
            }


            final RenameField  rename      = new RenameField();
            final SingleEdit   resolved    = Edits.resolve(edit);

            checkChangeCreation(rename, resolved);
        }
    }


    @Test public void testRenameClassBySelectingOneOfItsUsages() {
        final Context context = new Context(
                InternalUtil.createSourceWithSomeConstructorInvocation()
        );

        parser.parseJava(context);

        final List<Location>    spots     = Locations.locateWord(context.getSource(), "Name");

        for(Location spot : spots){
            final SourceSelection   selection = new SourceSelection(spot);


            final ProgramUnitLocator    locator    = new ProgramUnitLocator(context);
            final List<NamedLocation>   locations  = locator.locate(new SelectedUnit(selection));


            final SingleEdit       edit   = SingleEdit.renameSelectedMember(selection);
            assertThat(locations.isEmpty(), is(false));

            for(NamedLocation eachLocation : locations){
                final ProgramUnitLocation target  = (ProgramUnitLocation)eachLocation;
                edit.addNode(target.getNode());
            }


            final RenameClassOrInterface    rename      = new RenameClassOrInterface();
            final SingleEdit                resolved    = Edits.resolve(edit);

            checkChangeCreation(rename, resolved);
        }

    }


    @Test public void testRenameLocalVariableBySelectingOneOfItsUsages() {
        final Context context = new Context(
                InternalUtil.createSourceWithSomeUsedFieldAndLocalVariable()
        );

        parser.parseJava(context);

        final List<Location>    spots     = Locations.locateWord(context.getSource(), "b");
        for(Location spot : spots){
            final SourceSelection   selection = new SourceSelection(spot);


            final ProgramUnitLocator    locator    = new ProgramUnitLocator(context);
            final List<NamedLocation>   locations  = locator.locate(new SelectedUnit(selection));


            final SingleEdit       edit   = SingleEdit.renameSelectedMember(selection);
            assertThat(locations.isEmpty(), is(false));

            for(NamedLocation eachLocation : locations){
                final ProgramUnitLocation target  = (ProgramUnitLocation)eachLocation;
                edit.addNode(target.getNode());
            }


            final RenameLocalVariable   rename      = new RenameLocalVariable();
            final SingleEdit            resolved    = Edits.resolve(edit);

            checkChangeCreation(rename, resolved);
        }
    }


    @Test public void testRenameMethodParameterBySelectingOneOfItsUsages() {
        final Context context = new Context(
                InternalUtil.createSourceWithSomeUsedFieldAndLocalVariable()
        );

        parser.parseJava(context);

        final List<Location>    spots     = Locations.locateWord(context.getSource(), "msg");

        for(Location spot : spots){
            final SourceSelection   selection = new SourceSelection(spot);


            final ProgramUnitLocator    locator    = new ProgramUnitLocator(context);
            final List<NamedLocation>   locations  = locator.locate(new SelectedUnit(selection));


            final SingleEdit       edit   = SingleEdit.renameSelectedMember(selection);
            assertThat(locations.isEmpty(), is(false));

            for(NamedLocation eachLocation : locations){
                final ProgramUnitLocation target  = (ProgramUnitLocation)eachLocation;
                edit.addNode(target.getNode());
            }


            final RenameParam  rename     = new RenameParam();
            final SingleEdit   resolved   = Edits.resolve(edit);

            checkChangeCreation(rename, resolved);
        }


    }

    @Test public void testChangerForOptimizeImports() throws Exception {
        final Source  code    = InternalUtil.createGeneralSource();
        final Context context = new Context(code);

        parser.parseJava(context);

        final RemoveUnusedImports remove = new RemoveUnusedImports();
        final SingleEdit          edit   = SingleEdit.optimizeImports(code);
        edit.addNode(context.getCompilationUnit());
        final Change              change = remove.createChange(edit, Maps.<String, Parameter>newHashMap());
        assertThat(change.isValid(), is(true));
    }


    @Test public void testRemoveDetectedUnusedField(){
        final Context context = new Context(
                InternalUtil.createSourceWithUnusedField()
        );

        parser.parseJava(context);

        final UnusedFields unusedFields = new UnusedFields();
        final Set<Issue>    issues      = unusedFields.detectIssues(context);

        final RemoveUnusedFields remove = new RemoveUnusedFields();

        for(Issue each : issues){
            Change change = remove.createChange(each, Maps.<String, Parameter>newHashMap());
            assertNotNull(change);
            assertThat(change.isValid(), is(true));
        }
    }


    @Test public void testRemoveWholeMethodSelection(){
        final Source src = InternalUtil.createScratchedSourceWithOneMethod();

        final Context context = new Context(src);
        parser.parseJava(context);

        final ProgramUnitLocator locator   = new ProgramUnitLocator(context);
        final SourceSelection    selection = new SourceSelection(context.getSource(), 30, 358);
        final List<NamedLocation>     locations = locator.locate(new SelectedUnit(selection));

        final SingleEdit       edit   = SingleEdit.deleteRegion(selection);
        assertThat(locations.isEmpty(), is(false));

        for(NamedLocation eachLocation : locations){
            final ProgramUnitLocation target  = (ProgramUnitLocation)eachLocation;
            edit.addNode(target.getNode());
        }


        final RemoveUnusedMethods   remove      = new RemoveUnusedMethods();
        final SingleEdit            resolved    = Edits.resolve(edit);

        checkChangeCreation(remove, resolved);
    }


    @Test (expected = RuntimeException.class)
    public void testRemoveWholeMethodSelectionFromBrokenSource() throws Exception {
        final Source src = InternalUtil.createBrokenSourceWithOneMethod();

        final Context context = new Context(src);
        parser.parseJava(context);
    }


    @Test public void testTryRemovingUsedField(){
        final Context context = new Context(
                InternalUtil.createSourceWithUsedField()
        );

        parser.parseJava(context);

        final ProgramUnitLocator locator   = new ProgramUnitLocator(context);
        final List<NamedLocation>     locations = locator.locate(new FieldUnit("a"));

        final ProgramUnitLocation target      = (ProgramUnitLocation)locations.get(0);
        final FieldDeclaration    declaration = (FieldDeclaration)target.getNode();

        final Location           loc    = Locations.locate(declaration);
        final RemoveUnusedFields remove = new RemoveUnusedFields();

        final SingleEdit        edit        = SingleEdit.deleteField(new SourceSelection(loc));
        edit.addNode(declaration);
        final Change            change      = remove.createChange(edit, Maps.<String, Parameter>newHashMap());
        assertThat(change.isValid(), is(false));
    }

    @Test public void testRemoveSelectedField(){
        final Source  code    = InternalUtil.createSourceWithUnusedField();
        final Context context = new Context(code);

        parser.parseJava(context);

        final ProgramUnitLocator  locator   = new ProgramUnitLocator(context);
        final SourceSelection     selection = new SourceSelection(SourceLocation.createLocation(code, code.getContents(), 18, 19));
        final List<NamedLocation> locations = locator.locate(new SelectedUnit(selection));


        final SingleEdit       edit   = SingleEdit.deleteRegion(selection);
        assertThat(locations.isEmpty(), is(false));

        for(NamedLocation eachLocation : locations){
            final ProgramUnitLocation target  = (ProgramUnitLocation)eachLocation;
            edit.addNode(target.getNode());
        }


        final RemoveUnusedFields    remove      = new RemoveUnusedFields();
        final SingleEdit            resolved    = Edits.resolve(edit);

        checkChangeCreation(remove, resolved);
    }


    @Test public void testTryRemovingSelectedBlock(){
        final Context context = new Context(
                InternalUtil.createBrokenBubbleSortSource()
        );

        parser.parseJava(context);

        final ProgramUnitLocator locator   = new ProgramUnitLocator(context);
        final SourceSelection    select    = new SourceSelection(context.getSource(), 363, 440);
        final List<NamedLocation>     locations = locator.locate(new SelectedUnit(select));

        final SingleEdit       edit   = SingleEdit.deleteRegion(select);
        assertThat(locations.isEmpty(), is(false));

        for(NamedLocation eachLocation : locations){
            final ProgramUnitLocation target  = (ProgramUnitLocation)eachLocation;
            edit.addNode(target.getNode());
        }


        final RemoveCodeRegion remover  = new RemoveCodeRegion();
        final SingleEdit       resolved = Edits.resolve(edit);

        checkChangeCreation(remover, resolved);

    }


    @Test public void testTryRemovingArrayStatement(){
        final Context context = new Context(
                InternalUtil.createBrokenBubbleSortSource2()
        );

        parser.parseJava(context);

        final ProgramUnitLocator locator   = new ProgramUnitLocator(context);
        final SourceSelection    select    = new SourceSelection(context.getSource(), 68, 117);
        final List<NamedLocation>     locations = locator.locate(new SelectedUnit(select));

        final SingleEdit       edit   = SingleEdit.deleteRegion(select);
        assertThat(locations.isEmpty(), is(false));

        for(NamedLocation eachLocation : locations){
            final ProgramUnitLocation target  = (ProgramUnitLocation)eachLocation;
            edit.addNode(target.getNode());
        }


        final RemoveUnusedLocalVariable remover  = new RemoveUnusedLocalVariable();
        final SingleEdit                resolved = Edits.resolve(edit);

        checkChangeCreation(remover, resolved);

    }


    @Test public void testTryRemovingArrayStatement2(){
        final Context context = new Context(
                InternalUtil.createBrokenBubbleSortSource3()
        );

        parser.parseJava(context);

        final SourceSelection    select    = new SourceSelection(context.getSource(), 50, 99);

        final Refactorer refactorer = Vesper.createRefactorer();

        final Change change = refactorer.createChange(ChangeRequest.deleteRegion(select));
        final Commit commit = refactorer.apply(change);

        assertThat(commit.isValidCommit(), is(true));
    }


    @Test public void testTryRemovingParameterDeclaration(){
        final Context context = new Context(
                InternalUtil.createBrokenBubbleSortSource2()
        );

        parser.parseJava(context);

        final ProgramUnitLocator locator   = new ProgramUnitLocator(context);
        final SourceSelection    select    = new SourceSelection(context.getSource(), 44, 62);
        final List<NamedLocation>     locations = locator.locate(new SelectedUnit(select));

        final SingleEdit       edit   = SingleEdit.deleteRegion(select);
        assertThat(locations.isEmpty(), is(false));

        for(NamedLocation eachLocation : locations){
            final ProgramUnitLocation target  = (ProgramUnitLocation)eachLocation;
            edit.addNode(target.getNode());
        }


        final RemoveUnusedParameters    remover  = new RemoveUnusedParameters();
        final SingleEdit                resolved = Edits.resolve(edit);

        checkChangeCreation(remover, resolved);

    }



    @Test public void testTryRemovingSelectedAndUsedLocalVariable(){
        final Source  code    = InternalUtil.createSourceWithSomeUsedFieldAndLocalVariable();
        final Context context = new Context(code);

        parser.parseJava(context);


        final List<Location>    spots     = Locations.locateWord(context.getSource(), "b");
        for(Location spot : spots){
            final SourceSelection   selection = new SourceSelection(spot);


            final ProgramUnitLocator    locator    = new ProgramUnitLocator(context);
            final List<NamedLocation>   locations  = locator.locate(new SelectedUnit(selection));


            final SingleEdit       edit   = SingleEdit.deleteRegion(selection);
            assertThat(locations.isEmpty(), is(false));

            for(NamedLocation eachLocation : locations){
                final ProgramUnitLocation target  = (ProgramUnitLocation)eachLocation;
                edit.addNode(target.getNode());
            }


            final RemoveUnusedLocalVariable     remover     = new RemoveUnusedLocalVariable();
            final SingleEdit                    resolved    = Edits.resolve(edit);

            final Change  change  = remover.createChange(resolved, Maps.<String, Parameter>newHashMap());
            assertThat(change.isValid(), is(false));
        }
    }


    @Test public void testRemoveSelectedAndUnUsedLocalVariable(){
        final Source  code    = InternalUtil.createSourceWithSomeUnUsedLocalVariable();
        final Context context = new Context(code);

        parser.parseJava(context);


        final List<Location>    spots     = Locations.locateWord(context.getSource(), "b");
        for(Location spot : spots){
            final SourceSelection   selection = new SourceSelection(spot);


            final ProgramUnitLocator    locator    = new ProgramUnitLocator(context);
            final List<NamedLocation>   locations  = locator.locate(new SelectedUnit(selection));


            final SingleEdit       edit   = SingleEdit.deleteRegion(selection);
            assertThat(locations.isEmpty(), is(false));

            for(NamedLocation eachLocation : locations){
                final ProgramUnitLocation target  = (ProgramUnitLocation)eachLocation;
                edit.addNode(target.getNode());
            }


            final RemoveUnusedLocalVariable     remover     = new RemoveUnusedLocalVariable();
            final SingleEdit                    resolved    = Edits.resolve(edit);

            checkChangeCreation(remover, resolved);
        }
    }


    @Test public void testRemoveSelectedMethod(){
        final Source  code    = InternalUtil.createSourceWithUnusedMethodAndParameter();
        final Context context = new Context(code);

        parser.parseJava(context);

        final ProgramUnitLocator  locator   = new ProgramUnitLocator(context);

        final SourceSelection     selection = new SourceSelection(SourceLocation.createLocation(code, code.getContents(), 44, 48));
        final List<NamedLocation> locations = locator.locate(new SelectedUnit(selection));


        final SingleEdit       edit   = SingleEdit.deleteRegion(selection);
        assertThat(locations.isEmpty(), is(false));

        for(NamedLocation eachLocation : locations){
            final ProgramUnitLocation target  = (ProgramUnitLocation)eachLocation;
            edit.addNode(target.getNode());
        }


        final RemoveUnusedMethods   remove      = new RemoveUnusedMethods();
        final SingleEdit            resolved    = Edits.resolve(edit);

        checkChangeCreation(remove, resolved);
    }


    @Test public void testRemoveSelectedClass(){
        final Source  code    = InternalUtil.createSourceWithOneUnusedStaticNestedClass();
        final Context context = new Context(code);

        parser.parseJava(context);

        final ProgramUnitLocator  locator   = new ProgramUnitLocator(context);

        final SourceSelection     selection = new SourceSelection(SourceLocation.createLocation(code, code.getContents(), 195, 211));
        final List<NamedLocation> locations = locator.locate(new SelectedUnit(selection));


        final SingleEdit       edit   = SingleEdit.deleteRegion(selection);
        assertThat(locations.isEmpty(), is(false));

        for(NamedLocation eachLocation : locations){
            final ProgramUnitLocation target  = (ProgramUnitLocation)eachLocation;
            edit.addNode(target.getNode());
        }


        final RemoveUnusedTypes   remove      = new RemoveUnusedTypes();
        final SingleEdit          resolved    = Edits.resolve(edit);

        checkChangeCreation(remove, resolved);
    }


    @Test public void testRemoveSelectedMethodParameter(){
        final Source  code    = InternalUtil.createSourceWithUnusedMethodAndParameter();
        final Context context = new Context(code);

        parser.parseJava(context);

        final ProgramUnitLocator  locator   = new ProgramUnitLocator(context);

        final SourceSelection     selection = new SourceSelection(SourceLocation.createLocation(code, code.getContents(), 31, 34));
        final List<NamedLocation> locations = locator.locate(new SelectedUnit(selection));


        final SingleEdit       edit   = SingleEdit.deleteRegion(selection);
        assertThat(locations.isEmpty(), is(false));

        for(NamedLocation eachLocation : locations){
            final ProgramUnitLocation target  = (ProgramUnitLocation)eachLocation;
            edit.addNode(target.getNode());
        }


        final RemoveUnusedParameters    remove      = new RemoveUnusedParameters();
        final SingleEdit                resolved    = Edits.resolve(edit);

        checkChangeCreation(remove, resolved);
    }


    @Test public void testRemoveFieldTriggeredByUser(){
        final Context context = new Context(
                InternalUtil.createSourceWithUnusedField()
        );

        parser.parseJava(context);

        final UnusedFields unusedFields = new UnusedFields();
        final Set<Issue>    issues      = unusedFields.detectIssues(context);

        for(Issue each : issues){

            final ASTNode target = Iterables.getFirst(each.getAffectedNodes(), null);
            assertNotNull(target);

            final Location      loc     = Locations.locate(target);
            final SingleEdit    edit    = SingleEdit.deleteField(new SourceSelection(loc));
            edit.addNode(target);

            final RemoveUnusedFields remove = new RemoveUnusedFields();
            Change change = remove.createChange(edit, Maps.<String, Parameter>newHashMap());
            assertNotNull(change);
            assertThat(change.isValid(), is(true));
        }
    }


    @Test public void testRemoveUnusedMethod(){
        final Context context = new Context(
                InternalUtil.createSourceWithUnusedMethodAndParameter()
        );

        parser.parseJava(context);

        final UnusedMethods unusedMethods = new UnusedMethods();
        final Set<Issue>    issues        = unusedMethods.detectIssues(context);

        assertThat(issues.size(), is(2));

        final RemoveUnusedMethods remove = new RemoveUnusedMethods();
        for(Issue each : issues){
            Change change = remove.createChange(each, Maps.<String, Parameter>newHashMap());
            assertNotNull(change);
            assertThat(change.isValid(), is(true));
        }

    }


    @Test public void testTryRemovingUsedMethod(){
        final Context context = new Context(
                InternalUtil.createSourceWithJavaDocs()
        );

        parser.parseJava(context);

        final ProgramUnitLocator locator   = new ProgramUnitLocator(context);
        final List<NamedLocation>     locations = locator.locate(new MethodUnit("boom"));

        final ProgramUnitLocation target      = (ProgramUnitLocation)locations.get(0);
        final MethodDeclaration declaration   = (MethodDeclaration)target.getNode();

        final Location            loc    = Locations.locate(declaration);
        final RemoveUnusedMethods remove = new RemoveUnusedMethods();

        final SingleEdit        edit        = SingleEdit.deleteMethod(new SourceSelection(loc));
        edit.addNode(declaration);
        final Change            change      = remove.createChange(edit, Maps.<String, Parameter>newHashMap());
        assertThat(change.isValid(), is(false));
    }



    @Test public void testRemoveDetectedUnusedMethodParameter(){
        final Context context = new Context(
                InternalUtil.createSourceWithUnusedMethodAndParameter()
        );

        parser.parseJava(context);

        final UnusedParameters unusedParameters = new UnusedParameters();
        final Set<Issue>       issues           = unusedParameters.detectIssues(context);

        assertThat(issues.size(), is(1));


        final RemoveUnusedParameters remove = new RemoveUnusedParameters();
        for(Issue each : issues){
            Change change = remove.createChange(each, Maps.<String, Parameter>newHashMap());
            assertNotNull(change);
            assertThat(change.isValid(), is(true));
        }
    }


    @Test public void testTryRemovingUsedMethodParameter(){
        final Context context = new Context(
                InternalUtil.createSourceWithUsedMethodAndParameter()
        );

        parser.parseJava(context);

        final ProgramUnitLocator locator   = new ProgramUnitLocator(context);
        final List<NamedLocation>     locations = locator.locate(new ParameterUnit("msg"));

        final RemoveUnusedParameters remove = new RemoveUnusedParameters();
        for(Location each : locations){
            final ProgramUnitLocation target  = (ProgramUnitLocation)each;
            final MethodDeclaration   method  = AstUtil.exactCast(MethodDeclaration.class, target.getNode().getParent());
            if(method.getName().getIdentifier().equals("boom")){
                final SingleEdit        edit        = SingleEdit.deleteParameter(new SourceSelection(each));
                edit.addNode(target.getNode());
                final Change            change      = remove.createChange(edit, Maps.<String, Parameter>newHashMap());
                assertThat(change.isValid(), is(false));
            }

        }

    }


    @Test public void testRemoveDuplicatedMethods(){
        final Context context = new Context(
                InternalUtil.createSourceWithDuplicatedMethods()
        );

        parser.parseJava(context);

        final DuplicatedCode detector = new DuplicatedCode();
        final Set<Issue>        issues   = detector.detectIssues(context);

        assertThat(issues.size(), is(1));


        final DeduplicateCode remove = new DeduplicateCode();
        for(Issue each : issues){
            Change change = remove.createChange(each, Maps.<String, Parameter>newHashMap());
            assertNotNull(change);
            assertThat(change.isValid(), is(true));
        }

    }


    @Test public void testRemoveSelectedRegion(){
        final Source  code    = InternalUtil.createGeneralSource();
        final Context context = new Context(code);

        parser.parseJava(context);

        final ProgramUnitLocator  locator   = new ProgramUnitLocator(context);
        final SourceSelection     selection = new SourceSelection(SourceLocation.createLocation(code, code.getContents(), 88, 281));
        final List<NamedLocation> locations = locator.locate(new SelectedUnit(selection));

        final RemoveCodeRegion remove = new RemoveCodeRegion();
        final SingleEdit       edit   = SingleEdit.deleteRegion(selection);


        assertThat(locations.isEmpty(), is(false));

        for(NamedLocation eachLocation : locations){
            final ProgramUnitLocation target  = (ProgramUnitLocation)eachLocation;
            edit.addNode(target.getNode());
        }

        final Change  change  = remove.createChange(edit, Maps.<String, Parameter>newHashMap());
        assertThat(change.isValid(), is(true));
    }


    @Test public void testTryRemovingInvalidSelectedRegion(){
        final Source  code    = InternalUtil.createGeneralSource();
        final Context context = new Context(code);

        parser.parseJava(context);

        final ProgramUnitLocator  locator   = new ProgramUnitLocator(context);
        final SourceSelection     selection = new SourceSelection(SourceLocation.createLocation(code, code.getContents(), 88, 275));
        final List<NamedLocation> locations = locator.locate(new SelectedUnit(selection));
        assertThat(locations.isEmpty(), is(true)); // it's empty since this is an invalid selection
    }


    @Test public void testRenameSelectedMethod(){
        final Source  code    = InternalUtil.createSourceWithJavaDocs();
        final Context context = new Context(code);

        parser.parseJava(context);

        final ProgramUnitLocator  locator   = new ProgramUnitLocator(context);
        final SourceSelection     selection = new SourceSelection(SourceLocation.createLocation(code, code.getContents(), 53, 57));
        final List<NamedLocation> locations = locator.locate(new SelectedUnit(selection));


        final SingleEdit       edit   = SingleEdit.renameSelectedMember(selection);
        assertThat(locations.isEmpty(), is(false));

        for(NamedLocation eachLocation : locations){
            final ProgramUnitLocation target  = (ProgramUnitLocation)eachLocation;
            edit.addNode(target.getNode());
        }


        final RenameMethod  rename      = new RenameMethod();
        final SingleEdit    resolved    = Edits.resolve(edit);

        checkChangeCreation(rename, resolved);
    }


    @Test public void testRenameSelectedClass(){
        final Source  code    = InternalUtil.createSourceWithJavaDocs();
        final Context context = new Context(code);

        parser.parseJava(context);

        final ProgramUnitLocator  locator   = new ProgramUnitLocator(context);
        final SourceSelection     selection = new SourceSelection(SourceLocation.createLocation(code, code.getContents(), 6, 10));
        final List<NamedLocation> locations = locator.locate(new SelectedUnit(selection));


        final SingleEdit       edit   = SingleEdit.renameSelectedMember(selection);
        assertThat(locations.isEmpty(), is(false));

        for(NamedLocation eachLocation : locations){
            final ProgramUnitLocation target  = (ProgramUnitLocation)eachLocation;
            edit.addNode(target.getNode());
        }


        final RenameClassOrInterface    rename      = new RenameClassOrInterface();
        final SingleEdit                resolved    = Edits.resolve(edit);

        checkChangeCreation(rename, resolved);
    }


    @Test(expected = RuntimeException.class) public void testRenameSelectedWholeClass(){
        final Source  code    = InternalUtil.createSourceWithJavaDocs();
        final Context context = new Context(code);

        parser.parseJava(context);

        final ProgramUnitLocator  locator   = new ProgramUnitLocator(context);
        final SourceSelection     selection = new SourceSelection(SourceLocation.createLocation(code, code.getContents(), 0, 87));
        final List<NamedLocation> locations = locator.locate(new SelectedUnit(selection));


        final SingleEdit       edit   = SingleEdit.renameSelectedMember(selection);
        assertThat(locations.isEmpty(), is(false));

        for(NamedLocation eachLocation : locations){
            final ProgramUnitLocation target  = (ProgramUnitLocation)eachLocation;
            edit.addNode(target.getNode());
        }

        Edits.resolve(edit); // will throw an exception; cannot rename a compilation unit

        fail("Tried to rename a compilation unit, which is wrong!");

    }


    @Test public void testRenameSelectedParameter(){
        final Source  code    = InternalUtil.createSourceWithUsedField();
        final Context context = new Context(code);

        parser.parseJava(context);

        final ProgramUnitLocator  locator   = new ProgramUnitLocator(context);
        final SourceSelection     selection = new SourceSelection(SourceLocation.createLocation(code, code.getContents(), 42, 45));
        final List<NamedLocation> locations = locator.locate(new SelectedUnit(selection));


        final SingleEdit       edit   = SingleEdit.renameSelectedMember(selection);
        assertThat(locations.isEmpty(), is(false));

        for(NamedLocation eachLocation : locations){
            final ProgramUnitLocation target  = (ProgramUnitLocation)eachLocation;
            edit.addNode(target.getNode());
        }


        final RenameParam    rename      = new RenameParam();
        final SingleEdit     resolved    = Edits.resolve(edit);

        checkChangeCreation(rename, resolved);
    }


    @Test public void testRenameSelectedField(){
        final Source  code    = InternalUtil.createSourceWithUsedField();
        final Context context = new Context(code);

        parser.parseJava(context);

        final ProgramUnitLocator  locator   = new ProgramUnitLocator(context);
        final SourceSelection     selection = new SourceSelection(SourceLocation.createLocation(code, code.getContents(), 18, 19));
        final List<NamedLocation> locations = locator.locate(new SelectedUnit(selection));


        final SingleEdit       edit   = SingleEdit.renameSelectedMember(selection);
        assertThat(locations.isEmpty(), is(false));

        for(NamedLocation eachLocation : locations){
            final ProgramUnitLocation target  = (ProgramUnitLocation)eachLocation;
            edit.addNode(target.getNode());
        }


        final RenameField    rename      = new RenameField();
        final SingleEdit     resolved    = Edits.resolve(edit);

        checkChangeCreation(rename, resolved);
    }

    private static void checkChangeCreation(SourceChanger changer, SingleEdit resolved){
        final Change  change  = changer.createChange(resolved, Parameters.newMemberName("hey"));
        assertThat(change.isValid(), is(true));

        final Commit commit = change.perform().commit();

        assertThat(commit != null, is(true));

        if(commit != null){
            assertThat(commit.isValidCommit(), is(true));
        }
    }


    @After public void tearDown() throws Exception {
        parser  = null;
    }
}
