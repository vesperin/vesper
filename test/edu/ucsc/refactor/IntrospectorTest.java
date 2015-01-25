package edu.ucsc.refactor;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import edu.ucsc.refactor.internal.InternalUtil;
import edu.ucsc.refactor.util.StringUtil;
import org.junit.Test;

import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

/**
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public class IntrospectorTest {
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
        final int offset = StringUtil.offsetOf(addon);

        final Source b = Source.wrap(a, "Quicksort", addon);

        final List<Clip> clips = introspector.multiStage(b);

        final Map<Clip, List<Location>> summarized = introspector.summarize(clips, 17);
        final Map<Clip, List<Location>> adjusted   = CodeIntrospector.adjustClipspace(
                summarized,
                offset
        );

        assertThat(adjusted.isEmpty(), is(false));
        assertThat(adjusted.size() == clips.size(), is(true));

    }


    private static List<Clip> makeClipSpace(Source src, Introspector introspector){
        return introspector.multiStage(src);
    }

}
