package edu.ucsc.refactor.internal.changers;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import edu.ucsc.refactor.*;
import edu.ucsc.refactor.internal.*;
import edu.ucsc.refactor.internal.detectors.*;
import edu.ucsc.refactor.internal.util.AstUtil;
import edu.ucsc.refactor.internal.util.Edits;
import edu.ucsc.refactor.internal.visitors.SimpleNameVisitor;
import edu.ucsc.refactor.locators.*;
import edu.ucsc.refactor.spi.JavaParser;
import edu.ucsc.refactor.spi.JavaSnippetParser;
import edu.ucsc.refactor.spi.SourceChanger;
import edu.ucsc.refactor.util.Locations;
import edu.ucsc.refactor.util.Parameters;
import edu.ucsc.refactor.util.StringUtil;
import org.eclipse.jdt.core.dom.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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


    @Test public void testChangerForSourceWithGenerics() throws Exception {
        final Context ctx = new Context(InternalUtil.createSourceWithGenerics());
        parser.parseJava(ctx);
        assertThat(ctx.isMalformedContext(), is(true));
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
            final Edit edit        = Edit.deleteClass(new SourceSelection(loc));
            edit.addNode(declaration);
            final Change            change      = remove.createChange(edit, Maps.<String, Parameter>newHashMap());

            assertThat(change.isValid(), is(false));
        }
    }

    @Test public void testWeirdError() throws Exception {
        // rule: Source's FileName should match the class name in code snippet;
        // otherwise this will blow up
        String content =         "public class SortArray \n" +
                "{\n" +
                "    public static void main(String[] args)\n" +
                "    {\n" +
                "        int[] arr={4,6,4,2,764,23,23};\n" +
                "        sort(arr);\n" +
                "    }\n" +
                "    static void sort(int[] arr)\n" +
                "    {\n" +
                "        int k;\n" +
                "        for(int i=0;i<arr.length;i++)\n" +
                "        {\n" +
                "            for(int j=i;j<arr.length-1;j++)\n" +
                "                {\n" +
                "                    if(arr[i]<arr[j+1])\n" +
                "                    {\n" +
                "                        k=arr[j+1];\n" +
                "                        arr[j+1]=arr[i];\n" +
                "                        arr[i]=k;\n" +
                "                    }\n" +
                "                }\n" +
                "            System.out.print(arr[i]+\" \");\n" +
                "        }   \n" +
                "    }\n" +
                "}";
        final Context ctx = new Context(new Source("SortArray.java", content));
        parser.parseJava(ctx);
        assertThat(ctx.isMalformedContext(), is(false));
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


        final Edit edit   = Edit.renameSelectedMember(selection);
        assertThat(locations.isEmpty(), is(false));

        for(NamedLocation eachLocation : locations){
            final ProgramUnitLocation target  = (ProgramUnitLocation)eachLocation;
            edit.addNode(target.getNode());
        }


        final RenameMethod  rename      = new RenameMethod();
        final Edit resolved    = Edits.resolve(edit);

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


            final Edit edit   = Edit.renameSelectedMember(selection);
            assertThat(locations.isEmpty(), is(false));

            for(NamedLocation eachLocation : locations){
                final ProgramUnitLocation target  = (ProgramUnitLocation)eachLocation;
                final FieldDeclaration dec = AstUtil.parent(FieldDeclaration.class, target.getNode());
                if(dec == null){
                    edit.addNode(target.getNode());
                } else {
                    edit.addNode(dec);
                }
            }


            final RenameField  rename      = new RenameField();
            final Edit resolved    = Edits.resolve(edit);

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


            final Edit edit   = Edit.renameSelectedMember(selection);
            assertThat(locations.isEmpty(), is(false));

            for(NamedLocation eachLocation : locations){
                final ProgramUnitLocation target  = (ProgramUnitLocation)eachLocation;
                edit.addNode(target.getNode());
            }


            final RenameClassOrInterface    rename      = new RenameClassOrInterface();
            final Edit resolved    = Edits.resolve(edit);

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


            final Edit edit   = Edit.renameSelectedMember(selection);
            assertThat(locations.isEmpty(), is(false));

            for(NamedLocation eachLocation : locations){
                final ProgramUnitLocation target  = (ProgramUnitLocation)eachLocation;
                edit.addNode(target.getNode());
            }


            final RenameLocalVariable   rename      = new RenameLocalVariable();
            final Edit resolved    = Edits.resolve(edit);

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


            final Edit edit   = Edit.renameSelectedMember(selection);
            assertThat(locations.isEmpty(), is(false));

            for(NamedLocation eachLocation : locations){
                final ProgramUnitLocation target  = (ProgramUnitLocation)eachLocation;
                edit.addNode(target.getNode());
            }


            final RenameParam  rename     = new RenameParam();
            final Edit resolved   = Edits.resolve(edit);

            checkChangeCreation(rename, resolved);
        }


    }

    @Test public void testChangerForOptimizeImports() throws Exception {
        final Source  code    = InternalUtil.createGeneralSource();
        final Context context = new Context(code);

        parser.parseJava(context);

        final RemoveUnusedImports remove = new RemoveUnusedImports();
        final Edit edit   = Edit.optimizeImports(code);
        edit.addNode(context.getCompilationUnit());
        final Change              change = remove.createChange(edit, Maps.<String, Parameter>newHashMap());
        assertThat(change.isValid(), is(true));
    }


    @Test public void testChangerForOptimizeImports2() throws Exception {
        final Source  code    = InternalUtil.createSourceWithOptimizationBug();
        final Context context = new Context(code);

        parser.parseJava(context);

        final RemoveUnusedImports remove = new RemoveUnusedImports();
        final Edit edit   = Edit.optimizeImports(code);
        edit.addNode(context.getCompilationUnit());
        final Change              change = remove.createChange(edit, Maps.<String, Parameter>newHashMap());
        assertThat(change.isValid(), is(true));
    }


    @Test public void testChangerForDeduplicationError() throws Exception {
        final Source  code    = InternalUtil.createSourceWithOptimizationBug();
        final Context context = new Context(code);

        parser.parseJava(context);

        final DuplicatedCode detector = new DuplicatedCode();
        final Set<Issue>        issues   = detector.detectIssues(context);

        assertThat(issues.size() > 0, is(true));


        final DeduplicateCode remove = new DeduplicateCode();
        for(Issue each : issues){
            Change change = remove.createChange(each, Maps.<String, Parameter>newHashMap());
            assertNotNull(change);
            assertThat(change.isValid(), is(true));
        }
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

        final Edit edit   = Edit.deleteRegion(selection);
        assertThat(locations.isEmpty(), is(false));

        for(NamedLocation eachLocation : locations){
            final ProgramUnitLocation target  = (ProgramUnitLocation)eachLocation;
            edit.addNode(target.getNode());
        }


        final RemoveUnusedMethods   remove      = new RemoveUnusedMethods();
        final Edit resolved    = Edits.resolve(edit);

        checkChangeCreation(remove, resolved);
    }

    @Test public void testBasicClipSelection() throws Exception {
        final Source src = InternalUtil.createScratchedSourceForClipping();

        final Context context = new Context(src);
        parser.parseJava(context);

        final ProgramUnitLocator locator   = new ProgramUnitLocator(context);
        final SourceSelection    selection = new SourceSelection(context.getSource(), 31, 496);
        final List<NamedLocation>     locations = locator.locate(new SelectedUnit(selection));

        final Edit edit   = Edit.clipSelection(selection);
        assertThat(locations.isEmpty(), is(false));

        for(NamedLocation eachLocation : locations){
            final ProgramUnitLocation target  = (ProgramUnitLocation)eachLocation;
            edit.addNode(target.getNode());
        }


        final ClipSelection  remove     = new ClipSelection();
        final Edit resolved   = Edits.resolve(edit);

        checkChangeCreation(remove, resolved);
    }


    @Test public void testRemoveSelectedBlockFromMethod() throws Exception {
        final Source src = InternalUtil.createScratchedSourceForClipping();

        final Context context = new Context(src);
        parser.parseJava(context);

        final ProgramUnitLocator locator   = new ProgramUnitLocator(context);
        final SourceSelection    selection = new SourceSelection(context.getSource(), 76, 492);
        final List<NamedLocation>     locations = locator.locate(new SelectedUnit(selection));

        final Edit edit   = Edit.deleteRegion(selection);
        assertThat(locations.isEmpty(), is(false));

        for(NamedLocation eachLocation : locations){
            final ProgramUnitLocation target  = (ProgramUnitLocation)eachLocation;
            edit.addNode(target.getNode());
        }


        final RemoveCodeRegion  remove  = new RemoveCodeRegion();
        final Edit resolved   = Edits.resolve(edit);

        checkChangeCreation(remove, resolved);
    }


    @Test public void testInvalidRemoveSelectedBlockFromMethod() throws Exception {
        final Source src = InternalUtil.createScratchedSourceForClipping();

        final Context context = new Context(src);
        parser.parseJava(context);

        final ProgramUnitLocator locator   = new ProgramUnitLocator(context);
        final SourceSelection    selection = new SourceSelection(context.getSource(), 128, 492);
        final List<NamedLocation>     locations = locator.locate(new SelectedUnit(selection));

        final Edit edit   = Edit.deleteRegion(selection);
        assertThat(locations.isEmpty(), is(false));

        for(NamedLocation eachLocation : locations){
            final ProgramUnitLocation target  = (ProgramUnitLocation)eachLocation;
            edit.addNode(target.getNode());
        }


        final RemoveCodeRegion  remove  = new RemoveCodeRegion();
        final Edit resolved   = Edits.resolve(edit);

        final Change  change  = remove.createChange(resolved, Parameters.newMemberName("hey"));
        assertThat(change.isValid(), is(false));
    }


    @Test public void testMediumClipSelection(){
        final Source  code    = InternalUtil.createGeneralCropableSource();
        final Context context = new Context(code);

        parser.parseJava(context);

        final ProgramUnitLocator  locator   = new ProgramUnitLocator(context);
        final SourceSelection     selection = new SourceSelection(SourceLocation.createLocation(code, code.getContents(), 88, 165));
        final List<NamedLocation> locations = locator.locate(new SelectedUnit(selection));

        final Edit edit = Edit.clipSelection(selection);


        assertThat(locations.isEmpty(), is(false));

        for(NamedLocation eachLocation : locations){
            final ProgramUnitLocation target  = (ProgramUnitLocation)eachLocation;
            edit.addNode(target.getNode());
        }

        final ClipSelection  remove     = new ClipSelection();
        final Edit resolved   = Edits.resolve(edit);

        checkChangeCreation(remove, resolved);
    }


    @Test public void testAdvClipSelection(){
        final Source  code    = InternalUtil.createGeneralCropableSource();
        final Context context = new Context(code);

        parser.parseJava(context);

        final ProgramUnitLocator  locator   = new ProgramUnitLocator(context);
        final SourceSelection     selection = new SourceSelection(SourceLocation.createLocation(code, code.getContents(), 88, 238));
        final List<NamedLocation> locations = locator.locate(new SelectedUnit(selection));

        final Edit edit = Edit.clipSelection(selection);


        assertThat(locations.isEmpty(), is(false));

        for(NamedLocation eachLocation : locations){
            final ProgramUnitLocation target  = (ProgramUnitLocation)eachLocation;
            edit.addNode(target.getNode());
        }

        final ClipSelection  remove     = new ClipSelection();
        final Edit resolved   = Edits.resolve(edit);

        checkChangeCreation(remove, resolved);
    }



    @Test public void testAdv2ClipSelection(){
        final Source  code    = InternalUtil.createGeneralCropableSource2();
        final Context context = new Context(code);

        parser.parseJava(context);

        final ProgramUnitLocator  locator   = new ProgramUnitLocator(context);
        final SourceSelection     selection = new SourceSelection(SourceLocation.createLocation(code, code.getContents(), 88, 165));
        final List<NamedLocation> locations = locator.locate(new SelectedUnit(selection));

        final Edit edit = Edit.clipSelection(selection);


        assertThat(locations.isEmpty(), is(false));

        for(NamedLocation eachLocation : locations){
            final ProgramUnitLocation target  = (ProgramUnitLocation)eachLocation;
            edit.addNode(target.getNode());
        }

        final ClipSelection  remove     = new ClipSelection();
        final Edit resolved   = Edits.resolve(edit);

        checkChangeCreation(remove, resolved);
    }


    @Test public void testAdv3ClipSelection(){
        final Source  code    = InternalUtil.createGeneralCropableSource3();
        final Context context = new Context(code);

        parser.parseJava(context);

        final ProgramUnitLocator  locator   = new ProgramUnitLocator(context);
        final SourceSelection     selection = new SourceSelection(SourceLocation.createLocation(code, code.getContents(), 88, 165));
        final List<NamedLocation> locations = locator.locate(new SelectedUnit(selection));

        final Edit edit = Edit.clipSelection(selection);


        assertThat(locations.isEmpty(), is(false));

        for(NamedLocation eachLocation : locations){
            final ProgramUnitLocation target  = (ProgramUnitLocation)eachLocation;
            edit.addNode(target.getNode());
        }

        final ClipSelection  remove     = new ClipSelection();
        final Edit resolved   = Edits.resolve(edit);

        checkChangeCreation(remove, resolved);
    }


    @Test public void testAdv4ClipSelection(){
        final Source  code    = InternalUtil.createGeneralCropableSource3();
        final Context context = new Context(code);

        parser.parseJava(context);

        final ProgramUnitLocator  locator   = new ProgramUnitLocator(context);
        final SourceSelection     selection = new SourceSelection(SourceLocation.createLocation(code, code.getContents(), 167, 238));
        final List<NamedLocation> locations = locator.locate(new SelectedUnit(selection));

        final Edit edit = Edit.clipSelection(selection);


        assertThat(locations.isEmpty(), is(false));

        for(NamedLocation eachLocation : locations){
            final ProgramUnitLocation target  = (ProgramUnitLocation)eachLocation;
            edit.addNode(target.getNode());
        }

        final ClipSelection  remove     = new ClipSelection();
        final Edit resolved   = Edits.resolve(edit);

        checkChangeCreation(remove, resolved);
    }


    @Test public void testBasicAdv5ClipSelection() throws Exception {
        final Source src = InternalUtil.createSourceWithStaticNestedClass_ClippingEntireInnerClass();

        final Context context = new Context(src);
        parser.parseJava(context);

        final ProgramUnitLocator locator   = new ProgramUnitLocator(context);
        final SourceSelection    selection = new SourceSelection(context.getSource(), 1169, 1430);
        final List<NamedLocation>     locations = locator.locate(new SelectedUnit(selection));

        final Edit edit   = Edit.clipSelection(selection);
        assertThat(locations.isEmpty(), is(false));

        for(NamedLocation eachLocation : locations){
            final ProgramUnitLocation target  = (ProgramUnitLocation)eachLocation;
            edit.addNode(target.getNode());
        }


        final ClipSelection  remove     = new ClipSelection();
        final Edit resolved   = Edits.resolve(edit);

        checkChangeCreation(remove, resolved);
    }


    @Test public void testAdv6ClipSelection(){
        final Source  code    = InternalUtil.createSourceForClippingAtClassLevel();
        final Context context = new Context(code);

        parser.parseJava(context);

        final ProgramUnitLocator  locator   = new ProgramUnitLocator(context);
        final SourceSelection     selection = new SourceSelection(SourceLocation.createLocation(code, code.getContents(), 41, 723));
        final List<NamedLocation> locations = locator.locate(new SelectedUnit(selection));

        final Edit edit = Edit.clipSelection(selection);


        assertThat(locations.isEmpty(), is(false));

        for(NamedLocation eachLocation : locations){
            final ProgramUnitLocation target  = (ProgramUnitLocation)eachLocation;
            edit.addNode(target.getNode());
        }

        final ClipSelection  remove     = new ClipSelection();
        final Edit resolved   = Edits.resolve(edit);

        checkChangeCreation(remove, resolved, false);
    }


    @Test public void testUnusedImportsRemoval(){
        final Source src = InternalUtil.createToSortSource();

        final Context context = new Context(src);
        parser.parseJava(context);

        final RemoveUnusedImports remove = new RemoveUnusedImports();
        final Edit edit   = Edit.optimizeImports(src);
        edit.addNode(context.getCompilationUnit());
        final Change              change = remove.createChange(edit, Maps.<String, Parameter>newHashMap());
        assertThat(change.isValid(), is(true));

        final Commit commit = change.perform().commit();
        assertThat(commit != null, is(true));

        final Introspector i = Vesper.createIntrospector();
        assert commit != null;
        final Set<Issue> issues = i.detectIssues(commit.getSourceAfterChange());

        assertThat(issues.isEmpty(), is(false));

    }


    @Test public void testRenameShortNameParameter(){
        final Source src = InternalUtil.createSourceWithShortNameMembers();

        final Context context = new Context(src);
        parser.parseJava(context);


        final SourceSelection   selection = new SourceSelection(SourceLocation.createLocation(src, src.getContents(), 72, 74));
//      final SourceSelection   selection = new SourceSelection(SourceLocation.createLocation(src, src.getContents(), 6, 15));


        final ProgramUnitLocator    locator    = new ProgramUnitLocator(context);
        final List<NamedLocation>   locations  = locator.locate(new SelectedUnit(selection));


        final Edit edit   = Edit.renameSelectedMember(selection);
        assertThat(locations.isEmpty(), is(false));

        for(NamedLocation eachLocation : locations){
            final ProgramUnitLocation target  = (ProgramUnitLocation)eachLocation;
            edit.addNode(target.getNode());
        }


        final RenameParam   rename      = new RenameParam();
        final Edit resolved    = Edits.resolve(edit);


        final Change  change  = rename.createChange(resolved, Parameters.newMemberName("start"));
        assertThat(change.isValid(), is(true));

        final Commit commit = change.perform().commit();

        assertThat(commit != null, is(true));

        if(commit != null){
            assertThat(commit.isValidCommit(), is(true));
        }
    }


    @Test public void testRenameShortNameParameter2(){
        final Source src = InternalUtil.createSourceWithShortNameMembers2();

        final Context context = new Context(src);
        parser.parseJava(context);

//            final SourceSelection   selection = new SourceSelection(SourceLocation.createLocation(src, src.getContents(), 72, 74));
//            final SourceSelection   selection = new SourceSelection(SourceLocation.createLocation(src, src.getContents(), 7, 16));
        final SourceSelection   selection = new SourceSelection(SourceLocation.createLocation(src, src.getContents(), 166, 167));


        final ProgramUnitLocator    locator    = new ProgramUnitLocator(context);
        final List<NamedLocation>   locations  = locator.locate(new SelectedUnit(selection));


        final Edit edit   = Edit.renameSelectedMember(selection);
        assertThat(locations.isEmpty(), is(false));

        for(NamedLocation eachLocation : locations){
            final ProgramUnitLocation target  = (ProgramUnitLocation)eachLocation;
            edit.addNode(target.getNode());
        }


        final RenameLocalVariable   rename      = new RenameLocalVariable();
        final Edit resolved    = Edits.resolve(edit);


        final Change  change  = rename.createChange(resolved, Parameters.newMemberName("start"));
        assertThat(change.isValid(), is(true));

        final Commit commit = change.perform().commit();

        assertThat(commit != null, is(true));

        if(commit != null){
            assertThat(commit.isValidCommit(), is(true));
        }
    }

    @Test public void testRemoveShortNameParameter(){
        final Source src = InternalUtil.createSourceWithShortNameMembers3();

        final Context context = new Context(src);
        parser.parseJava(context);

        final SourceSelection   selection = new SourceSelection(SourceLocation.createLocation(src, src.getContents(), 170, 173));


        final ProgramUnitLocator    locator    = new ProgramUnitLocator(context);
        final List<NamedLocation>   locations  = locator.locate(new SelectedUnit(selection));


        final Edit edit   = Edit.deleteRegion(selection);
        assertThat(locations.isEmpty(), is(false));

        for(NamedLocation eachLocation : locations){
            final ProgramUnitLocation target  = (ProgramUnitLocation)eachLocation;
            edit.addNode(target.getNode());
        }


        final RemoveUnusedLocalVariable   remove      = new RemoveUnusedLocalVariable();
        final Edit resolved    = Edits.resolve(edit);


        final Change            change      = remove.createChange(resolved, Maps.<String, Parameter>newHashMap());
        assertThat(change.isValid(), is(false));
    }

    @Test public void testRenameLocalVariableAndAllItsUsages(){
        final Source src = InternalUtil.createScratchedSourceWithOneMethod();

        final Context context = new Context(src);
        parser.parseJava(context);

        final List<Location>    spots     = Locations.locateWord(context.getSource(), "arr");
        for(Location spot : spots){
            final SourceSelection   selection = new SourceSelection(spot);


            final ProgramUnitLocator    locator    = new ProgramUnitLocator(context);
            final List<NamedLocation>   locations  = locator.locate(new SelectedUnit(selection));


            final Edit edit   = Edit.renameSelectedMember(selection);
            assertThat(locations.isEmpty(), is(false));

            for(NamedLocation eachLocation : locations){
                final ProgramUnitLocation target  = (ProgramUnitLocation)eachLocation;
                edit.addNode(target.getNode());
            }


            final RenameLocalVariable   rename      = new RenameLocalVariable();
            final Edit resolved    = Edits.resolve(edit);


            final Change  change  = rename.createChange(resolved, Parameters.newMemberName("localArray"));
            assertThat(change.isValid(), is(true));

            final Commit commit = change.perform().commit();

            assertThat(commit != null, is(true));

            if(commit != null){
                assertThat(commit.isValidCommit(), is(true));
            }


        }
    }


    @Test public void testRenameClassAndAllItsUsages(){
        final Source src = InternalUtil.createSortingFromLowestToHighest();

        final Context context = new Context(src);
        parser.parseJava(context);

        final SourceSelection     selection = new SourceSelection(SourceLocation.createLocation(src, src.getContents(), 47, 54));


        final ProgramUnitLocator    locator    = new ProgramUnitLocator(context);
        final List<NamedLocation>   locations  = locator.locate(new SelectedUnit(selection));


        final Edit edit   = Edit.renameSelectedMember(selection);
        assertThat(locations.isEmpty(), is(false));

        for(NamedLocation eachLocation : locations){
            final ProgramUnitLocation target  = (ProgramUnitLocation)eachLocation;
            edit.addNode(target.getNode());
        }


        final RenameClassOrInterface    rename      = new RenameClassOrInterface();
        final Edit resolved    = Edits.resolve(edit);


        final Change  change  = rename.createChange(resolved, Parameters.newMemberName("SortingFromLowestToHighest"));
        assertThat(change.isValid(), is(true));

        final Commit commit = change.perform().commit();

        assertThat(commit != null, is(true));

        if(commit != null){
            assertThat(commit.isValidCommit(), is(true));
        }
    }



    @Test public void testRenameFieldAndAllItsUsages(){
        final Source src = InternalUtil.createScratchedSourceWithOneFieldAccessedInMethod();

        final Context context = new Context(src);
        parser.parseJava(context);

        final List<Location>    spots     = Locations.locateWord(context.getSource(), "arr");
        for(Location spot : spots){
            final SourceSelection   selection = new SourceSelection(spot);


            final ProgramUnitLocator    locator    = new ProgramUnitLocator(context);
            final List<NamedLocation>   locations  = locator.locate(new SelectedUnit(selection));


            final Edit edit   = Edit.renameSelectedMember(selection);
            assertThat(locations.isEmpty(), is(false));

            for(NamedLocation eachLocation : locations){
                final ProgramUnitLocation target  = (ProgramUnitLocation)eachLocation;
                edit.addNode(target.getNode());
            }


            final RenameField   rename      = new RenameField();
            final Edit resolved    = Edits.resolve(edit);


            final Change  change  = rename.createChange(resolved, Parameters.newMemberName("localArray"));
            assertThat(change.isValid(), is(true));

            final Commit commit = change.perform().commit();

            assertThat(commit != null, is(true));

            if(commit != null){
                assertThat(commit.isValidCommit(), is(true));
            }


        }
    }


    @Test (expected = RuntimeException.class) public void testRemoveWholeMethodSelectionFromBrokenSource() throws Exception {
        final Source src = InternalUtil.createBrokenSourceWithOneMethod();

        final Context context = new Context(src);
        parser.parseJava(context);
        Context.throwCompilationErrorIfExist(context);
        fail("if the code gets here");
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

        final Edit edit        = Edit.deleteField(new SourceSelection(loc));
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


        final Edit edit   = Edit.deleteRegion(selection);
        assertThat(locations.isEmpty(), is(false));

        for(NamedLocation eachLocation : locations){
            final ProgramUnitLocation target  = (ProgramUnitLocation)eachLocation;
            edit.addNode(target.getNode());
        }


        final RemoveUnusedFields    remove      = new RemoveUnusedFields();
        final Edit resolved    = Edits.resolve(edit);

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

        final Edit edit   = Edit.deleteRegion(select);
        assertThat(locations.isEmpty(), is(false));

        for(NamedLocation eachLocation : locations){
            final ProgramUnitLocation target  = (ProgramUnitLocation)eachLocation;
            edit.addNode(target.getNode());
        }


        final RemoveCodeRegion remover  = new RemoveCodeRegion();
        final Edit resolved = Edits.resolve(edit);

        final Change            change      = remover.createChange(resolved, Maps.<String, Parameter>newHashMap());
        assertThat(change.isValid(), is(false));

    }


    @Test public void testTryRemovingArrayStatement(){
        final Context context = new Context(
                InternalUtil.createBrokenBubbleSortSource2()
        );

        parser.parseJava(context);

        final ProgramUnitLocator locator   = new ProgramUnitLocator(context);
        final SourceSelection    select    = new SourceSelection(context.getSource(), 68, 117);
        final List<NamedLocation>     locations = locator.locate(new SelectedUnit(select));

        final Edit edit   = Edit.deleteRegion(select);
        assertThat(locations.isEmpty(), is(false));

        for(NamedLocation eachLocation : locations){
            final ProgramUnitLocation target  = (ProgramUnitLocation)eachLocation;
            edit.addNode(target.getNode());
        }


        final RemoveUnusedLocalVariable remover  = new RemoveUnusedLocalVariable();
        final Edit resolved = Edits.resolve(edit);

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

        final Edit edit   = Edit.deleteRegion(select);
        assertThat(locations.isEmpty(), is(false));

        for(NamedLocation eachLocation : locations){
            final ProgramUnitLocation target  = (ProgramUnitLocation)eachLocation;
            edit.addNode(target.getNode());
        }


        final RemoveUnusedParameters    remover  = new RemoveUnusedParameters();
        final Edit resolved = Edits.resolve(edit);

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


            final Edit edit   = Edit.deleteRegion(selection);
            assertThat(locations.isEmpty(), is(false));

            for(NamedLocation eachLocation : locations){
                final ProgramUnitLocation target  = (ProgramUnitLocation)eachLocation;
                edit.addNode(target.getNode());
            }


            final RemoveUnusedLocalVariable     remover     = new RemoveUnusedLocalVariable();
            final Edit resolved    = Edits.resolve(edit);

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


            final Edit edit   = Edit.deleteRegion(selection);
            assertThat(locations.isEmpty(), is(false));

            for(NamedLocation eachLocation : locations){
                final ProgramUnitLocation target  = (ProgramUnitLocation)eachLocation;
                edit.addNode(target.getNode());
            }

            final RemoveUnusedLocalVariable remover = new RemoveUnusedLocalVariable();
            final Edit resolved    = Edits.resolve(edit);

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


        final Edit edit   = Edit.deleteRegion(selection);
        assertThat(locations.isEmpty(), is(false));

        for(NamedLocation eachLocation : locations){
            final ProgramUnitLocation target  = (ProgramUnitLocation)eachLocation;
            edit.addNode(target.getNode());
        }


        final RemoveUnusedMethods   remove      = new RemoveUnusedMethods();
        final Edit resolved    = Edits.resolve(edit);

        checkChangeCreation(remove, resolved);
    }


    @Test public void testRemoveSelectedClass(){
        final Source  code    = InternalUtil.createSourceWithOneUnusedStaticNestedClass();
        final Context context = new Context(code);

        parser.parseJava(context);

        final ProgramUnitLocator  locator   = new ProgramUnitLocator(context);

        final SourceSelection     selection = new SourceSelection(SourceLocation.createLocation(code, code.getContents(), 195, 211));
        final List<NamedLocation> locations = locator.locate(new SelectedUnit(selection));


        final Edit edit   = Edit.deleteRegion(selection);
        assertThat(locations.isEmpty(), is(false));

        for(NamedLocation eachLocation : locations){
            final ProgramUnitLocation target  = (ProgramUnitLocation)eachLocation;
            edit.addNode(target.getNode());
        }


        final RemoveUnusedTypes   remove      = new RemoveUnusedTypes();
        final Edit resolved    = Edits.resolve(edit);

        checkChangeCreation(remove, resolved);
    }


    @Test public void testRemoveSelectedMethodParameter(){
        final Source  code    = InternalUtil.createSourceWithUnusedMethodAndParameter();
        final Context context = new Context(code);

        parser.parseJava(context);

        final ProgramUnitLocator  locator   = new ProgramUnitLocator(context);

        final SourceSelection     selection = new SourceSelection(SourceLocation.createLocation(code, code.getContents(), 31, 34));
        final List<NamedLocation> locations = locator.locate(new SelectedUnit(selection));


        final Edit edit   = Edit.deleteRegion(selection);
        assertThat(locations.isEmpty(), is(false));

        for(NamedLocation eachLocation : locations){
            final ProgramUnitLocation target  = (ProgramUnitLocation)eachLocation;
            edit.addNode(target.getNode());
        }


        final RemoveUnusedParameters    remove      = new RemoveUnusedParameters();
        final Edit resolved    = Edits.resolve(edit);

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
            final Edit edit    = Edit.deleteField(new SourceSelection(loc));
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

        final Edit edit        = Edit.deleteMethod(new SourceSelection(loc));
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
                final Edit edit        = Edit.deleteParameter(new SourceSelection(each));
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
        final SourceSelection     selection = new SourceSelection(SourceLocation.createLocation(code, code.getContents(), 88, 238));
        final List<NamedLocation> locations = locator.locate(new SelectedUnit(selection));

        final RemoveCodeRegion remove = new RemoveCodeRegion();
        final Edit edit   = Edit.deleteRegion(selection);


        assertThat(locations.isEmpty(), is(false));

        for(NamedLocation eachLocation : locations){
            final ProgramUnitLocation target  = (ProgramUnitLocation)eachLocation;
            edit.addNode(target.getNode());
        }

        final Change  change  = remove.createChange(edit, Maps.<String, Parameter>newHashMap());
        assertThat(change.isValid(), is(true));
    }


    @Test public void testRemoveInvalidSelectedRegion(){
        final Source  code    = InternalUtil.createGeneralSourceWithInvalidSelection();

        final Introspector introspector = Vesper.createIntrospector();
        final List<String> problems     = introspector.detectSyntaxErrors(code);

        assertThat(problems.isEmpty(), is(false));
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


        final Edit edit   = Edit.renameSelectedMember(selection);
        assertThat(locations.isEmpty(), is(false));

        for(NamedLocation eachLocation : locations){
            final ProgramUnitLocation target  = (ProgramUnitLocation)eachLocation;
            edit.addNode(target.getNode());
        }


        final RenameMethod  rename      = new RenameMethod();
        final Edit resolved    = Edits.resolve(edit);

        checkChangeCreation(rename, resolved);
    }


    @Test public void testRenameSelectedClass(){
        final Source  code    = InternalUtil.createSourceWithJavaDocs();
        final Context context = new Context(code);

        parser.parseJava(context);

        final ProgramUnitLocator  locator   = new ProgramUnitLocator(context);
        final SourceSelection     selection = new SourceSelection(SourceLocation.createLocation(code, code.getContents(), 6, 10));
        final List<NamedLocation> locations = locator.locate(new SelectedUnit(selection));


        final Edit edit   = Edit.renameSelectedMember(selection);
        assertThat(locations.isEmpty(), is(false));

        for(NamedLocation eachLocation : locations){
            final ProgramUnitLocation target  = (ProgramUnitLocation)eachLocation;
            edit.addNode(target.getNode());
        }


        final RenameClassOrInterface    rename      = new RenameClassOrInterface();
        final Edit resolved    = Edits.resolve(edit);

        checkChangeCreation(rename, resolved);
    }


    @Test(expected = RuntimeException.class) public void testRenameSelectedWholeClass(){
        final Source  code    = InternalUtil.createSourceWithJavaDocs();
        final Context context = new Context(code);

        parser.parseJava(context);

        final ProgramUnitLocator  locator   = new ProgramUnitLocator(context);
        final SourceSelection     selection = new SourceSelection(SourceLocation.createLocation(code, code.getContents(), 0, 87));
        final List<NamedLocation> locations = locator.locate(new SelectedUnit(selection));


        final Edit edit   = Edit.renameSelectedMember(selection);
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


        final Edit edit   = Edit.renameSelectedMember(selection);
        assertThat(locations.isEmpty(), is(false));

        for(NamedLocation eachLocation : locations){
            final ProgramUnitLocation target  = (ProgramUnitLocation)eachLocation;
            edit.addNode(target.getNode());
        }


        final RenameParam    rename      = new RenameParam();
        final Edit resolved    = Edits.resolve(edit);

        checkChangeCreation(rename, resolved);
    }


    @Test public void testRenameSelectedField(){
        final Source  code    = InternalUtil.createSourceWithUsedField();
        final Context context = new Context(code);

        parser.parseJava(context);

        final ProgramUnitLocator  locator   = new ProgramUnitLocator(context);
        final SourceSelection     selection = new SourceSelection(SourceLocation.createLocation(code, code.getContents(), 18, 19));
        final List<NamedLocation> locations = locator.locate(new SelectedUnit(selection));


        final Edit edit   = Edit.renameSelectedMember(selection);
        assertThat(locations.isEmpty(), is(false));

        for(NamedLocation eachLocation : locations){
            final ProgramUnitLocation target  = (ProgramUnitLocation)eachLocation;
            edit.addNode(target.getNode());
        }


        final RenameField    rename      = new RenameField();
        final Edit resolved    = Edits.resolve(edit);

        checkChangeCreation(rename, resolved);
    }

    @Test public void testRecoveryOfStatementsFromBrokenCodeSnippet() throws Exception {
        final Source  code    = InternalUtil.createSourceWithMissingImports();
        final Context context = new Context(code);

        parser.parseJava(context);

        final CompilationUnit unit = context.getCompilationUnit();


        final SimpleNameVisitor visitor = new SimpleNameVisitor(Locations.locate(unit));
        context.accept(visitor);

        final Set<String> BINDINGS = new HashSet<String>();
        for(SimpleName name : visitor.getNames()){
            final IBinding binding = AstUtil.getDeclaration(name.resolveBinding());
            if(binding != null && IBinding.TYPE == binding.getKind()){
                BINDINGS.add(binding.getName());
            }
        }

        assertThat(BINDINGS.isEmpty(), is(false));

        final Introspector introspector = Vesper.createIntrospector();
        final Set<String>  required     = introspector.detectMissingImports(code);
        assertThat(required.size(), is(1));

    }


    @Test public void testRecoveryOfStatementsFromOnlyStatementsCodeSnippet() throws Exception {
        final Source  code    = InternalUtil.createSourceWithOnlyStatements();
        final Context context = new Context(code);

        final CompilationUnit compilationUnit = AstUtil.getCompilationUnit(
                parser.parseJava(
                        context,
                        EclipseJavaParser.PARSE_STATEMENTS
                )
        );


        final Set<String> imports  = AstUtil.getUsedTypesInCode(compilationUnit);
        assertThat(imports.size(), is(9));

        final Introspector introspector = Vesper.createIntrospector();
        final Set<String>  required     = introspector.detectMissingImports(code);
        assertThat(required.isEmpty(), is(false));

        final Set<String> staticImports  = AstUtil.getUsedStaticTypesInCode(compilationUnit);
        assertThat(staticImports.size(), is(0));

    }

    @Test public void testCodeSnippetParsing() throws Exception {
        final Source  code    = InternalUtil.createSourceWithOnlyStatements();
        final Context context = new Context(code);

        final JavaSnippetParser p = new EclipseJavaSnippetParser();
        final ResultPackage result = p.offer(context);

        assertThat(result.getParsedNode().getClass() == TypeDeclaration.class, is(true));

        final List<ASTNode> children = AstUtil.getChildren(result.getParsedNode());
        for(ASTNode child : children){
            if(AstUtil.isOfType(Block.class, child)){
                final List<Statement> statements = AstUtil.getStatements(child);
                assertThat(statements.isEmpty(), is(false));
            }
        }

        final Source code1 = InternalUtil.createSourceWithMissingImports();
        final Context context1 = new Context(code1);


        final ResultPackage result1 = p.offer(context1);
        assertThat(result1.getParsedNode().getClass() == CompilationUnit.class, is(true));
    }

    @Test public void testMalformedCodeSnippetParsing() throws Exception {
        final Source  code    = InternalUtil.createSourceWithCommentsAndStatements();
        final Context context = new Context(code);

        final JavaSnippetParser p = new EclipseJavaSnippetParser();
        final ResultPackage result = p.offer(context);

        assertThat(result.getParsedNode().getClass() == TypeDeclaration.class, is(true));

    }

    @Test public void testMethodClipSelection() throws Exception {
        final Source src = InternalUtil.createQuickSortSource();

        final Context context = new Context(src);
        parser.parseJava(context);

        final ProgramUnitLocator locator   = new ProgramUnitLocator(context);
        final SourceSelection    selection = new SourceSelection(context.getSource(), 329, 538);
        final List<NamedLocation>     locations = locator.locate(new SelectedUnit(selection));

        final Edit edit   = Edit.clipSelection(selection);
        assertThat(locations.isEmpty(), is(false));

        for(NamedLocation eachLocation : locations){
            final ProgramUnitLocation target  = (ProgramUnitLocation)eachLocation;
            edit.addNode(target.getNode());
        }


        final ClipSelection  remove     = new ClipSelection();
        final Edit resolved   = Edits.resolve(edit);

        checkChangeCreation(remove, resolved);
    }


    @Test public void testClipSpaceGeneration() throws Exception {
        final Source src = InternalUtil.createQuickSortSource();

        final List<Clip> clipSpace = makeClipSpace(src, Vesper.createIntrospector());

        assertThat(clipSpace.isEmpty(), is(false));

    }

    @Test public void testDeleteDifferences() throws Exception {
        final Source src = InternalUtil.createQuickSortSource();

        final Introspector introspector = Vesper.createIntrospector();
        final List<Clip> clipSpace = makeClipSpace(src, introspector);
        final List<Clip> clipOneToBaseClip  = ImmutableList.of(clipSpace.get(clipSpace.size() -
                1), clipSpace.get(1));

        final Diff diff = introspector.differences(
                clipOneToBaseClip.get(0).getSource(),
                clipOneToBaseClip.get(1).getSource()
        );

        assertThat(diff.getChangesFromOriginal().isEmpty(), is(true));
        assertThat(diff.getDeletesFromOriginal().isEmpty(), is(false));
    }


    @Test public void testInsertDifferences() throws Exception {
        final Source src = InternalUtil.createQuickSortSource();

        final Introspector introspector = Vesper.createIntrospector();
        final List<Clip> clipSpace = makeClipSpace(src, introspector);

        final Diff diff = introspector.differences(
                clipSpace.get(1).getSource(),
                clipSpace.get(2).getSource()
        );

        assertThat(diff.getChangesFromOriginal().isEmpty(), is(true));
        assertThat(diff.getInsertsFromOriginal().isEmpty(), is(false));
    }

    @Test public void testChangeDifferences() throws Exception {
        final Source src = InternalUtil.createQuickSortSource();

        final Introspector introspector = Vesper.createIntrospector();
        final List<Clip> clipSpace = makeClipSpace(src, introspector);

        final Source a = clipSpace.get(0).getSource();
        final Source b = clipSpace.get(1).getSource();

        final Source c = Source.from(b, b.getContents().replaceAll("swap", "exchange"));

        final Diff diff = introspector.differences(
                a,
                c
        );

        assertThat(diff.getChangesFromOriginal().isEmpty(), is(false));
        assertThat(diff.getInsertsFromOriginal().isEmpty(), is(true));
    }


    @Test public void testWrappingOfIncompleteExample() throws Exception {
        final Source a = InternalUtil.createMethodOnlyCodeExample();
        final Source b = InternalUtil.createMethodWithShellCodeExample();

        final Introspector introspector = Vesper.createIntrospector();

        final Diff diff = introspector.differences(b, a);
        final Source c  = diff.resolve();
        assertThat(c != null, is(true));

        final Diff diff1 = introspector.differences(a, b);
        final Source d   = diff1.resolve();
        assertThat(d != null, is(true));
    }

    @Test public void testContentAdjustmentOfIncompleteCodeExample() throws Exception {
        final Source a = InternalUtil.createMethodOnlyCodeExample();
        final Source b = InternalUtil.createMethodWithShellCodeExample();
        final Introspector introspector = Vesper.createIntrospector();

        final List<String> directives = Lists.newLinkedList(introspector.detectMissingImports(a));

        final Source patched = Source.wrap(
                a,
                "WellManners",
                StringUtil.concat(
                        "WellManners",
                        true,
                        directives
                )
        );

        assertThat(patched.getName(), is("WellManners.java"));
        assertThat(patched.equals(b), is(true));

    }

    @Test public void testCompileIncompleteCodeExample() throws Exception {
        final Introspector introspector = Vesper.createIntrospector();
        final Source a = InternalUtil.createMethodOnlyCodeExample();

        final List<String> directives = Lists.newLinkedList(introspector.detectMissingImports(a));
        final Source b = Source.wrap(
                a,
                "WellManners",
                StringUtil.concat(
                        "WellManners",
                        true,
                        directives
                )
        );


        final Context context = new Context(b);
        parser.parseJava(context);

        Context.throwCompilationErrorIfExist(context);

        assertNotNull(context);
        assertNotNull(context.getCompilationUnit());

        final Context flawedContext = new Context(a);
        try {
            parser.parseJava(flawedContext);
            Context.throwCompilationErrorIfExist(flawedContext);
            fail("Error if we got here");
        } catch (Exception e){
            // good
        }
    }


    @Test public void testCropContentFromSource() throws Exception {
        final Introspector introspector = Vesper.createIntrospector();

        final Source a = InternalUtil.createMethodOnlyCodeExample();
        final Source b = InternalUtil.createMethodWithShellCodeExample();

        final List<String> directives = Lists.newLinkedList(introspector.detectMissingImports(a));
        final Source c = Source.wrap(
                a,
                "WellManners",
                StringUtil.concat(
                        "WellManners",
                        true,
                        directives
                )
        );

        assertThat(c.equals(b), is(true));

        final Source d = renameMethod(c, "greet", "hello");

        final Source f = Source.unwrap(d);
        assertNotNull(f);
        assertThat(d.equals(f), is(false));
    }


    @Test public void testCropContentFromMoreComplexSource() throws Exception {
        final Introspector introspector = Vesper.createIntrospector();
        final Source a = InternalUtil.createIncompleteQuickSortCodeExample();
        final List<String> directives = Lists.newLinkedList(introspector.detectMissingImports(a));
        final Source b = Source.wrap(
                a,
                "Quicksort",
                StringUtil.concat(
                        "Quicksort",
                        true,
                        directives
                )
        );

        final Context context = new Context(b);
        parser.parseJava(context);

        final Source c = renameMethod(
                deleteMethod(context, "main"),
                "randomizedPartition",
                "randomPartition"
        );

        final Source d = Source.unwrap(c);
        assertNotNull(d);
        assertThat(d.equals(InternalUtil.updatedIncompleteQuickSortCodeExample()), is(true));
    }


    @Test public void testCropContentWithOffsetAdjustment() throws Exception {
        final Source a = InternalUtil.createIncompleteQuickSortCodeExample();

        final int startOffset = 300;
        final int endOffset   = 319;

        final SourceSelection adjusted = Vesper.createAdjustedSelection(startOffset, endOffset, a);

        final Context context = new Context(adjusted.getSource());
        parser.parseJava(context);

        adjusted.setContext(context);

        final ProgramUnitLocator locator   = new ProgramUnitLocator(context);
        final List<NamedLocation>   locations  = locator.locate(new SelectedUnit(adjusted));

        assertThat(locations.isEmpty(), is(false));

        final Edit edit   = Edit.renameSelectedMember(adjusted);

        for(NamedLocation eachLocation : locations){
            final ProgramUnitLocation target  = (ProgramUnitLocation)eachLocation;
            edit.addNode(target.getNode());
        }


        final RenameMethod  rename      = new RenameMethod();
        final Edit          resolved    = Edits.resolve(edit);

        final Change  change  = rename.createChange(resolved, Parameters.newMemberName
                ("randomPartition"));

        final Commit commit = change.perform().commit();

        assertNotNull(commit);
        assertThat(commit.isValidCommit(), is(true));

        final Source result = commit.getSourceAfterChange();
        final Source cropped = Source.unwrap(result);
        assertNotNull(cropped);
    }

    private static Source deleteMethod(Context context, String methodName){
        final ProgramUnitLocator locator   = new ProgramUnitLocator(context);
        final List<NamedLocation>     locations = locator.locate(new MethodUnit(methodName));

        final ProgramUnitLocation target      = (ProgramUnitLocation)locations.get(0);
        final MethodDeclaration declaration   = (MethodDeclaration)target.getNode();

        final Location            loc    = Locations.locate(declaration);
        final RemoveUnusedMethods remove = new RemoveUnusedMethods();

        final Edit edit        = Edit.deleteMethod(new SourceSelection(loc));
        edit.addNode(declaration);
        final Change            change      = remove.createChange(edit, Maps.<String, Parameter>newHashMap());

        final Commit commit = change.perform().commit();
        if(commit != null && commit.isValidCommit()){
            return commit.getSourceAfterChange();
        } else {
            return context.getSource(); // unable to make change
        }
    }

    private static Source renameMethod(Source src, String targetMethod, String name){
        final JavaParser    parser  = new EclipseJavaParser();
        final Context       context = new Context(src);

        parser.parseJava(context);

        final List<Location>    spots     = Locations.locateWord(src, targetMethod);
        final Location          spot      = spots.get(0);

        final SourceSelection   selection = new SourceSelection(spot);


        final ProgramUnitLocator    locator    = new ProgramUnitLocator(context);
        final List<NamedLocation>   locations  = locator.locate(new SelectedUnit(selection));


        final Edit edit   = Edit.renameSelectedMember(selection);

        for(NamedLocation eachLocation : locations){
            final ProgramUnitLocation target  = (ProgramUnitLocation)eachLocation;
            edit.addNode(target.getNode());
        }


        final RenameMethod  rename      = new RenameMethod();
        final Edit          resolved    = Edits.resolve(edit);

        final Change  change  = rename.createChange(resolved, Parameters.newMemberName(name));

        final Commit commit = change.perform().commit();
        if(commit != null && commit.isValidCommit()){
            return commit.getSourceAfterChange();
        } else {
            return src; // unable to make change
        }
    }


    @Ignore @Test public void testCallingFindJarSearchEngine() throws Exception {
        //http://www.findjar.com/index.jsp?query=
        String address = "http://www.findjar.com/index.x?query=";
        String query = "ImmutableList";
        String charset = "UTF-8";

        URL url = new URL(address + URLEncoder.encode(query, charset));

        BufferedReader in = new BufferedReader(new InputStreamReader(
                url.openStream()));
        String str;

        while ((str = in.readLine()) != null) {
            System.out.println(str);
        }

        in.close();
    }

    @Test public void testClipSpaceForwardPatching() throws Exception {
        final Source src = InternalUtil.createQuickSortSource();

        final Introspector introspector = Vesper.createIntrospector();
        final List<Clip> clipSpace = makeClipSpace(src, introspector);

        final Clip clip = Clip.sync(
                introspector,
                clipSpace.subList(1, clipSpace.size())
        );

        assertThat(clip != null, is(true));

        assert clip != null;
        final Source patched = clip.getSource();

        final String expected  = clipSpace.get(clipSpace.size() - 1).getSource().getContents();
        final String revised   = patched.getContents();


        assertThat(revised.equals(expected), is(true));
    }

    @Test public void testSyncingClipAfterBaseWithBaseClip() throws Exception {
        final Source src = InternalUtil.createQuickSortSource();

        final Introspector introspector = Vesper.createIntrospector();
        final List<Clip> clipSpace = makeClipSpace(src, introspector);
        final List<Clip> clipOneToBaseClip  = clipSpace.subList(0, 2);

        final Clip   clip      = Clip.sync(introspector, clipOneToBaseClip);

        assert clip != null;
        final Source patched   = clip.getSource();
        final String expected  = clipOneToBaseClip.get(clipOneToBaseClip.size() - 1).getSource().getContents();
        final String revised   = patched.getContents();
        assertThat(revised.equals(expected), is(true));
    }


    @Test public void testSummarizeSingleSourceCode() throws Exception {
        final Source src = InternalUtil.createQuickSortSource();

        final Introspector introspector = Vesper.createIntrospector();
        List<Location> foldingLocations = introspector.summarize("quicksort", src, 17);
        assertThat(foldingLocations.isEmpty(), is(false));
    }

    @Test public void testSummarizeSingleMethodAndLongSourceCode() throws Exception {
        final Source src = InternalUtil.createSourceWithShortNameMembers();

        final Introspector introspector = Vesper.createIntrospector();
        List<Location> foldingLocations = introspector.summarize("qsort", src, 17);
        assertThat(foldingLocations.isEmpty(), is(false));

    }


    @Test public void testSummarizeSourceCodeWithStaticNestedClass() throws Exception {
        final Source src = InternalUtil.createSourceWithStaticNestedClass_ClippingEntireInnerClass();

        final Introspector introspector = Vesper.createIntrospector();
        List<Location> foldingLocations = introspector.summarize("main", src, 17);
        assertThat(foldingLocations.isEmpty(), is(false));

    }

    @Test public void testSummarizeAllPossibleClips() throws Exception {
        final Source src = InternalUtil.createQuickSortSource();

        final Introspector introspector = Vesper.createIntrospector();
        Map<Clip, List<Location>> allSummaries = introspector.summarize(introspector.multiStage
                (src), 17);
        assertThat(allSummaries.isEmpty(), is(false));
    }


    @Test public void testSummarizeClipByRanking() throws Exception {
        final Source src = InternalUtil.createQuickSortSource();

        final Introspector introspector = Vesper.createIntrospector();
        List<Clip> clips = introspector.multiStage(src);

        final Clip clip = Clip.find(src, clips);
        assertNotNull(clip);

        List<Location> foldingLocations = introspector.summarize(clip, 17);
        assertThat(foldingLocations.isEmpty(), is(false));
    }



    @Test public void testMultiStageBrokenCode() throws Exception {
        final Source src = InternalUtil.createGeneralBrokenSource();

        final Introspector introspector = Vesper.createIntrospector();
        List<Clip> clips = introspector.multiStage(src);

        assertThat(clips.isEmpty(), is(false));
    }


    private static List<Clip> makeClipSpace(Source src, Introspector introspector){
        return introspector.multiStage(src);
    }

    private static void  checkChangeCreation(SourceChanger changer, Edit resolved){
       checkChangeCreation(changer, resolved, true);
    }

    private static void checkChangeCreation(SourceChanger changer, Edit resolved, boolean target){
        final Change  change  = changer.createChange(resolved, Parameters.newMemberName("hey"));
        assertThat(change.isValid(), is(target));

        final Commit commit = change.perform().commit();

        assertThat(commit != null, is(true));

        if(commit != null){
            assertThat(commit.isValidCommit(), is(target));
        }
    }


    @After public void tearDown() throws Exception {
        parser  = null;
    }
}
