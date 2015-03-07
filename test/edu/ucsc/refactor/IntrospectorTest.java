package edu.ucsc.refactor;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import edu.ucsc.refactor.internal.EclipseJavaParser;
import edu.ucsc.refactor.internal.InternalUtil;
import edu.ucsc.refactor.spi.find.TypeSpace;
import edu.ucsc.refactor.util.SourceFormatter;
import edu.ucsc.refactor.util.StringUtil;
import org.junit.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

/**
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public class IntrospectorTest {
    static {
      TypeSpace.getInstance();
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


    @Test public void testResolveInsertDifferences() throws Exception {
        final Source src = InternalUtil.createQuickSortSource();

        final Introspector introspector = Vesper.createIntrospector();
        final List<Clip> clipSpace = makeClipSpace(src, introspector);

        final Diff diff = introspector.differences(
                clipSpace.get(1).getSource(),
                clipSpace.get(2).getSource()
        );

        final Source resolved = diff.resolve();
        assertThat(resolved != null, is(true));
    }


    @Test public void testResolveWeirdMultistagedExample() throws Exception {
        final Source src = InternalUtil.createWeirdMultistagedClass();

        final Introspector introspector = Vesper.createIntrospector();
        final List<Clip> clipSpace = makeClipSpace(src, introspector);

        assertThat(clipSpace.size(), is(1));

        final Map<Clip, List<Location>> summaries = introspector.summarize(clipSpace, 10);
        assertThat(summaries.size(), is(1));

        for(Clip each : summaries.keySet()){
            final List<Location> foldings = summaries.get(each);
            assertThat(foldings.size(), is(4));
        }
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

    @Test
    public void testClipSpaceForwardPatching() throws Exception {
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

    @Test public void testMultistageOfIncompleteCodeExample() throws Exception {

        final Introspector introspector = Vesper.createIntrospector();
        final Source a = InternalUtil.createIncompleteQuickSortCodeExample();

        final List<String> directives = Lists.newLinkedList(introspector.detectMissingImports(a));
        final String addon = StringUtil.concat("Quicksort", true, directives);


        final Map<Clip, List<Location>> adjusted   = summarizedSpace(introspector, a, addon);

        assertThat(adjusted.isEmpty(), is(false));

    }


    @Test public void testRecommendMissingImports() throws Exception {

      final Introspector introspector = Vesper.createIntrospector();
      final Source a = InternalUtil.createSourceWithGenericsAndMissingImports();

      final Set<String> expectedImports = Sets.newHashSet(
            "java.util.PriorityQueue;",
            "java.util.List;",
            "java.util.Collections;",
            "java.util.ArrayList;"
      );

      final List<String> directives = Lists.newLinkedList(introspector.detectMissingImports(a));
      for(String each : directives){
        assertThat(expectedImports.contains(each), is(true));
      }
    }

    @Test public void testRecommendMissingImports2() throws Exception {

      final Introspector introspector = Vesper.createIntrospector();
      final Source a = InternalUtil.createSourceUsingStackoverflowExampleWithMissingImports();

      final Set<String> expectedImports = Sets.newHashSet(
            "java.util.Arrays;",
            "java.io.InputStreamReader;",
            "java.io.IOException;",
            "java.io.BufferedReader;"
      );

      final List<String> directives = Lists.newLinkedList(introspector.detectMissingImports(a));
      for(String each : directives){
        assertThat(expectedImports.contains(each), is(true));
      }
    }

    @Test public void testSummarizedMultistageOfCodeExample() throws Exception {
        final Introspector introspector = Vesper.createIntrospector();
        final Source a = InternalUtil.createIncompleteQuickSortCodeExample();

        final String addon = Source.missingHeader(introspector, a, "Quicksort");

        final Map<Clip, List<Location>> summarized = summarizedSpace(introspector, a, addon);
        for(Clip each : summarized.keySet()){
            final String content = each.getSource().getContents();
            final List<Location> locations = summarized.get(each);
            for(Location eachLocation : locations){
                final String extracted = content.substring(
                        eachLocation.getStart().getOffset(),
                        eachLocation.getEnd().getOffset()
                );

                assertThat(StringUtil.isEmpty(extracted), is(false));

            }
        }

    }



    @Test public void testDetectIssuesOnIncompleteCodeExample() throws Exception {
        final Introspector introspector = Vesper.createIntrospector();
        // TODO(Huascar) WARNING: always trim an incomplete code example before wrapping it
        final Source a = InternalUtil.createIncompleteCodeExampleWithUnusedNestedClass();

        final String addon = Source.missingHeader(introspector, a, "Greeter");
        final List<String> split = StringUtil.normalize(Splitter.on("\n").split(addon));
        final Source wrapped = Source.wrap(a, "Greeter", addon);

        final Context context = new Context(wrapped);

        new EclipseJavaParser().parseJava(context);

        final Set<Issue> issues = introspector.detectIssues(context);

        final Set<String> toCheck = Sets.newHashSet(
                "SingleIssue{name=Unused field, summary=Vesper has detected one or more unused fields in code!, from(line)=5, to(line)=5}",
                "SingleIssue{name=Unused type, summary=Vesper has detected one or more unused type declarations!, from(line)=4, to(line)=6}"
        );

        final List<Issue> reversed = Lists.reverse(Lists.newLinkedList(issues));
        for(Issue each : reversed){
            assertThat(toCheck.contains(each.more(split.size() - 1)), is(true));
        }

    }


    @Test public void testZeroDetectIssuesOnIncompleteCodeExample() throws Exception {
        final Introspector introspector = Vesper.createIntrospector();
        final Source a = InternalUtil.createIncompleteCodeExampleWithUsedNestedClass();

        final String addon = Source.missingHeader(introspector, a, "Greeter");
        final Source wrapped = Source.wrap(a, "Greeter", addon);

        final Context context = new Context(wrapped);

        new EclipseJavaParser().parseJava(context);

        final Set<Issue> issues = introspector.detectIssues(context);

        assertThat(issues.isEmpty(), is(true));


    }

    @Test public void testMultistageQuicksort() throws Exception {
        final Introspector introspector = Vesper.createIntrospector();
        final Source a = InternalUtil.createCompleteQuickSortCodeExample();
        // to use merely to test hypotheses
        clipIt(introspector, a);
    }

    @Test public void testMultistageDisorganizedCode() throws Exception {
        final Introspector introspector = Vesper.createIntrospector();
        final Source a = InternalUtil.createDisorganizedCodeExample();
        final String content = new SourceFormatter().format(a.getContents());
        final Source f = Source.from(a, content);

        // to use merely to test hypotheses
        clipIt(introspector, f);
    }

    static void clipIt(Introspector introspector, Source toMultistage){
        final List<Clip> clips = introspector.multiStage(toMultistage);
        final Map<Clip, List<Location>> summaries = introspector.summarize(clips, 10);
        // it seems Violette is the one with the bug.
        for(Clip each : summaries.keySet()){
            final List<Location> locations = summaries.get(each);
            final String content = each.getSource().getContents();
            for(Location eachLocation :  locations){
                System.out.println(content.substring(eachLocation.getStart().getOffset(),
                        eachLocation.getEnd().getOffset()));
            }
        }

    }

    @Test public void testCompilerErrorIncompleteCode() throws Exception {
        final Introspector introspector = Vesper.createIntrospector();
        final Source a = InternalUtil.createIncompleteQuickSortCodeExample();

        assertThat(introspector.detectPartialSnippet(a), is(true));

        final Source b = InternalUtil.createSourceWithOnlyStatements();
        assertThat(introspector.detectPartialSnippet(b), is(true));

        final Source c = InternalUtil.createCompleteQuickSortCodeExample();
        assertThat(introspector.detectPartialSnippet(c), is(false));

    }

    @Test public void testAdjustedSummarizedMultistageOfCodeExample() throws Exception {

        final Introspector introspector = Vesper.createIntrospector();
        final Source a = InternalUtil.createIncompleteQuickSortCodeExample();

        final List<String> directives = Lists.newLinkedList(introspector.detectMissingImports(a));
        final String addon = StringUtil.concat("Quicksort", true, directives);

        final Map<Clip, List<Location>> adjusted   = adjustSummarizedSpace(introspector, a, addon);

        for(Clip each : adjusted.keySet()){
            final String content = each.getSource().getContents();
            final List<Location> locations = adjusted.get(each);
            for(Location eachLocation : locations){
                final String extracted = content.substring(
                        eachLocation.getStart().getOffset(),
                        eachLocation.getEnd().getOffset()
                );

                assertThat(StringUtil.isEmpty(extracted), is(false));
            }
        }
    }

    @Test public void testMultistageVsAdjustedMultistageOfCodeExample() throws Exception {
        final Introspector introspector = Vesper.createIntrospector();
        final Source a = InternalUtil.createIncompleteQuickSortCodeExample();

        final String addon = Source.missingHeader(introspector, a, "Quicksort");

        final Map<Clip, List<Location>> summarized = summarizedSpace(introspector, a, addon);
        final List<String> blocks = Lists.newLinkedList();

        for(Clip each : summarized.keySet()){
            final String content = each.getSource().getContents();
            final List<Location> locations = summarized.get(each);
            for(Location eachLocation : locations){
                final String extracted = content.substring(
                        eachLocation.getStart().getOffset(),
                        eachLocation.getEnd().getOffset()
                );

                if(!"import java.util.Random;".equals(extracted)){
                    blocks.add(extracted);
                }


            }
        }


        final Map<Clip, List<Location>> adjusted   = adjustSummarizedSpace(introspector, a, addon);
        final List<String> blocks1 = Lists.newLinkedList();
        for(Clip each : adjusted.keySet()){
            final String content = each.getSource().getContents();
            final List<Location> locations = adjusted.get(each);
            for(Location eachLocation : locations){
                final String extracted = content.substring(
                        eachLocation.getStart().getOffset(),
                        eachLocation.getEnd().getOffset()
                );

                blocks1.add(extracted);
            }
        }

        final int N = Math.max(blocks.size(), blocks1.size());
        for(int idx = 0; idx < N; idx++){
            final String e  = blocks.get(idx);
            final String e1 = blocks1.get(idx);

            assertEquals(e, e1);
        }

    }

    private static Map<Clip, List<Location>> summarizedSpace(
            Introspector introspector,
            Source a,
            String addon
    ){


        final Source b = Source.wrap(a, "Quicksort", addon);
        final List<Clip> clips = introspector.multiStage(b);

        return introspector.summarize(clips, 17);
    }

    private static Map<Clip, List<Location>> adjustSummarizedSpace(Introspector introspector,
                                                                   Source a, String addon){
        final Map<Clip, List<Location>> summarized = summarizedSpace(introspector, a, addon);

        Map<Clip, List<Location>> adjusted = CodeIntrospector.adjustClipspace(summarized);

        assertThat(summarized.size() == adjusted.size(), is(true));

        return adjusted;
    }


    private static List<Clip> makeClipSpace(Source src, Introspector introspector){
        return introspector.multiStage(src);
    }

}
